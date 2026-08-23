package com.noahrose.pocketlab.feature.linux.runtime.provision

import com.noahrose.pocketlab.feature.linux.runtime.platform.LinuxRuntimeAbi

data class LinuxRuntimeBinaryDescriptor(
    val abi: LinuxRuntimeAbi,
    val assetName: String,
    val executableName: String,
    val sha256: String? = null
)