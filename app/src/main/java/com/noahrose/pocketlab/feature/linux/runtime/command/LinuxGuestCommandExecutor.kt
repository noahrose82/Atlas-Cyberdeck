package com.noahrose.pocketlab.feature.linux.runtime.command

import com.noahrose.pocketlab.feature.linux.runtime.ProotLinuxRuntimeBackend
import com.noahrose.pocketlab.feature.linux.runtime.safety.LinuxRuntimeCircuitBreaker
import com.noahrose.pocketlab.feature.linux.runtime.safety.LinuxRuntimeRecoveryPolicy
import com.noahrose.pocketlab.feature.linux.runtime.safety.LinuxRuntimeSafetyMode
import com.noahrose.pocketlab.feature.linux.runtime.safety.LinuxRuntimeSafetyReason
import java.io.InputStream
import java.nio.charset.StandardCharsets
import java.util.concurrent.atomic.AtomicLong

object LinuxGuestCommandExecutor {

    private const val DEFAULT_TIMEOUT_MILLIS =
        15_000L

    private const val PACKAGE_TIMEOUT_MILLIS =
        10 * 60 * 1000L

    private const val POLL_INTERVAL_MILLIS =
        20L

    private const val MAX_CAPTURE_CHARS =
        2 * 1024 * 1024

    private const val MARKER_WINDOW_CHARS =
        16 * 1024

    private val commandCounter =
        AtomicLong(
            0L
        )

    @Synchronized
    fun execute(
        command: String,
        onOutputLine: ((String) -> Unit)? = null,
        onErrorLine: ((String) -> Unit)? = null
    ): LinuxGuestCommandResult {

        if (
            command.isBlank()
        ) {

            return LinuxGuestCommandResult
                .Failure(
                    message =
                        "Linux command cannot be empty."
                )
        }

        val safetySnapshot =
            LinuxRuntimeCircuitBreaker
                .getSnapshot()

        if (
            safetySnapshot.mode ==
            LinuxRuntimeSafetyMode.SAFE_MODE
        ) {

            return LinuxGuestCommandResult
                .Failure(
                    message =
                        "Atlas Linux safe mode is active. " +
                                "Return to the Atlas shell and run 'safety status'."
                )
        }

        if (
            safetySnapshot.mode ==
            LinuxRuntimeSafetyMode.RECOVERY_ARMED &&
            !LinuxRuntimeRecoveryPolicy
                .isAllowedInRecovery(
                    command
                )
        ) {

            return LinuxGuestCommandResult
                .Failure(
                    message =
                        "Atlas recovery mode blocked this command. " +
                                "Only runtime/package recovery commands are allowed."
                )
        }

        val process =
            ProotLinuxRuntimeBackend
                .getProcess()
                ?: return LinuxGuestCommandResult
                    .Failure(
                        message =
                            "Ubuntu runtime is not running."
                    )

        if (
            !process.isAlive
        ) {

            return LinuxGuestCommandResult
                .Failure(
                    message =
                        "Ubuntu runtime process is not alive."
                )
        }

        val packagePreparation =
            LinuxPackageCommandPolicy
                .prepare(
                    command
                )

        val preparedCommand =
            when (
                packagePreparation
            ) {

                is LinuxPackageCommandPreparation.Ready -> {

                    packagePreparation.command
                }

                is LinuxPackageCommandPreparation.Blocked -> {

                    return LinuxGuestCommandResult
                        .Failure(
                            message =
                                packagePreparation.message
                        )
                }
            }

        val timeoutMillis =
            determineTimeout(
                command
            )

        val commandId =
            commandCounter
                .incrementAndGet()

        val beginMarker =
            "__ATLAS_CMD_${commandId}_BEGIN__"

        val statusMarker =
            "__ATLAS_CMD_${commandId}_STATUS__"

        val endMarker =
            "__ATLAS_CMD_${commandId}_END__"

        val payload =
            buildString {

                append(
                    "printf '$beginMarker\\n'\n"
                )

                append(
                    preparedCommand
                )

                append(
                    "\n"
                )

                append(
                    "__atlas_exit_code=${'$'}?\n"
                )

                append(
                    "printf '$statusMarker%s\\n' \"${'$'}__atlas_exit_code\"\n"
                )

                append(
                    "printf '$endMarker\\n'\n"
                )
            }

        return try {

            /*
             * Canonical output capture used for the
             * returned LinuxGuestCommandResult.
             */
            val stdoutCapture =
                StringBuilder()

            val stderrCapture =
                StringBuilder()

            /*
             * Line streamers are only responsible for
             * live terminal presentation.
             *
             * Command completion is intentionally NOT
             * dependent on the line streamer. Instead,
             * a rolling raw-text window searches for
             * Atlas' end marker directly.
             */
            val stdoutStreamer =
                GuestStdoutStreamer(
                    beginMarker =
                        beginMarker,

                    statusMarker =
                        statusMarker,

                    endMarker =
                        endMarker,

                    onLine =
                        onOutputLine
                )

            val stderrStreamer =
                GuestLineStreamer(
                    onLine =
                        onErrorLine
                )

            val markerWindow =
                StringBuilder()

            var completed =
                false

            process
                .outputStream
                .write(
                    payload.toByteArray(
                        StandardCharsets.UTF_8
                    )
                )

            process
                .outputStream
                .flush()

            val deadline =
                System.currentTimeMillis() +
                        timeoutMillis

            while (
                System.currentTimeMillis() <
                deadline
            ) {

                drainAvailable(
                    input =
                        process.inputStream
                ) { text ->

                    appendLimited(
                        target =
                            stdoutCapture,

                        text =
                            text
                    )

                    stdoutStreamer
                        .accept(
                            text
                        )

                    appendMarkerWindow(
                        markerWindow =
                            markerWindow,

                        text =
                            text
                    )

                    if (
                        markerWindow.contains(
                            endMarker
                        )
                    ) {

                        completed =
                            true
                    }
                }

                drainAvailable(
                    input =
                        process.errorStream
                ) { text ->

                    appendLimited(
                        target =
                            stderrCapture,

                        text =
                            text
                    )

                    stderrStreamer
                        .accept(
                            text
                        )
                }

                if (
                    completed
                ) {

                    break
                }

                if (
                    !process.isAlive
                ) {

                    drainAvailable(
                        input =
                            process.inputStream
                    ) { text ->

                        appendLimited(
                            target =
                                stdoutCapture,

                            text =
                                text
                        )

                        stdoutStreamer
                            .accept(
                                text
                            )
                    }

                    drainAvailable(
                        input =
                            process.errorStream
                    ) { text ->

                        appendLimited(
                            target =
                                stderrCapture,

                            text =
                                text
                        )

                        stderrStreamer
                            .accept(
                                text
                            )
                    }

                    stdoutStreamer
                        .finish()

                    stderrStreamer
                        .finish()

                    val capturedOutput =
                        extractCommandOutput(
                            rawOutput =
                                stdoutCapture.toString(),

                            beginMarker =
                                beginMarker,

                            statusMarker =
                                statusMarker
                        )

                    val capturedError =
                        stderrCapture
                            .toString()
                            .trim()

                    LinuxRuntimeCircuitBreaker
                        .trip(
                            reason =
                                LinuxRuntimeSafetyReason.RUNTIME_PROCESS_LOST,

                            message =
                                "Ubuntu runtime exited while Atlas was executing a guest command."
                        )

                    return LinuxGuestCommandResult
                        .Failure(
                            message =
                                "Atlas circuit breaker tripped because the Ubuntu runtime exited unexpectedly.",

                            output =
                                capturedOutput,

                            errorOutput =
                                capturedError
                        )
                }

                Thread.sleep(
                    POLL_INTERVAL_MILLIS
                )
            }

            /*
             * Final drain after completion/timeout.
             */
            drainAvailable(
                input =
                    process.inputStream
            ) { text ->

                appendLimited(
                    target =
                        stdoutCapture,

                    text =
                        text
                )

                stdoutStreamer
                    .accept(
                        text
                    )

                appendMarkerWindow(
                    markerWindow =
                        markerWindow,

                    text =
                        text
                )

                if (
                    markerWindow.contains(
                        endMarker
                    )
                ) {

                    completed =
                        true
                }
            }

            drainAvailable(
                input =
                    process.errorStream
            ) { text ->

                appendLimited(
                    target =
                        stderrCapture,

                    text =
                        text
                )

                stderrStreamer
                    .accept(
                        text
                    )
            }

            stdoutStreamer
                .finish()

            stderrStreamer
                .finish()

            val rawOutput =
                stdoutCapture
                    .toString()

            val rawError =
                stderrCapture
                    .toString()
                    .trim()

            if (
                !completed
            ) {

                return LinuxGuestCommandResult
                    .Failure(
                        message =
                            buildTimeoutMessage(
                                command =
                                    command,

                                timeoutMillis =
                                    timeoutMillis
                            ),

                        output =
                            extractCommandOutput(
                                rawOutput =
                                    rawOutput,

                                beginMarker =
                                    beginMarker,

                                statusMarker =
                                    statusMarker
                            ),

                        errorOutput =
                            rawError
                    )
            }

            val exitCode =
                extractExitCode(
                    rawOutput =
                        rawOutput,

                    statusMarker =
                        statusMarker
                )

            val commandOutput =
                extractCommandOutput(
                    rawOutput =
                        rawOutput,

                    beginMarker =
                        beginMarker,

                    statusMarker =
                        statusMarker
                )

            val packageIntegrityFailure =
                LinuxRuntimeRecoveryPolicy
                    .detectPackageIntegrityFailure(
                        rawError
                    )

            if (
                packageIntegrityFailure != null
            ) {

                LinuxRuntimeCircuitBreaker
                    .trip(
                        reason =
                            LinuxRuntimeSafetyReason.PACKAGE_STATE_FAILURE,

                        message =
                            packageIntegrityFailure
                    )

                return LinuxGuestCommandResult
                    .Failure(
                        message =
                            "Atlas circuit breaker tripped because package integrity verification failed.",

                        output =
                            commandOutput,

                        errorOutput =
                            rawError
                    )
            }

            val recoveryArmed =
                LinuxRuntimeCircuitBreaker
                    .isRecoveryArmed()

            if (
                recoveryArmed &&
                LinuxRuntimeRecoveryPolicy
                    .isRepairOperation(
                        command
                    ) &&
                exitCode != 0
            ) {

                onErrorLine
                    ?.invoke(
                        "Atlas safety: recovery command failed " +
                                "(exit $exitCode); recovery mode remains armed."
                    )
            }

            if (
                recoveryArmed &&
                LinuxRuntimeRecoveryPolicy
                    .isAuditOnly(
                        command
                    ) &&
                exitCode == 0
            ) {

                onErrorLine
                    ?.invoke(
                        "Atlas safety: audit complete; recovery mode remains armed " +
                                "until a repair command succeeds."
                    )
            }

            if (
                recoveryArmed &&
                LinuxRuntimeRecoveryPolicy
                    .recoveryVerified(
                        command =
                            command,

                        output =
                            commandOutput,

                        errorOutput =
                            rawError,

                        exitCode =
                            exitCode
                    )
            ) {

                LinuxRuntimeCircuitBreaker
                    .resetAfterVerifiedRecovery()

                onErrorLine
                    ?.invoke(
                        "Atlas safety: recovery verified; safe mode cleared."
                    )
            }

            LinuxGuestCommandResult
                .Success(
                    output =
                        commandOutput,

                    errorOutput =
                        rawError,

                    exitCode =
                        exitCode
                )

        } catch (
            exception: InterruptedException
        ) {

            Thread
                .currentThread()
                .interrupt()

            LinuxGuestCommandResult
                .Failure(
                    message =
                        "Ubuntu command execution was interrupted."
                )

        } catch (
            exception: Exception
        ) {

            LinuxGuestCommandResult
                .Failure(
                    message =
                        exception.message
                            ?: "Unable to execute Ubuntu command."
                )
        }
    }

    private fun determineTimeout(
        command: String
    ): Long {

        return if (
            isPackageManagementCommand(
                command
            )
        ) {

            PACKAGE_TIMEOUT_MILLIS

        } else {

            DEFAULT_TIMEOUT_MILLIS
        }
    }

    private fun isPackageManagementCommand(
        command: String
    ): Boolean {

        return LinuxPackageCommandPolicy
            .isPackageManagementCommand(
                command
            )
    }

    private fun buildTimeoutMessage(
        command: String,
        timeoutMillis: Long
    ): String {

        val seconds =
            timeoutMillis /
                    1000L

        return if (
            isPackageManagementCommand(
                command
            )
        ) {

            "Ubuntu package command timed out after $seconds seconds."

        } else {

            "Linux command timed out after $seconds seconds."
        }
    }

    private fun extractCommandOutput(
        rawOutput: String,
        beginMarker: String,
        statusMarker: String
    ): String {

        if (
            !rawOutput.contains(
                beginMarker
            )
        ) {

            return rawOutput.trim()
        }

        return rawOutput
            .substringAfter(
                beginMarker
            )
            .substringBefore(
                statusMarker
            )
            .trim()
    }

    private fun extractExitCode(
        rawOutput: String,
        statusMarker: String
    ): Int {

        val statusIndex =
            rawOutput
                .lastIndexOf(
                    statusMarker
                )

        if (
            statusIndex < 0
        ) {

            return -1
        }

        val afterMarker =
            rawOutput
                .substring(
                    statusIndex +
                            statusMarker.length
                )

        return afterMarker
            .lineSequence()
            .firstOrNull()
            ?.trim()
            ?.toIntOrNull()
            ?: -1
    }

    private fun drainAvailable(
        input: InputStream,
        onText: (String) -> Unit
    ) {

        val buffer =
            ByteArray(
                4096
            )

        while (
            input.available() >
            0
        ) {

            val available =
                input.available()

            if (
                available <= 0
            ) {

                break
            }

            val count =
                input.read(
                    buffer,
                    0,
                    minOf(
                        buffer.size,
                        available
                    )
                )

            if (
                count <= 0
            ) {

                break
            }

            onText(
                String(
                    buffer,
                    0,
                    count,
                    StandardCharsets.UTF_8
                )
            )
        }
    }

    private fun appendLimited(
        target: StringBuilder,
        text: String
    ) {

        if (
            target.length >=
            MAX_CAPTURE_CHARS
        ) {

            return
        }

        val remaining =
            MAX_CAPTURE_CHARS -
                    target.length

        if (
            text.length <=
            remaining
        ) {

            target.append(
                text
            )

        } else {

            target.append(
                text.take(
                    remaining
                )
            )
        }
    }

    /*
     * Keep only a small rolling raw-text window for
     * reliable end-marker detection.
     *
     * This remains independent of the capture limit,
     * so verbose commands are still allowed to finish
     * after Atlas stops retaining their full output.
     */
    private fun appendMarkerWindow(
        markerWindow: StringBuilder,
        text: String
    ) {

        markerWindow
            .append(
                text
            )

        if (
            markerWindow.length >
            MARKER_WINDOW_CHARS
        ) {

            markerWindow.delete(
                0,
                markerWindow.length -
                        MARKER_WINDOW_CHARS
            )
        }
    }

    /*
     * Live stdout presentation.
     *
     * Atlas' begin/status/end protocol remains hidden
     * from the user.
     */
    private class GuestStdoutStreamer(
        private val beginMarker: String,
        private val statusMarker: String,
        private val endMarker: String,
        private val onLine: ((String) -> Unit)?
    ) {

        private enum class State {
            WAITING_FOR_BEGIN,
            CAPTURING,
            WAITING_FOR_END,
            FINISHED
        }

        private var state =
            State.WAITING_FOR_BEGIN

        private val pending =
            StringBuilder()

        private var previousWasCarriageReturn =
            false

        fun accept(
            text: String
        ) {

            text.forEach { character ->

                when (
                    character
                ) {

                    '\r' -> {

                        flushLine()

                        previousWasCarriageReturn =
                            true
                    }

                    '\n' -> {

                        if (
                            previousWasCarriageReturn
                        ) {

                            previousWasCarriageReturn =
                                false

                        } else {

                            flushLine()
                        }
                    }

                    else -> {

                        previousWasCarriageReturn =
                            false

                        pending.append(
                            character
                        )
                    }
                }
            }
        }

        fun finish() {

            if (
                pending.isNotEmpty()
            ) {

                flushLine()
            }
        }

        private fun flushLine() {

            val line =
                pending
                    .toString()

            pending
                .setLength(
                    0
                )

            when (
                state
            ) {

                State.WAITING_FOR_BEGIN -> {

                    if (
                        line.contains(
                            beginMarker
                        )
                    ) {

                        state =
                            State.CAPTURING
                    }
                }

                State.CAPTURING -> {

                    when {

                        line.contains(
                            statusMarker
                        ) -> {

                            state =
                                State.WAITING_FOR_END
                        }

                        line.contains(
                            endMarker
                        ) -> {

                            state =
                                State.FINISHED
                        }

                        else -> {

                            onLine
                                ?.invoke(
                                    line
                                )
                        }
                    }
                }

                State.WAITING_FOR_END -> {

                    if (
                        line.contains(
                            endMarker
                        )
                    ) {

                        state =
                            State.FINISHED
                    }
                }

                State.FINISHED -> {
                    // Ignore shell residue after command end.
                }
            }
        }
    }

    private class GuestLineStreamer(
        private val onLine: ((String) -> Unit)?
    ) {

        private val pending =
            StringBuilder()

        private var previousWasCarriageReturn =
            false

        fun accept(
            text: String
        ) {

            text.forEach { character ->

                when (
                    character
                ) {

                    '\r' -> {

                        flushLine()

                        previousWasCarriageReturn =
                            true
                    }

                    '\n' -> {

                        if (
                            previousWasCarriageReturn
                        ) {

                            previousWasCarriageReturn =
                                false

                        } else {

                            flushLine()
                        }
                    }

                    else -> {

                        previousWasCarriageReturn =
                            false

                        pending.append(
                            character
                        )
                    }
                }
            }
        }

        fun finish() {

            if (
                pending.isNotEmpty()
            ) {

                flushLine()
            }
        }

        private fun flushLine() {

            val line =
                pending
                    .toString()

            pending
                .setLength(
                    0
                )

            onLine
                ?.invoke(
                    line
                )
        }
    }
}
