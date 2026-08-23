package com.noahrose.pocketlab.feature.linux.rootfs.filesystem

object LinuxRootfsStagingManager {

    private var lastPreparationResult:
            LinuxRootfsStagingResult? =
        null

    fun getLastPreparationResult():
            LinuxRootfsStagingResult? =
        lastPreparationResult

    fun prepare(): LinuxRootfsStagingResult {

        val paths =
            LinuxRootfsPathManager
                .getPaths()

        if (paths == null) {

            return finish(
                LinuxRootfsStagingResult
                    .Failure(
                        message =
                            "Rootfs staging paths are unavailable."
                    )
            )
        }

        return try {

            val directory =
                paths.stagingDirectory

            if (directory.exists()) {

                if (!directory.isDirectory) {

                    return finish(
                        LinuxRootfsStagingResult
                            .Failure(
                                message =
                                    "Rootfs staging path is not a directory: ${directory.absolutePath}"
                            )
                    )
                }

            } else {

                if (!directory.mkdirs()) {

                    return finish(
                        LinuxRootfsStagingResult
                            .Failure(
                                message =
                                    "Unable to create rootfs staging directory."
                            )
                    )
                }
            }

            finish(
                LinuxRootfsStagingResult
                    .Ready
            )

        } catch (exception: Exception) {

            finish(
                LinuxRootfsStagingResult
                    .Failure(
                        message =
                            "Failed to prepare rootfs staging storage.",
                        cause =
                            exception
                    )
            )
        }
    }

    private fun finish(
        result: LinuxRootfsStagingResult
    ): LinuxRootfsStagingResult {

        lastPreparationResult =
            result

        return result
    }
}