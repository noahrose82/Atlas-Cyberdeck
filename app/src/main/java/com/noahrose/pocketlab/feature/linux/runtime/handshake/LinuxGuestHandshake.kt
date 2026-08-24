package com.noahrose.pocketlab.feature.linux.runtime.handshake

import com.noahrose.pocketlab.feature.linux.runtime.process.LinuxProcessHandle
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.nio.charset.StandardCharsets

object LinuxGuestHandshake {

    private const val BEGIN_MARKER =
        "__ATLAS_UBUNTU_BEGIN__"

    private const val END_MARKER =
        "__ATLAS_UBUNTU_READY__"

    private const val TIMEOUT_MILLIS =
        5_000L

    private const val POLL_INTERVAL_MILLIS =
        25L

    private const val MAX_CAPTURE_BYTES =
        64 * 1024

    fun execute(
        process: LinuxProcessHandle
    ): LinuxGuestHandshakeResult {

        if (!process.isAlive) {

            return LinuxGuestHandshakeResult
                .Failure(
                    message =
                        "PRoot process is not alive."
                )
        }

        val command =
            buildString {

                append(
                    "printf '$BEGIN_MARKER\\n'\n"
                )

                append(
                    "id\n"
                )

                append(
                    "pwd\n"
                )

                append(
                    "uname -m\n"
                )

                append(
                    "if [ -r /etc/os-release ]; then\n"
                )

                append(
                    "  . /etc/os-release\n"
                )

                append(
                    "  printf 'PRETTY_NAME=%s\\n' \"${'$'}PRETTY_NAME\"\n"
                )

                append(
                    "fi\n"
                )

                append(
                    "printf '$END_MARKER\\n'\n"
                )
            }

        return try {

            /*
             * Send commands directly into the
             * Ubuntu shell running inside PRoot.
             */
            process
                .outputStream
                .write(
                    command.toByteArray(
                        StandardCharsets.UTF_8
                    )
                )

            process
                .outputStream
                .flush()

            val standardOutput =
                ByteArrayOutputStream()

            val errorOutput =
                ByteArrayOutputStream()

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
                        standardOutput
                )

                drainAvailable(
                    input =
                        process.errorStream,

                    output =
                        errorOutput
                )

                val currentOutput =
                    standardOutput
                        .toString(
                            StandardCharsets.UTF_8
                                .name()
                        )

                if (
                    currentOutput.contains(
                        END_MARKER
                    )
                ) {

                    completed =
                        true

                    break
                }

                if (!process.isAlive) {

                    /*
                     * Capture anything still waiting
                     * in the pipes before reporting
                     * that the runtime exited.
                     */
                    drainAvailable(
                        input =
                            process.inputStream,

                        output =
                            standardOutput
                    )

                    drainAvailable(
                        input =
                            process.errorStream,

                        output =
                            errorOutput
                    )

                    return LinuxGuestHandshakeResult
                        .Failure(
                            message =
                                "Ubuntu shell exited during guest handshake.",

                            output =
                                standardOutput
                                    .toString(
                                        StandardCharsets.UTF_8
                                            .name()
                                    ),

                            errorOutput =
                                errorOutput
                                    .toString(
                                        StandardCharsets.UTF_8
                                            .name()
                                    )
                        )
                }

                Thread.sleep(
                    POLL_INTERVAL_MILLIS
                )
            }

            /*
             * Capture any final bytes already
             * available after receiving the marker.
             */
            drainAvailable(
                input =
                    process.inputStream,

                output =
                    standardOutput
            )

            drainAvailable(
                input =
                    process.errorStream,

                output =
                    errorOutput
            )

            val rawOutput =
                standardOutput
                    .toString(
                        StandardCharsets.UTF_8
                            .name()
                    )

            val rawError =
                errorOutput
                    .toString(
                        StandardCharsets.UTF_8
                            .name()
                    )

            if (!completed) {

                return LinuxGuestHandshakeResult
                    .Failure(
                        message =
                            "Timed out waiting for Ubuntu guest handshake.",

                        output =
                            rawOutput,

                        errorOutput =
                            rawError
                    )
            }

            val guestOutput =
                rawOutput
                    .substringAfter(
                        BEGIN_MARKER
                    )
                    .substringBefore(
                        END_MARKER
                    )
                    .trim()

            val lines =
                guestOutput
                    .lines()
                    .map {
                        it.trim()
                    }
                    .filter {
                        it.isNotEmpty()
                    }

            val identityVerified =
                lines.any {
                    it.startsWith(
                        "uid=0"
                    )
                }

            val workingDirectoryVerified =
                lines.any {
                    it == "/root"
                }

            val architectureVerified =
                lines.any {
                    it == "aarch64"
                }

            val ubuntuVerified =
                lines.any {
                    it.startsWith(
                        "PRETTY_NAME="
                    ) &&
                            it.contains(
                                "Ubuntu",
                                ignoreCase = true
                            )
                }

            if (!identityVerified) {

                return LinuxGuestHandshakeResult
                    .Failure(
                        message =
                            "Ubuntu guest did not report root identity.",

                        output =
                            guestOutput,

                        errorOutput =
                            rawError
                    )
            }

            if (!workingDirectoryVerified) {

                return LinuxGuestHandshakeResult
                    .Failure(
                        message =
                            "Ubuntu guest working directory verification failed.",

                        output =
                            guestOutput,

                        errorOutput =
                            rawError
                    )
            }

            if (!architectureVerified) {

                return LinuxGuestHandshakeResult
                    .Failure(
                        message =
                            "Ubuntu guest architecture verification failed.",

                        output =
                            guestOutput,

                        errorOutput =
                            rawError
                    )
            }

            if (!ubuntuVerified) {

                return LinuxGuestHandshakeResult
                    .Failure(
                        message =
                            "Ubuntu guest operating system verification failed.",

                        output =
                            guestOutput,

                        errorOutput =
                            rawError
                    )
            }

            LinuxGuestHandshakeResult
                .Success(
                    output =
                        guestOutput,

                    errorOutput =
                        rawError
                )

        } catch (exception: Exception) {

            LinuxGuestHandshakeResult
                .Failure(
                    message =
                        exception.message
                            ?: "Ubuntu guest handshake failed."
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

            val count =
                input.read(
                    buffer,
                    0,
                    minOf(
                        buffer.size,
                        remaining,
                        input.available()
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