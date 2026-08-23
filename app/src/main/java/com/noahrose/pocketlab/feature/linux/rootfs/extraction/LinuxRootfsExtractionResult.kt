package com.noahrose.pocketlab.feature.linux.rootfs.extraction

sealed interface LinuxRootfsExtractionResult {

    data class Success(
        val entriesExtracted: Int,
        val bytesExtracted: Long
    ) : LinuxRootfsExtractionResult

    data class Failure(
        val message: String,
        val cause: Throwable? = null
    ) : LinuxRootfsExtractionResult
}