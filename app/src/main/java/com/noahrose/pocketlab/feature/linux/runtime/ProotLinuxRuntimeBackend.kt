package com.noahrose.pocketlab.feature.linux.runtime

import android.util.Log
import com.noahrose.pocketlab.feature.linux.runtime.activity.LinuxRuntimeActivityReporter
import com.noahrose.pocketlab.feature.linux.runtime.handshake.LinuxGuestHandshake
import com.noahrose.pocketlab.feature.linux.runtime.handshake.LinuxGuestHandshakeResult
import com.noahrose.pocketlab.feature.linux.runtime.network.LinuxGuestDnsManager
import com.noahrose.pocketlab.feature.linux.runtime.network.LinuxGuestDnsSyncResult
import com.noahrose.pocketlab.feature.linux.runtime.process.AndroidLinuxProcessLauncher
import com.noahrose.pocketlab.feature.linux.runtime.process.LinuxProcessHandle
import com.noahrose.pocketlab.feature.linux.runtime.process.LinuxProcessLaunchResult
import com.noahrose.pocketlab.feature.linux.runtime.process.LinuxProotProcessSpecFactory
import com.noahrose.pocketlab.feature.linux.runtime.process.LinuxProotProcessSpecResult
import java.io.File

object ProotLinuxRuntimeBackend :
    LinuxRuntimeBackend {

    private const val TAG =
        "AtlasProot"

    private const val GUEST_TAG =
        "AtlasGuest"

    private const val DNS_TAG =
        "AtlasDns"

    private val processLauncher =
        AndroidLinuxProcessLauncher()

    /*
     * Native PRoot process currently owned by Atlas.
     */
    private var activeProcess:
            LinuxProcessHandle? =
        null

    /*
     * Logical runtime session associated with the
     * active PRoot process.
     */
    private var activeSession:
            LinuxRuntimeSession? =
        null

    /*
     * Last runtime startup or shutdown error.
     */
    private var lastError:
            String? =
        null

    /*
     * Last Ubuntu guest handshake output.
     */
    private var lastGuestOutput:
            String? =
        null

    fun getLastError():
            String? {

        return lastError
    }

    fun getLastGuestOutput():
            String? {

        return lastGuestOutput
    }

    override fun start():
            LinuxRuntimeBackendResult {

        Log.i(
            TAG,
            "Runtime backend start requested."
        )

        /*
         * Do not create a second PRoot runtime.
         */
        val existingProcess =
            activeProcess

        if (
            existingProcess != null &&
            existingProcess.isAlive
        ) {

            LinuxRuntimeActivityReporter
                .success(
                    "Existing PRoot process is already active."
                )

            val existingSession =
                activeSession
                    ?: LinuxRuntimeSession(
                        processId =
                            existingProcess.processId,

                        startedAtEpochMillis =
                            System.currentTimeMillis(),

                        workingDirectory =
                            "/root"
                    )

            activeSession =
                existingSession

            lastError =
                null

            return LinuxRuntimeBackendResult
                .Success(
                    session =
                        existingSession
                )
        }

        /*
         * Clear stale backend references.
         */
        activeProcess =
            null

        activeSession =
            null

        lastError =
            null

        lastGuestOutput =
            null

        /*
         * ------------------------------------------------
         * BUILD PROOT SPECIFICATION
         * ------------------------------------------------
         */
        LinuxRuntimeActivityReporter
            .info(
                "Building PRoot launch specification."
            )

        val specResult =
            try {

                LinuxProotProcessSpecFactory
                    .create()

            } catch (
                exception: Exception
            ) {

                val message =
                    exception.message
                        ?: "Unexpected failure while building the PRoot launch specification."

                lastError =
                    message

                LinuxRuntimeActivityReporter
                    .error(
                        message
                    )

                Log.e(
                    TAG,
                    message,
                    exception
                )

                return LinuxRuntimeBackendResult
                    .Failure(
                        message =
                            message
                    )
            }

        val spec =
            when (
                specResult
            ) {

                is LinuxProotProcessSpecResult.Ready -> {

                    LinuxRuntimeActivityReporter
                        .success(
                            "PRoot launch specification ready."
                        )

                    specResult.spec
                }

                is LinuxProotProcessSpecResult.Failure -> {

                    lastError =
                        specResult.message

                    LinuxRuntimeActivityReporter
                        .error(
                            specResult.message
                        )

                    Log.e(
                        TAG,
                        "Launch specification failed: ${specResult.message}"
                    )

                    return LinuxRuntimeBackendResult
                        .Failure(
                            message =
                                specResult.message
                        )
                }
            }

        /*
         * ------------------------------------------------
         * DNS SYNCHRONIZATION
         * ------------------------------------------------
         */
        LinuxRuntimeActivityReporter
            .info(
                "Synchronizing Ubuntu DNS configuration."
            )

        synchronizeGuestDns(
            rootfsDirectory =
                spec.workingDirectory
        )

        /*
         * Detailed absolute paths remain in Logcat.
         *
         * The user-facing activity history intentionally
         * reports concise runtime events.
         */
        Log.i(
            TAG,
            "PRoot executable: ${spec.executable.absolutePath}"
        )

        Log.i(
            TAG,
            "Working directory: ${spec.workingDirectory?.absolutePath}"
        )

        Log.i(
            TAG,
            "Arguments: ${spec.arguments.joinToString(" ")}"
        )

        /*
         * Verify external PRoot loader diagnostics.
         */
        val loaderPath =
            spec.environment[
                "PROOT_LOADER"
            ]

        Log.i(
            TAG,
            "PROOT_LOADER: ${loaderPath ?: "NOT SET"}"
        )

        if (
            loaderPath != null
        ) {

            val loaderFile =
                File(
                    loaderPath
                )

            Log.i(
                TAG,
                "PRoot loader " +
                        "exists=${loaderFile.exists()} " +
                        "file=${loaderFile.isFile} " +
                        "executable=${loaderFile.canExecute()} " +
                        "size=${
                            if (
                                loaderFile.exists()
                            ) {
                                loaderFile.length()
                            } else {
                                0L
                            }
                        }"
            )
        }

        /*
         * ------------------------------------------------
         * START NATIVE PROOT PROCESS
         * ------------------------------------------------
         */
        LinuxRuntimeActivityReporter
            .info(
                "Launching native PRoot process."
            )

        val launchResult =
            try {

                processLauncher
                    .launch(
                        spec
                    )

            } catch (
                exception: Exception
            ) {

                val message =
                    exception.message
                        ?: "Unexpected failure while launching PRoot."

                lastError =
                    message

                LinuxRuntimeActivityReporter
                    .error(
                        message
                    )

                Log.e(
                    TAG,
                    "PRoot launch failed: $message",
                    exception
                )

                return LinuxRuntimeBackendResult
                    .Failure(
                        message =
                            message
                    )
            }

        return when (
            launchResult
        ) {

            is LinuxProcessLaunchResult.Success -> {

                LinuxRuntimeActivityReporter
                    .info(
                        "Android created the PRoot process."
                    )

                handleSuccessfulProcessLaunch(
                    process =
                        launchResult.process
                )
            }

            is LinuxProcessLaunchResult.Failure -> {

                activeProcess =
                    null

                activeSession =
                    null

                lastError =
                    launchResult.message

                LinuxRuntimeActivityReporter
                    .error(
                        launchResult.message
                    )

                Log.e(
                    TAG,
                    "PRoot launch failed: ${launchResult.message}",
                    launchResult.cause
                )

                LinuxRuntimeBackendResult
                    .Failure(
                        message =
                            launchResult.message
                    )
            }
        }
    }

    private fun handleSuccessfulProcessLaunch(
        process: LinuxProcessHandle
    ): LinuxRuntimeBackendResult {

        /*
         * ProcessBuilder successfully creating a process
         * does not prove PRoot remained alive.
         */
        LinuxRuntimeActivityReporter
            .info(
                "Verifying PRoot process state."
            )

        if (
            !process.isAlive
        ) {

            val message =
                "PRoot process exited immediately after launch."

            lastError =
                message

            LinuxRuntimeActivityReporter
                .error(
                    message
                )

            Log.e(
                TAG,
                message
            )

            cleanupFailedProcess(
                process =
                    process
            )

            return LinuxRuntimeBackendResult
                .Failure(
                    message =
                        message
                )
        }

        LinuxRuntimeActivityReporter
            .success(
                "PRoot process is alive."
            )

        /*
         * ------------------------------------------------
         * UBUNTU GUEST HANDSHAKE
         * ------------------------------------------------
         *
         * A living PRoot process alone does not prove that
         * Ubuntu is functioning.
         */
        LinuxRuntimeActivityReporter
            .info(
                "Beginning Ubuntu guest handshake."
            )

        Log.i(
            GUEST_TAG,
            "Beginning Ubuntu guest handshake."
        )

        val handshake =
            try {

                LinuxGuestHandshake
                    .execute(
                        process
                    )

            } catch (
                exception: Exception
            ) {

                val message =
                    exception.message
                        ?: "Ubuntu guest handshake threw an unexpected exception."

                lastError =
                    message

                LinuxRuntimeActivityReporter
                    .error(
                        message
                    )

                Log.e(
                    GUEST_TAG,
                    message,
                    exception
                )

                cleanupFailedProcess(
                    process =
                        process
                )

                return LinuxRuntimeBackendResult
                    .Failure(
                        message =
                            message
                    )
            }

        return when (
            handshake
        ) {

            is LinuxGuestHandshakeResult.Success -> {

                handleSuccessfulHandshake(
                    process =
                        process,

                    handshake =
                        handshake
                )
            }

            is LinuxGuestHandshakeResult.Failure -> {

                handleFailedHandshake(
                    process =
                        process,

                    handshake =
                        handshake
                )
            }
        }
    }

    private fun handleSuccessfulHandshake(
        process: LinuxProcessHandle,
        handshake: LinuxGuestHandshakeResult.Success
    ): LinuxRuntimeBackendResult {

        lastGuestOutput =
            handshake.output

        handshake
            .output
            .lines()
            .forEach { line ->

                if (
                    line.isNotBlank()
                ) {

                    Log.i(
                        GUEST_TAG,
                        line
                    )
                }
            }

        if (
            handshake
                .errorOutput
                .isNotBlank()
        ) {

            Log.w(
                GUEST_TAG,
                "Guest stderr: ${
                    handshake
                        .errorOutput
                        .trim()
                }"
            )
        }

        LinuxRuntimeActivityReporter
            .success(
                "Ubuntu guest handshake verified."
            )

        Log.i(
            GUEST_TAG,
            "Ubuntu guest handshake VERIFIED."
        )

        /*
         * Verify that PRoot survived the complete guest
         * handshake before exposing a session.
         */
        LinuxRuntimeActivityReporter
            .info(
                "Verifying PRoot process after guest handshake."
            )

        if (
            !process.isAlive
        ) {

            val message =
                "PRoot process exited after the Ubuntu guest handshake."

            lastError =
                message

            LinuxRuntimeActivityReporter
                .error(
                    message
                )

            Log.e(
                TAG,
                message
            )

            cleanupFailedProcess(
                process =
                    process
            )

            return LinuxRuntimeBackendResult
                .Failure(
                    message =
                        message
                )
        }

        /*
         * Ubuntu has now demonstrated a functional guest
         * environment and the native process remains alive.
         */
        val session =
            LinuxRuntimeSession(
                processId =
                    process.processId,

                startedAtEpochMillis =
                    System.currentTimeMillis(),

                workingDirectory =
                    "/root"
            )

        activeProcess =
            process

        activeSession =
            session

        lastError =
            null

        LinuxRuntimeActivityReporter
            .success(
                "Ubuntu runtime session established."
            )

        Log.i(
            TAG,
            "Ubuntu runtime session started. " +
                    "pid=${
                        session.processId
                            ?: "unavailable"
                    }"
        )

        return LinuxRuntimeBackendResult
            .Success(
                session =
                    session
            )
    }

    private fun handleFailedHandshake(
        process: LinuxProcessHandle,
        handshake: LinuxGuestHandshakeResult.Failure
    ): LinuxRuntimeBackendResult {

        lastError =
            handshake.message

        lastGuestOutput =
            handshake.output

        LinuxRuntimeActivityReporter
            .error(
                "Ubuntu guest handshake failed: ${handshake.message}"
            )

        Log.e(
            GUEST_TAG,
            "Ubuntu guest handshake FAILED: ${handshake.message}"
        )

        if (
            handshake
                .output
                .isNotBlank()
        ) {

            Log.e(
                GUEST_TAG,
                "Guest stdout: ${
                    handshake
                        .output
                        .trim()
                }"
            )
        }

        if (
            handshake
                .errorOutput
                .isNotBlank()
        ) {

            Log.e(
                GUEST_TAG,
                "Guest stderr: ${
                    handshake
                        .errorOutput
                        .trim()
                }"
            )
        }

        LinuxRuntimeActivityReporter
            .warning(
                "Cleaning up failed PRoot process."
            )

        cleanupFailedProcess(
            process =
                process
        )

        return LinuxRuntimeBackendResult
            .Failure(
                message =
                    handshake.message
            )
    }

    override fun stop():
            LinuxRuntimeBackendResult {

        Log.i(
            TAG,
            "Runtime backend stop requested."
        )

        val process =
            activeProcess

        /*
         * No native process is currently owned by Atlas.
         */
        if (
            process == null
        ) {

            activeSession =
                null

            lastError =
                null

            LinuxRuntimeActivityReporter
                .info(
                    "No active PRoot process exists."
                )

            return LinuxRuntimeBackendResult
                .Success()
        }

        return try {

            if (
                process.isAlive
            ) {

                LinuxRuntimeActivityReporter
                    .info(
                        "Requesting normal PRoot termination."
                    )

                process.stop()

                if (
                    process.isAlive
                ) {

                    LinuxRuntimeActivityReporter
                        .warning(
                            "PRoot remained alive; forcing termination."
                        )

                    process.forceStop()
                }

                if (
                    process.isAlive
                ) {

                    val message =
                        "PRoot remained alive after forced termination."

                    lastError =
                        message

                    LinuxRuntimeActivityReporter
                        .error(
                            message
                        )

                    return LinuxRuntimeBackendResult
                        .Failure(
                            message =
                                message
                        )
                }
            }

            activeProcess =
                null

            activeSession =
                null

            lastError =
                null

            LinuxRuntimeActivityReporter
                .success(
                    "Native PRoot process terminated."
                )

            Log.i(
                TAG,
                "PRoot runtime stopped."
            )

            LinuxRuntimeBackendResult
                .Success()

        } catch (
            exception: Exception
        ) {

            val message =
                exception.message
                    ?: "Failed to stop the PRoot runtime."

            lastError =
                message

            LinuxRuntimeActivityReporter
                .error(
                    message
                )

            Log.e(
                TAG,
                "PRoot stop failed: $message",
                exception
            )

            LinuxRuntimeBackendResult
                .Failure(
                    message =
                        message
                )
        }
    }

    /*
     * Synchronize Android DNS into Ubuntu.
     *
     * DNS failure remains non-fatal because Atlas may
     * intentionally be operating offline.
     */
    private fun synchronizeGuestDns(
        rootfsDirectory: File?
    ) {

        if (
            rootfsDirectory == null
        ) {

            LinuxRuntimeActivityReporter
                .warning(
                    "Ubuntu DNS synchronization skipped."
                )

            Log.w(
                DNS_TAG,
                "Ubuntu DNS synchronization skipped because the rootfs directory is unavailable."
            )

            return
        }

        try {

            when (
                val dnsResult =
                    LinuxGuestDnsManager
                        .synchronize(
                            rootfsDirectory
                        )
            ) {

                is LinuxGuestDnsSyncResult.Success -> {

                    LinuxRuntimeActivityReporter
                        .success(
                            "Ubuntu DNS configuration synchronized."
                        )

                    Log.i(
                        DNS_TAG,
                        "Ubuntu DNS synchronized: ${
                            dnsResult
                                .dnsServers
                                .joinToString(
                                    ", "
                                )
                        }"
                    )
                }

                is LinuxGuestDnsSyncResult.Skipped -> {

                    LinuxRuntimeActivityReporter
                        .warning(
                            "Ubuntu DNS synchronization skipped: ${dnsResult.message}"
                        )

                    Log.w(
                        DNS_TAG,
                        "Ubuntu DNS synchronization skipped: ${dnsResult.message}"
                    )
                }

                is LinuxGuestDnsSyncResult.Failure -> {

                    LinuxRuntimeActivityReporter
                        .warning(
                            "Ubuntu DNS synchronization failed; continuing startup."
                        )

                    Log.w(
                        DNS_TAG,
                        "Ubuntu DNS synchronization failed: ${dnsResult.message}",
                        dnsResult.cause
                    )
                }
            }

        } catch (
            exception: Exception
        ) {

            LinuxRuntimeActivityReporter
                .warning(
                    "Ubuntu DNS synchronization encountered an error; continuing startup."
                )

            Log.w(
                DNS_TAG,
                "Ubuntu DNS synchronization threw an exception; continuing runtime startup.",
                exception
            )
        }
    }

    /*
     * Clean up a native process after failed startup or
     * failed Ubuntu guest verification.
     */
    private fun cleanupFailedProcess(
        process: LinuxProcessHandle
    ) {

        runCatching {

            if (
                process.isAlive
            ) {

                process.forceStop()
            }

        }.onFailure { exception ->

            Log.w(
                TAG,
                "Unable to clean up failed PRoot process.",
                exception
            )
        }

        activeProcess =
            null

        activeSession =
            null
    }

    /*
     * Returns true only when Atlas currently owns a living
     * native PRoot process.
     */
    fun isProcessAlive():
            Boolean {

        val alive =
            activeProcess
                ?.isAlive
                ?: false

        if (
            !alive
        ) {

            activeProcess =
                null

            activeSession =
                null
        }

        return alive
    }

    /*
     * Expose the active process to the persistent Ubuntu
     * command bridge.
     */
    fun getProcess():
            LinuxProcessHandle? {

        val process =
            activeProcess
                ?: return null

        return if (
            process.isAlive
        ) {

            process

        } else {

            Log.i(
                TAG,
                "Detected exited PRoot process."
            )

            activeProcess =
                null

            activeSession =
                null

            null
        }
    }
}