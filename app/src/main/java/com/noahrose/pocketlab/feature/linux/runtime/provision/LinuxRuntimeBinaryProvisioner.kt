package com.noahrose.pocketlab.feature.linux.runtime.provision

import com.noahrose.pocketlab.feature.linux.runtime.filesystem.LinuxRuntimePathManager
import java.io.File
import java.io.InputStream
import java.security.MessageDigest

object LinuxRuntimeBinaryProvisioner {

    fun installProot(
        inputStream: InputStream,
        expectedSha256: String? = null
    ): LinuxRuntimeProvisionResult {

        val paths =
            runCatching {
                LinuxRuntimePathManager
                    .getPaths()
            }.getOrElse { exception ->

                return LinuxRuntimeProvisionResult
                    .Failure(
                        message =
                            exception.message
                                ?: "Linux runtime paths are unavailable.",
                        cause =
                            exception
                    )
            }

        val destination =
            paths.prootExecutable

        val temporaryFile =
            File(
                destination.parentFile,
                "${destination.name}.part"
            )

        return try {

            destination.parentFile
                ?.let { directory ->

                    if (
                        !directory.exists() &&
                        !directory.mkdirs()
                    ) {

                        return LinuxRuntimeProvisionResult
                            .Failure(
                                message =
                                    "Unable to create runtime binary directory."
                            )
                    }
                }

            if (temporaryFile.exists()) {
                temporaryFile.delete()
            }

            inputStream.use { input ->

                temporaryFile
                    .outputStream()
                    .buffered()
                    .use { output ->

                        input.copyTo(
                            output
                        )
                    }
            }

            if (
                !temporaryFile.exists() ||
                temporaryFile.length() == 0L
            ) {

                return LinuxRuntimeProvisionResult
                    .Failure(
                        message =
                            "PRoot provisioning produced an empty binary."
                    )
            }

            if (
                expectedSha256 != null
            ) {

                val actualSha256 =
                    calculateSha256(
                        temporaryFile
                    )

                if (
                    !actualSha256.equals(
                        expectedSha256,
                        ignoreCase = true
                    )
                ) {

                    temporaryFile.delete()

                    return LinuxRuntimeProvisionResult
                        .Failure(
                            message =
                                "PRoot checksum verification failed."
                        )
                }
            }

            if (destination.exists()) {

                if (!destination.delete()) {

                    temporaryFile.delete()

                    return LinuxRuntimeProvisionResult
                        .Failure(
                            message =
                                "Unable to replace the existing PRoot binary."
                        )
                }
            }

            if (
                !temporaryFile.renameTo(
                    destination
                )
            ) {

                temporaryFile.delete()

                return LinuxRuntimeProvisionResult
                    .Failure(
                        message =
                            "Unable to install the PRoot binary."
                    )
            }

            if (
                !destination.setExecutable(
                    true,
                    true
                )
            ) {

                destination.delete()

                return LinuxRuntimeProvisionResult
                    .Failure(
                        message =
                            "Unable to mark PRoot as executable."
                    )
            }

            LinuxRuntimeProvisionResult
                .Success

        } catch (exception: Exception) {

            runCatching {
                temporaryFile.delete()
            }

            LinuxRuntimeProvisionResult
                .Failure(
                    message =
                        "Failed to provision the PRoot binary.",
                    cause =
                        exception
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