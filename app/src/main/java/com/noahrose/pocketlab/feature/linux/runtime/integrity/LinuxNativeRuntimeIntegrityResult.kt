package com.noahrose.pocketlab.feature.linux.runtime.integrity

data class LinuxNativeRuntimeIntegrityResult(
    val status: LinuxNativeRuntimeIntegrityStatus,
    val expectedSha256: String? = null,
    val actualSha256: String? = null,
    val message: String? = null
)