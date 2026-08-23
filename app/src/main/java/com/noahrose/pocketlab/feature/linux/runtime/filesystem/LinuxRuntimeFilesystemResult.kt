package com.noahrose.pocketlab.feature.linux.runtime.filesystem

sealed interface LinuxRuntimeFilesystemResult {

    data object Ready :
        LinuxRuntimeFilesystemResult

    data class Failure(
        val message: String,
        val cause: Throwable? = null
    ) : LinuxRuntimeFilesystemResult
}