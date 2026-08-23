package com.noahrose.pocketlab.feature.linux.rootfs.provision

sealed interface LinuxRootfsDownloadState {

    data object Idle :
        LinuxRootfsDownloadState

    data object Downloading :
        LinuxRootfsDownloadState

    data class Ready(
        val archiveSizeBytes: Long,
        val downloadedNow: Boolean
    ) : LinuxRootfsDownloadState

    data class Failed(
        val message: String
    ) : LinuxRootfsDownloadState
}