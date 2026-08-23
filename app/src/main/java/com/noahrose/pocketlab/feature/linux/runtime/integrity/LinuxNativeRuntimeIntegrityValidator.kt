package com.noahrose.pocketlab.feature.linux.runtime.integrity

import com.noahrose.pocketlab.feature.linux.runtime.platform.LinuxNativeRuntimeResolver
import com.noahrose.pocketlab.feature.linux.runtime.provision.LinuxRuntimeBinarySelector
import java.io.File
import java.security.MessageDigest

object LinuxNativeRuntimeIntegrityValidator {

    fun validate():
            LinuxNativeRuntimeIntegrityResult {

        val executable =
            LinuxNativeRuntimeResolver
                .getProotExecutable()

        if (
            executable == null ||
            !executable.exists() ||
            !executable.isFile
        ) {

            return LinuxNativeRuntimeIntegrityResult(
                status =
                    LinuxNativeRuntimeIntegrityStatus
                        .BINARY_MISSING,

                message =
                    "Atlas PRoot executable was not found."
            )
        }

        val descriptor =
            LinuxRuntimeBinarySelector
                .getPreferredBinary()

        val expectedSha256 =
            descriptor
                ?.sha256
                ?.trim()
                ?.lowercase()

        if (expectedSha256.isNullOrBlank()) {

            return LinuxNativeRuntimeIntegrityResult(
                status =
                    LinuxNativeRuntimeIntegrityStatus
                        .HASH_NOT_PINNED,

                message =
                    "No trusted SHA-256 is pinned for this runtime binary."
            )
        }

        return try {

            val actualSha256 =
                calculateSha256(
                    executable
                )

            if (
                actualSha256.equals(
                    expectedSha256,
                    ignoreCase = true
                )
            ) {

                LinuxNativeRuntimeIntegrityResult(
                    status =
                        LinuxNativeRuntimeIntegrityStatus
                            .VERIFIED,

                    expectedSha256 =
                        expectedSha256,

                    actualSha256 =
                        actualSha256
                )

            } else {

                LinuxNativeRuntimeIntegrityResult(
                    status =
                        LinuxNativeRuntimeIntegrityStatus
                            .MISMATCH,

                    expectedSha256 =
                        expectedSha256,

                    actualSha256 =
                        actualSha256,

                    message =
                        "Installed Atlas PRoot SHA-256 does not match the trusted runtime descriptor."
                )
            }

        } catch (exception: Exception) {

            LinuxNativeRuntimeIntegrityResult(
                status =
                    LinuxNativeRuntimeIntegrityStatus
                        .VALIDATION_FAILED,

                expectedSha256 =
                    expectedSha256,

                message =
                    exception.message
                        ?: "Unable to validate Atlas PRoot integrity."
            )
        }
    }

    private fun calculateSha256(
        file: File
    ): String {

        val digest =
            MessageDigest
                .getInstance(
                    "SHA-256"
                )

        file.inputStream()
            .buffered()
            .use { input ->

                val buffer =
                    ByteArray(
                        DEFAULT_BUFFER_SIZE
                    )

                while (true) {

                    val count =
                        input.read(
                            buffer
                        )

                    if (count <= 0) {
                        break
                    }

                    digest.update(
                        buffer,
                        0,
                        count
                    )
                }
            }

        return digest
            .digest()
            .joinToString(
                separator = ""
            ) { byte ->

                "%02x".format(
                    byte.toInt() and 0xff
                )
            }
    }
}