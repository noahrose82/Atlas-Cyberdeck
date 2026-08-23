package com.noahrose.pocketlab.feature.linux.runtime.filesystem

object LinuxRuntimeAssetValidator {

    fun getStatus(): LinuxRuntimeAssetStatus {

        val filesystemResult =
            LinuxRuntimeFilesystemManager
                .getLastPreparationResult()

        if (
            filesystemResult !is
                    LinuxRuntimeFilesystemResult.Ready
        ) {

            return LinuxRuntimeAssetStatus
                .NOT_PREPARED
        }

        val paths =
            runCatching {

                LinuxRuntimePathManager
                    .getPaths()

            }.getOrElse {

                return LinuxRuntimeAssetStatus
                    .NOT_PREPARED
            }

        if (
            !paths.prootExecutable.exists() ||
            !paths.prootExecutable.isFile
        ) {

            return LinuxRuntimeAssetStatus
                .PROOT_MISSING
        }

        if (
            !paths.prootExecutable.canExecute()
        ) {

            return LinuxRuntimeAssetStatus
                .PROOT_NOT_EXECUTABLE
        }

        val shell =
            paths.rootfsDirectory
                .resolve(
                    "bin/sh"
                )

        if (
            !shell.exists() ||
            !shell.isFile
        ) {

            return LinuxRuntimeAssetStatus
                .ROOTFS_MISSING
        }

        return LinuxRuntimeAssetStatus
            .READY
    }
}