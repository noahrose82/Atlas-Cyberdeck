package com.noahrose.pocketlab.feature.linux.runtime.command

import com.noahrose.pocketlab.feature.linux.runtime.ProotLinuxRuntimeBackend
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.nio.charset.StandardCharsets
import java.util.concurrent.atomic.AtomicLong

object LinuxGuestCommandExecutor {

    private const val TIMEOUT_MILLIS =
        10_000L

    private const val POLL_INTERVAL_MILLIS =
        20L

    private const val MAX_CAPTURE_BYTES =
        256 * 1024

    private val commandCounter =
        AtomicLong(
            0L
        )

    @Synchronized
    fun execute(
        command: String
    ): LinuxGuestCommandResult {

        if (command.isBlank()) {

            return LinuxGuestCommandResult
                .Failure(
                    message =
                        "Linux command cannot be empty."
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

        if (!process.isAlive) {

            return LinuxGuestCommandResult
                .Failure(
                    message =
                        "Ubuntu runtime process is not alive."
                )
        }

        val commandId =
            commandCounter
                .incrementAndGet()

        val beginMarker =
            "__ATLAS_CMD_${commandId}_BEGIN__"

        val statusMarker =
            "__ATLAS_CMD_${commandId}_STATUS__"

        val endMarker =
            "__ATLAS_CMD_${commandId}_END__"

        /*
         * These commands execute in the persistent
         * Ubuntu shell.
         *
         * Because we do not spawn a subshell,
         * stateful commands such as:
         *
         * cd /etc
         *
         * can persist for later guest commands.
         */
        val payload =
            buildString {

                append(
                    "printf '$beginMarker\\n'\n"
                )

                append(
                    command
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

            val stdout =
                ByteArrayOutputStream()

            val stderr =
                ByteArrayOutputStream()

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
                        TIMEOUT_MILLIS

            var completed =
                false

            while (
                System.currentTimeMillis() <
                deadline
            ) {

                drainAvailable(
                    input =
                        process.inputStream,

                    output =
                        stdout
                )

                drainAvailable(
                    input =
                        process.errorStream,

                    output =
                        stderr
                )

                val currentOutput =
                    stdout.toString(
                        StandardCharsets.UTF_8
                            .name()
                    )

                if (
                    currentOutput.contains(
                        endMarker
                    )
                ) {

                    completed =
                        true

                    break
                }

                if (!process.isAlive) {

                    drainAvailable(
                        input =
                            process.inputStream,

                        output =
                            stdout
                    )

                    drainAvailable(
                        input =
                            process.errorStream,

                        output =
                            stderr
                    )

                    return LinuxGuestCommandResult
                        .Failure(
                            message =
                                "Ubuntu runtime exited while executing the command.",

                            output =
                                stdout.toString(
                                    StandardCharsets.UTF_8
                                        .name()
                                ),

                            errorOutput =
                                stderr.toString(
                                    StandardCharsets.UTF_8
                                        .name()
                                )
                        )
                }

                Thread.sleep(
                    POLL_INTERVAL_MILLIS
                )
            }

            drainAvailable(
                input =
                    process.inputStream,

                output =
                    stdout
            )

            drainAvailable(
                input =
                    process.errorStream,

                output =
                    stderr
            )

            val rawOutput =
                stdout.toString(
                    StandardCharsets.UTF_8
                        .name()
                )

            val rawError =
                stderr.toString(
                    StandardCharsets.UTF_8
                        .name()
                )

            if (!completed) {

                return LinuxGuestCommandResult
                    .Failure(
                        message =
                            "Linux command timed out.",

                        output =
                            rawOutput,

                        errorOutput =
                            rawError
                    )
            }

            val commandOutput =
                rawOutput
                    .substringAfter(
                        beginMarker
                    )
                    .substringBefore(
                        statusMarker
                    )
                    .trim()

            val statusSection =
                rawOutput
                    .substringAfter(
                        statusMarker,
                        ""
                    )

            val exitCode =
                statusSection
                    .lineSequence()
                    .firstOrNull()
                    ?.trim()
                    ?.toIntOrNull()
                    ?: -1

            LinuxGuestCommandResult
                .Success(
                    output =
                        commandOutput,

                    errorOutput =
                        rawError.trim(),

                    exitCode =
                        exitCode
                )

        } catch (exception: Exception) {

            LinuxGuestCommandResult
                .Failure(
                    message =
                        exception.message
                            ?: "Unable to execute Ubuntu command."
                )
        }
    }

    private fun drainAvailable(
        input: InputStream,
        output: ByteArrayOutputStream
    ) {

        val buffer =
            ByteArray(
                4096
            )

        while (
            input.available() > 0 &&
            output.size() <
            MAX_CAPTURE_BYTES
        ) {

            val remaining =
                MAX_CAPTURE_BYTES -
                        output.size()

            val available =
                input.available()

            if (
                remaining <= 0 ||
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
                        remaining,
                        available
                    )
                )

            if (count <= 0) {
                break
            }

            output.write(
                buffer,
                0,
                count
            )
        }
    }
}