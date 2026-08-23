package com.noahrose.pocketlab.feature.linux.runtime.provision

import com.noahrose.pocketlab.feature.linux.runtime.platform.LinuxRuntimeAbi

data class LinuxRuntimeBinaryDescriptor(
    val abi: LinuxRuntimeAbi,
    val assetName: String,
    val executableName: String,
    val source: LinuxRuntimeSourceDescriptor,

    /*
     * SHA-256 of the final Atlas-built binary.
     *
     * This stays null until we produce the
     * architecture-specific executable.
     */
    val sha256: String? = null
)