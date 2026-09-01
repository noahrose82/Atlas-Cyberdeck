package com.noahrose.pocketlab.feature.linux.runtime.process

import com.noahrose.pocketlab.feature.linux.runtime.ProotLinuxRuntimeBackend
import com.noahrose.pocketlab.feature.linux.runtime.command.LinuxGuestCommandExecutor
import com.noahrose.pocketlab.feature.linux.runtime.command.LinuxGuestCommandResult
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
     */
    @Synchronized
    fun start(
        command: String,
        columns: Int = 80,
        rows: Int = 24
    ): LinuxInteractiveSessionStartResult {

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
         * Require the persistent Ubuntu runtime.
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
     * LIVE PTY RESIZE
     * ------------------------------------------------
     *
     * The interactive PRoot process itself does not expose
     * its PTY descriptor to Android.
     *
     * LinuxInteractiveProotProcessSpecFactory therefore
     * publishes the PTY slave path into:
     *
     *     /tmp/.atlas-interactive-pty
     *
     * Example:
     *
     *     /dev/pts/3
     *
     * Atlas uses its persistent Ubuntu command process as
     * a control channel to update the kernel terminal
     * window size for that PTY.
     */
    @Synchronized
    fun resize(
        columns: Int,
        rows: Int
    ): LinuxInteractiveResizeResult {

        if (
            columns <= 0 ||
            rows <= 0
        ) {

            return LinuxInteractiveResizeResult
                .Failure(
                    message =
                        "Interactive terminal dimensions must be greater than zero."
                )
        }

        val process =
            activeProcess
                ?: return LinuxInteractiveResizeResult
                    .Failure(
                        message =
                            "No interactive Ubuntu session is active."
                    )

        if (
            !process.isAlive
        ) {

            activeProcess =
                null

            return LinuxInteractiveResizeResult
                .Failure(
                    message =
                        "The interactive Ubuntu process is no longer running."
                )
        }

        /*
         * Resize remains fail-closed under Atlas safety.
         */
        val safetySnapshot =
            LinuxRuntimeCircuitBreaker
                .getSnapshot()

        if (
            safetySnapshot.mode !=
            LinuxRuntimeSafetyMode.NORMAL
        ) {

            return LinuxInteractiveResizeResult
                .Failure(
                    message =
                        "Interactive terminal resize is unavailable " +
                                "while Atlas runtime safety is not NORMAL."
                )
        }

        val controlFile =
            LinuxInteractiveProotProcessSpecFactory
                .PTY_CONTROL_FILE

        /*
         * ------------------------------------------------
         * PTY VALIDATION + RESIZE COMMAND
         * ------------------------------------------------
         *
         * Do not blindly trust the transient control file.
         *
         * We require:
         *
         *     /dev/pts/<numeric-id>
         *
         * before passing the path to stty.
         *
         * rows and columns are Kotlin Int values that have
         * already been range-checked above.
         */
        val resizeCommand =
            buildString {

                append(
                    "ATLAS_PTY=\$(cat "
                )

                append(
                    controlFile
                )

                append(
                    " 2>/dev/null) || exit 70; "
                )

                append(
                    "case \"\$ATLAS_PTY\" in "
                )

                append(
                    "/dev/pts/*) "
                )

                append(
                    "ATLAS_PTY_NUMBER=\"\${ATLAS_PTY#/dev/pts/}\" "
                )

                append(
                    ";; "
                )

                append(
                    "*) exit 71 ;; "
                )

                append(
                    "esac; "
                )

                append(
                    "case \"\$ATLAS_PTY_NUMBER\" in "
                )

                append(
                    "''|*[!0-9]*) exit 71 ;; "
                )

                append(
                    "esac; "
                )

                /*
                 * Require the PTY device to still exist.
                 */
                append(
                    "[ -c \"\$ATLAS_PTY\" ] || exit 72; "
                )

                /*
                 * Apply the kernel terminal window size.
                 */
                append(
                    "stty -F \"\$ATLAS_PTY\" rows "
                )

                append(
                    rows
                )

                append(
                    " cols "
                )

                append(
                    columns
                )

                append(
                    " >/dev/null 2>&1 || exit 73; "
                )

                /*
                 * Read the value back so Atlas verifies
                 * that the kernel accepted it.
                 */
                append(
                    "ATLAS_SIZE=\$(stty -F \"\$ATLAS_PTY\" size 2>/dev/null) || exit 74; "
                )

                append(
                    "[ \"\$ATLAS_SIZE\" = \""
                )

                append(
                    rows
                )

                append(
                    " "
                )

                append(
                    columns
                )

                append(
                    "\" ] || exit 75"
                )
            }

        val result =
            LinuxGuestCommandExecutor
                .execute(
                    command =
                        resizeCommand
                )

        return when (
            result
        ) {

            is LinuxGuestCommandResult.Success -> {

                if (
                    result.exitCode ==
                    0
                ) {

                    LinuxInteractiveResizeResult
                        .Success(
                            columns =
                                columns,

                            rows =
                                rows
                        )

                } else {

                    LinuxInteractiveResizeResult
                        .Failure(
                            message =
                                "Ubuntu rejected the interactive terminal resize " +
                                        "(exit ${result.exitCode})."
                        )
                }
            }

            is LinuxGuestCommandResult.Failure -> {

                LinuxInteractiveResizeResult
                    .Failure(
                        message =
                            result.message
                    )
            }
        }
    }

    /*
     * ------------------------------------------------
     * RAW INPUT
     * ------------------------------------------------
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
    fun refresh():
            Boolean {

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
    fun stop():
            Boolean {

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

/*
 * ------------------------------------------------
 * LIVE RESIZE RESULT
 * ------------------------------------------------
 */
sealed interface LinuxInteractiveResizeResult {

    data class Success(
        val columns: Int,
        val rows: Int
    ) : LinuxInteractiveResizeResult

    data class Failure(
        val message: String
    ) : LinuxInteractiveResizeResult
}