package com.noahrose.pocketlab.feature.linux.runtime

data class LinuxRuntimeSession(
    val processId: Long? = null,
    val startedAtEpochMillis: Long,
    val workingDirectory: String? = null
)