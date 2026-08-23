package com.noahrose.pocketlab.feature.linux.runtime.filesystem

import android.content.Context
import java.io.File

object LinuxRuntimePathManager {

    private var paths: LinuxRuntimePaths? =
        null

    fun initialize(
        context: Context
    ) {

        val appContext =
            context.applicationContext

        val baseDirectory =
            File(
                appContext.noBackupFilesDir,
                "linux"
            )

        val binaryDirectory =
            File(
                baseDirectory,
                "bin"
            )

        val rootfsDirectory =
            File(
                baseDirectory,
                "rootfs"
            )

        val homeDirectory =
            File(
                baseDirectory,
                "home"
            )

        val runtimeDirectory =
            File(
                baseDirectory,
                "runtime"
            )

        val temporaryDirectory =
            File(
                appContext.cacheDir,
                "linux/tmp"
            )

        val prootExecutable =
            File(
                binaryDirectory,
                "proot"
            )

        paths =
            LinuxRuntimePaths(
                baseDirectory =
                    baseDirectory,
                binaryDirectory =
                    binaryDirectory,
                rootfsDirectory =
                    rootfsDirectory,
                homeDirectory =
                    homeDirectory,
                runtimeDirectory =
                    runtimeDirectory,
                temporaryDirectory =
                    temporaryDirectory,
                prootExecutable =
                    prootExecutable
            )
    }

    fun getPaths(): LinuxRuntimePaths {

        return checkNotNull(
            paths
        ) {
            "LinuxRuntimePathManager has not been initialized."
        }
    }
}