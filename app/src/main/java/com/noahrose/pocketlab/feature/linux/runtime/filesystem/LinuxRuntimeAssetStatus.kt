package com.noahrose.pocketlab.feature.linux.runtime.filesystem

enum class LinuxRuntimeAssetStatus(
    val label: String
) {
    READY(
        label = "READY"
    ),

    PROOT_MISSING(
        label = "PROOT MISSING"
    ),

    PROOT_NOT_EXECUTABLE(
        label = "PROOT NOT EXECUTABLE"
    ),

    ROOTFS_MISSING(
        label = "ROOTFS MISSING"
    ),

    NOT_PREPARED(
        label = "NOT PREPARED"
    )
}