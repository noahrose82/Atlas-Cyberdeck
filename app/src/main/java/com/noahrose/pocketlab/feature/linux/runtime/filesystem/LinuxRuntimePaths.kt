package com.noahrose.pocketlab.feature.linux.runtime.filesystem

import java.io.File

data class LinuxRuntimePaths(
    val baseDirectory: File,
    val binaryDirectory: File,
    val rootfsDirectory: File,
    val homeDirectory: File,
    val runtimeDirectory: File,
    val temporaryDirectory: File,
    val prootExecutable: File
)