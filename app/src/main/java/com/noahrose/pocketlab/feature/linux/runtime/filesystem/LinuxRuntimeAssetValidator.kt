package com.noahrose.pocketlab.feature.linux.runtime.filesystem

import com.noahrose.pocketlab.feature.linux.runtime.platform.LinuxNativeRuntimeResolver

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

        /*
         * PRoot is native executable code shipped
         * with the signed Atlas APK.
         *
         * It no longer lives in writable Linux
         * runtime storage.
         */
        if (
            !LinuxNativeRuntimeResolver
                .isProotAvailable()
        ) {

            return LinuxRuntimeAssetStatus
                .PROOT_MISSING
        }

        val paths =
            runCatching {

                LinuxRuntimePathManager
                    .getPaths()

            }.getOrElse {

                return LinuxRuntimeAssetStatus
                    .NOT_PREPARED
            }

        /*
         * /bin/sh is our basic rootfs readiness
         * indicator.
         */
        val shell =
            paths
                .rootfsDirectory
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