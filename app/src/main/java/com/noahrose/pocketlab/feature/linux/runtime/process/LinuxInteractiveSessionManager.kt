package com.noahrose.pocketlab.feature.linux.runtime.process

import com.noahrose.pocketlab.feature.linux.runtime.ProotLinuxRuntimeBackend
import com.noahrose.pocketlab.feature.linux.runtime.safety.LinuxRuntimeCircuitBreaker
import com.noahrose.pocketlab.feature.linux.runtime.safety.LinuxRuntimeSafetyMode
import java.io.InputStream
import java.nio.charset.StandardCharsets

object LinuxInteractiveSessionManager {

    private val processLauncher =
        AndroidLinuxProcessLauncher()

    private var activeProcess:
            LinuxProcessHandle? =
        null

    val isActive: Boolean
        @Synchronized
        get() =
            activeProcess
                ?.isAlive ==
                    true

    /*
     * ------------------------------------------------
     * START INTERACTIVE SESSION
     * ------------------------------------------------
     *
     * Interactive applications run in their own PRoot
     * process and never replace Atlas' persistent Ubuntu
     * command process.
     */
    @Synchronized
    fun start(
        command: String,
        columns: Int = 80,
        rows: Int = 24
    ): LinuxInteractiveSessionStartResult {

        /*
         * Clean up a stale reference from a previously
         * completed interactive process.
         */
        val existingProcess =
            activeProcess

        if (
            existingProcess != null
        ) {

            if (
                existingProcess.isAlive
            ) {

                return LinuxInteractiveSessionStartResult
                    .Failure(
                        message =
                            "An interactive Ubuntu session is already running."
                    )
            }

            activeProcess =
                null
        }

        /*
         * Interactive Linux access is available only
         * while Atlas safety is fully NORMAL.
         *
         * SAFE_MODE and RECOVERY_ARMED must never be
         * bypassed by the PTY path.
         */
        val safetySnapshot =
            LinuxRuntimeCircuitBreaker
                .getSnapshot()

        if (
            safetySnapshot.mode !=
            LinuxRuntimeSafetyMode.NORMAL
        ) {

            return LinuxInteractiveSessionStartResult
                .Failure(
                    message =
                        "Interactive Ubuntu applications are unavailable " +
                                "while Atlas runtime safety is not NORMAL."
                )
        }

        /*
         * Require the normal Ubuntu runtime to already be
         * alive.
         *
         * The interactive process is a companion session,
         * not an independent way to bypass `linux start`.
         */
        val runtimeProcess =
            ProotLinuxRuntimeBackend
                .getProcess()

        if (
            runtimeProcess == null ||
            !runtimeProcess.isAlive
        ) {

            return LinuxInteractiveSessionStartResult
                .Failure(
                    message =
                        "Ubuntu runtime is not running. Run 'linux start' first."
                )
        }

        val specResult =
            LinuxInteractiveProotProcessSpecFactory
                .create(
                    command =
                        command,

                    columns =
                        columns,

                    rows =
                        rows
                )

        val spec =
            when (
                specResult
            ) {

                is LinuxProotProcessSpecResult.Ready -> {

                    specResult.spec
                }

                is LinuxProotProcessSpecResult.Failure -> {

                    return LinuxInteractiveSessionStartResult
                        .Failure(
                            message =
                                specResult.message
                        )
                }
            }

        return when (
            val launchResult =
                processLauncher
                    .launch(
                        spec
                    )
        ) {

            is LinuxProcessLaunchResult.Success -> {

                activeProcess =
                    launchResult.process

                LinuxInteractiveSessionStartResult
                    .Started
            }

            is LinuxProcessLaunchResult.Failure -> {

                activeProcess =
                    null

                LinuxInteractiveSessionStartResult
                    .Failure(
                        message =
                            launchResult.message,

                        cause =
                            launchResult.cause
                    )
            }
        }
    }

    /*
     * ------------------------------------------------
     * RAW INPUT
     * ------------------------------------------------
     *
     * Unlike LinuxGuestCommandExecutor, interactive input
     * is NOT command-delimited.
     *
     * Every byte is forwarded directly to `script`, which
     * relays it into the PTY owned by nano/vim/etc.
     */
    @Synchronized
    fun write(
        bytes: ByteArray
    ): Boolean {

        val process =
            activeProcess
                ?: return false

        if (
            !process.isAlive
        ) {

            activeProcess =
                null

            return false
        }

        return try {

            process
                .outputStream
                .write(
                    bytes
                )

            process
                .outputStream
                .flush()

            true

        } catch (
            _: Exception
        ) {

            false
        }
    }

    fun writeText(
        text: String
    ): Boolean {

        return write(
            text.toByteArray(
                StandardCharsets.UTF_8
            )
        )
    }

    /*
     * ------------------------------------------------
     * RAW OUTPUT
     * ------------------------------------------------
     *
     * The consumer must read these streams as raw bytes.
     *
     * Do NOT convert interactive output into line-based
     * terminal output. ANSI terminal applications may
     * redraw any location on the screen.
     */
    @Synchronized
    fun getInputStream():
            InputStream? {

        val process =
            activeProcess
                ?: return null

        if (
            !process.isAlive
        ) {

            activeProcess =
                null

            return null
        }

        return process
            .inputStream
    }

    @Synchronized
    fun getErrorStream():
            InputStream? {

        val process =
            activeProcess
                ?: return null

        if (
            !process.isAlive
        ) {

            activeProcess =
                null

            return null
        }

        return process
            .errorStream
    }

    /*
     * ------------------------------------------------
     * SESSION LIFECYCLE
     * ------------------------------------------------
     */
    @Synchronized
    fun refresh(): Boolean {

        val process =
            activeProcess
                ?: return false

        if (
            process.isAlive
        ) {

            return true
        }

        activeProcess =
            null

        return false
    }

    /*
     * Request graceful termination first.
     *
     * If the interactive process refuses to exit, use the
     * same bounded force-stop behavior already provided by
     * LinuxProcessHandle.
     */
    @Synchronized
    fun stop(): Boolean {

        val process =
            activeProcess
                ?: return true

        if (
            process.isAlive
        ) {

            process.stop()
        }

        if (
            process.isAlive
        ) {

            process.forceStop()
        }

        val stopped =
            !process.isAlive

        if (
            stopped
        ) {

            activeProcess =
                null
        }

        return stopped
    }
}

sealed interface LinuxInteractiveSessionStartResult {

    data object Started :
        LinuxInteractiveSessionStartResult

    data class Failure(
        val message: String,
        val cause: Throwable? = null
    ) : LinuxInteractiveSessionStartResult
}