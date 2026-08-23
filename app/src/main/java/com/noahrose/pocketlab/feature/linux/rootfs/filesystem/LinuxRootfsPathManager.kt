package com.noahrose.pocketlab.feature.linux.rootfs.filesystem

import com.noahrose.pocketlab.feature.linux.rootfs.provision.LinuxRootfsSelector
import com.noahrose.pocketlab.feature.linux.runtime.filesystem.LinuxRuntimePathManager
import java.io.File

object LinuxRootfsPathManager {

    fun getPaths(): LinuxRootfsPaths? {

        val descriptor =
            LinuxRootfsSelector
                .getPreferredRootfs()
                ?: return null

        val runtimePaths =
            runCatching {
                LinuxRuntimePathManager
                    .getPaths()
            }.getOrNull()
                ?: return null

        val stagingDirectory =
            File(
                runtimePaths.baseDirectory,
                "staging"
            )

        val archiveFile =
            File(
                stagingDirectory,
                descriptor.archiveName
            )

        return LinuxRootfsPaths(
            stagingDirectory =
                stagingDirectory,

            archiveFile =
                archiveFile,

            rootfsDirectory =
                runtimePaths.rootfsDirectory
        )
    }
}