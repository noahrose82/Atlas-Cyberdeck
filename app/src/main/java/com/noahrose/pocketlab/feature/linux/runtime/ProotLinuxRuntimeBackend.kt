package com.noahrose.pocketlab.feature.linux.runtime

import android.util.Log
import com.noahrose.pocketlab.feature.linux.runtime.handshake.LinuxGuestHandshake
import com.noahrose.pocketlab.feature.linux.runtime.handshake.LinuxGuestHandshakeResult
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

    private val processLauncher =
        AndroidLinuxProcessLauncher()

    /*
     * The actual native PRoot process currently
     * owned by Atlas.
     */
    private var activeProcess:
            LinuxProcessHandle? =
        null

    /*
     * Logical Atlas runtime session associated
     * with the active PRoot process.
     */
    private var activeSession:
            LinuxRuntimeSession? =
        null

    /*
     * Last runtime startup/shutdown error.
     *
     * LinuxViewModel uses this to surface useful
     * feedback instead of silently returning
     * START_FAILED or STOP_FAILED.
     */
    private var lastError:
            String? =
        null

    /*
     * Last successfully captured or partially
     * captured Ubuntu guest handshake output.
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
            "Runtime start requested."
        )

        /*
         * Do not spawn duplicate PRoot runtimes.
         */
        val existingProcess =
            activeProcess

        if (
            existingProcess != null &&
            existingProcess.isAlive
        ) {

            Log.i(
                TAG,
                "Existing PRoot process is already alive."
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
         * Any process reaching this point is stale.
         */
        activeProcess =
            null

        activeSession =
            null

        lastError =
            null

        lastGuestOutput =
            null

        Log.i(
            TAG,
            "Building PRoot launch specification."
        )

        val specResult =
            LinuxProotProcessSpecFactory
                .create()

        val spec =
            when (specResult) {

                is LinuxProotProcessSpecResult.Ready -> {

                    specResult.spec
                }

                is LinuxProotProcessSpecResult.Failure -> {

                    lastError =
                        specResult.message

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
         * Launch diagnostics.
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
         * Verify that the external PRoot loader
         * supplied through PROOT_LOADER really
         * resolves to Android's executable native
         * library directory.
         */
        val loaderPath =
            spec.environment[
                "PROOT_LOADER"
            ]

        Log.i(
            TAG,
            "PROOT_LOADER: ${loaderPath ?: "NOT SET"}"
        )

        if (loaderPath != null) {

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
                            if (loaderFile.exists()) {
                                loaderFile.length()
                            } else {
                                0L
                            }
                        }"
            )
        }

        Log.i(
            TAG,
            "Launching verified PRoot runtime."
        )

        return when (
            val launchResult =
                processLauncher
                    .launch(
                        spec
                    )
        ) {

            is LinuxProcessLaunchResult.Success -> {

                val process =
                    launchResult.process

                Log.i(
                    TAG,
                    "Android created PRoot process."
                )

                /*
                 * ProcessBuilder.start() only proves
                 * that Android created the process.
                 *
                 * It does not yet prove Ubuntu is
                 * actually executing.
                 */
                if (!process.isAlive) {

                    val message =
                        "PRoot process exited immediately after launch."

                    lastError =
                        message

                    Log.e(
                        TAG,
                        message
                    )

                    runCatching {

                        process.forceStop()

                    }.onFailure { exception ->

                        Log.w(
                            TAG,
                            "Cleanup after failed PRoot launch failed.",
                            exception
                        )
                    }

                    activeProcess =
                        null

                    activeSession =
                        null

                    return LinuxRuntimeBackendResult
                        .Failure(
                            message =
                                message
                        )
                }

                Log.i(
                    TAG,
                    "PRoot process is alive."
                )

                /*
                 * A living PRoot process is not
                 * sufficient evidence that Ubuntu
                 * itself is functional.
                 *
                 * Execute a real guest handshake
                 * through stdin/stdout.
                 */
                Log.i(
                    GUEST_TAG,
                    "Beginning Ubuntu guest handshake."
                )

                when (
                    val handshake =
                        LinuxGuestHandshake
                            .execute(
                                process
                            )
                ) {

                    is LinuxGuestHandshakeResult.Success -> {

                        lastGuestOutput =
                            handshake.output

                        handshake
                            .output
                            .lines()
                            .forEach { line ->

                                if (line.isNotBlank()) {

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

                        Log.i(
                            GUEST_TAG,
                            "Ubuntu guest handshake VERIFIED."
                        )

                        /*
                         * Ubuntu has now proven:
                         *
                         * - guest shell execution
                         * - uid=0 root identity
                         * - /root working directory
                         * - ARM64 architecture
                         * - Ubuntu OS identity
                         *
                         * Only now may Atlas report
                         * the runtime as successfully
                         * started.
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

                        Log.i(
                            TAG,
                            "Ubuntu runtime session started. " +
                                    "pid=${
                                        session.processId
                                            ?: "unavailable"
                                    }"
                        )

                        LinuxRuntimeBackendResult
                            .Success(
                                session =
                                    session
                            )
                    }

                    is LinuxGuestHandshakeResult.Failure -> {

                        lastError =
                            handshake.message

                        lastGuestOutput =
                            handshake.output

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

                        /*
                         * Never leave a failed guest
                         * runtime alive.
                         */
                        runCatching {

                            if (process.isAlive) {

                                process.forceStop()
                            }

                        }.onFailure { exception ->

                            Log.w(
                                TAG,
                                "Unable to clean up failed guest process.",
                                exception
                            )
                        }

                        activeProcess =
                            null

                        activeSession =
                            null

                        LinuxRuntimeBackendResult
                            .Failure(
                                message =
                                    handshake.message
                            )
                    }
                }
            }

            is LinuxProcessLaunchResult.Failure -> {

                activeProcess =
                    null

                activeSession =
                    null

                lastError =
                    launchResult.message

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

    override fun stop():
            LinuxRuntimeBackendResult {

        Log.i(
            TAG,
            "Runtime stop requested."
        )

        val process =
            activeProcess

        if (process == null) {

            activeSession =
                null

            lastError =
                null

            Log.i(
                TAG,
                "No active PRoot process exists."
            )

            return LinuxRuntimeBackendResult
                .Success()
        }

        return try {

            if (process.isAlive) {

                Log.i(
                    TAG,
                    "Requesting normal PRoot termination."
                )

                process.stop()

                /*
                 * If normal termination does not
                 * immediately stop the runtime,
                 * force termination.
                 */
                if (process.isAlive) {

                    Log.w(
                        TAG,
                        "PRoot remained alive; forcing termination."
                    )

                    process.forceStop()
                }
            }

            activeProcess =
                null

            activeSession =
                null

            lastError =
                null

            Log.i(
                TAG,
                "PRoot runtime stopped."
            )

            LinuxRuntimeBackendResult
                .Success()

        } catch (exception: Exception) {

            val message =
                exception.message
                    ?: "Failed to stop the PRoot runtime."

            lastError =
                message

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
     * Returns true only when Atlas currently owns
     * a living native PRoot process.
     *
     * Dead process/session references are cleaned
     * automatically.
     */
    fun isProcessAlive():
            Boolean {

        val alive =
            activeProcess
                ?.isAlive
                ?: false

        if (!alive) {

            activeProcess =
                null

            activeSession =
                null
        }

        return alive
    }

    /*
     * Exposes the live process handle for the
     * upcoming interactive Ubuntu command bridge.
     *
     * F3P-E will use this handle to communicate
     * with the already-running Ubuntu shell.
     */
    fun getProcess():
            LinuxProcessHandle? {

        val process =
            activeProcess
                ?: return null

        return if (process.isAlive) {

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