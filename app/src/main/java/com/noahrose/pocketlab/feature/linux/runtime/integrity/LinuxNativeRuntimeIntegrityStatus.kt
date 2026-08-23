package com.noahrose.pocketlab.feature.linux.runtime.integrity

enum class LinuxNativeRuntimeIntegrityStatus(
    val label: String
) {

    VERIFIED(
        label = "VERIFIED"
    ),

    MISMATCH(
        label = "MISMATCH"
    ),

    BINARY_MISSING(
        label = "BINARY MISSING"
    ),

    HASH_NOT_PINNED(
        label = "HASH NOT PINNED"
    ),

    VALIDATION_FAILED(
        label = "VALIDATION FAILED"
    )
}