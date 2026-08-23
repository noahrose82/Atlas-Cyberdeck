package com.noahrose.pocketlab.feature.linux.rootfs.extraction

sealed interface LinuxRootfsExtractionState {

    data object Idle :
        LinuxRootfsExtractionState

    data object Extracting :
        LinuxRootfsExtractionState

    data class Ready(
        val entriesExtracted: Int,
        val bytesExtracted: Long
    ) : LinuxRootfsExtractionState

    data class Failed(
        val message: String
    ) : LinuxRootfsExtractionState
}