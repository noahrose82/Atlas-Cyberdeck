package com.noahrose.pocketlab.feature.linux.rootfs.provision

import com.noahrose.pocketlab.feature.linux.runtime.platform.LinuxRuntimeAbi

data class LinuxRootfsDescriptor(
    val id: String,
    val distribution: String,
    val release: String,
    val codename: String,
    val abi: LinuxRuntimeAbi,
    val archiveName: String,
    val downloadUrl: String,
    val sha256: String
)