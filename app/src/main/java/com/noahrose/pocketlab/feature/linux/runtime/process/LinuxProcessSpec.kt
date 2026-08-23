package com.noahrose.pocketlab.feature.linux.runtime.process

import java.io.File

data class LinuxProcessSpec(
    val executable: File,
    val arguments: List<String> = emptyList(),
    val workingDirectory: File? = null,
    val environment: Map<String, String> = emptyMap()
)