package com.noahrose.pocketlab.feature.linux.rootfs.integrity

enum class LinuxRootfsIntegrityStatus(
    val label: String
) {

    VERIFIED(
        label = "VERIFIED"
    ),

    ARCHIVE_MISSING(
        label = "ARCHIVE MISSING"
    ),

    HASH_MISMATCH(
        label = "HASH MISMATCH"
    ),

    HASH_NOT_PINNED(
        label = "HASH NOT PINNED"
    ),

    VALIDATION_FAILED(
        label = "VALIDATION FAILED"
    )
}