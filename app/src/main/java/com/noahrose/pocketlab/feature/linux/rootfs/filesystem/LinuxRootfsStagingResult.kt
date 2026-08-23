package com.noahrose.pocketlab.feature.linux.rootfs.filesystem

sealed interface LinuxRootfsStagingResult {

    data object Ready :
        LinuxRootfsStagingResult

    data class Failure(
        val message: String,
        val cause: Throwable? = null
    ) : LinuxRootfsStagingResult
}