package com.noahrose.pocketlab.feature.linux.runtime.filesystem

import java.io.File

object LinuxRuntimeFilesystemManager {

    private var lastPreparationResult:
            LinuxRuntimeFilesystemResult? =
        null

    fun getLastPreparationResult():
            LinuxRuntimeFilesystemResult? =
        lastPreparationResult

    fun prepare(): LinuxRuntimeFilesystemResult {

        val paths =
            runCatching {

                LinuxRuntimePathManager
                    .getPaths()

            }.getOrElse { exception ->

                return finish(
                    LinuxRuntimeFilesystemResult
                        .Failure(
                            message =
                                exception.message
                                    ?: "Linux runtime paths are unavailable.",
                            cause =
                                exception
                        )
                )
            }

        val requiredDirectories =
            listOf(
                paths.baseDirectory,
                paths.binaryDirectory,
                paths.rootfsDirectory,
                paths.homeDirectory,
                paths.runtimeDirectory,
                paths.temporaryDirectory
            )

        requiredDirectories
            .forEach { directory ->

                val result =
                    ensureDirectory(
                        directory
                    )

                if (
                    result is
                            LinuxRuntimeFilesystemResult.Failure
                ) {

                    return finish(
                        result
                    )
                }
            }

        return finish(
            validateWritableDirectory(
                paths.runtimeDirectory
            )
        )
    }

    private fun finish(
        result: LinuxRuntimeFilesystemResult
    ): LinuxRuntimeFilesystemResult {

        lastPreparationResult =
            result

        return result
    }

    private fun ensureDirectory(
        directory: File
    ): LinuxRuntimeFilesystemResult {

        return try {

            if (directory.exists()) {

                if (!directory.isDirectory) {

                    return LinuxRuntimeFilesystemResult
                        .Failure(
                            message =
                                "Runtime path is not a directory: ${directory.absolutePath}"
                        )
                }

                return LinuxRuntimeFilesystemResult
                    .Ready
            }

            if (!directory.mkdirs()) {

                return LinuxRuntimeFilesystemResult
                    .Failure(
                        message =
                            "Unable to create runtime directory: ${directory.absolutePath}"
                    )
            }

            LinuxRuntimeFilesystemResult
                .Ready

        } catch (exception: Exception) {

            LinuxRuntimeFilesystemResult
                .Failure(
                    message =
                        "Failed to prepare runtime directory: ${directory.absolutePath}",
                    cause =
                        exception
                )
        }
    }

    private fun validateWritableDirectory(
        directory: File
    ): LinuxRuntimeFilesystemResult {

        val probeFile =
            File(
                directory,
                ".atlas-write-test"
            )

        return try {

            probeFile.writeText(
                "atlas"
            )

            if (!probeFile.exists()) {

                return LinuxRuntimeFilesystemResult
                    .Failure(
                        message =
                            "Runtime directory validation failed: ${directory.absolutePath}"
                    )
            }

            LinuxRuntimeFilesystemResult
                .Ready

        } catch (exception: Exception) {

            LinuxRuntimeFilesystemResult
                .Failure(
                    message =
                        "Runtime directory is not writable: ${directory.absolutePath}",
                    cause =
                        exception
                )

        } finally {

            runCatching {
                probeFile.delete()
            }
        }
    }
}