package com.noahrose.pocketlab.feature.linux.rootfs.filesystem

import java.io.File

data class LinuxRootfsPaths(
    val stagingDirectory: File,
    val archiveFile: File,
    val rootfsDirectory: File
)