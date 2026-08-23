package com.noahrose.pocketlab.feature.linux.rootfs.provision

sealed interface LinuxRootfsDownloadResult {

    data class Success(
        val bytesDownloaded: Long
    ) : LinuxRootfsDownloadResult

    data class Failure(
        val message: String,
        val cause: Throwable? = null
    ) : LinuxRootfsDownloadResult
}