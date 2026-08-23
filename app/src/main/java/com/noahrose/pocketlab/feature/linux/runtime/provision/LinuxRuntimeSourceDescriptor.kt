package com.noahrose.pocketlab.feature.linux.runtime.provision

data class LinuxRuntimeSourceDescriptor(
    val id: String,
    val projectName: String,
    val repository: String,
    val version: String,
    val sourceArchiveSha256: String,
    val license: String
)