package com.noahrose.pocketlab.feature.linux.rootfs.integrity

data class LinuxRootfsIntegrityResult(
    val status: LinuxRootfsIntegrityStatus,
    val expectedSha256: String? = null,
    val actualSha256: String? = null,
    val message: String? = null
)