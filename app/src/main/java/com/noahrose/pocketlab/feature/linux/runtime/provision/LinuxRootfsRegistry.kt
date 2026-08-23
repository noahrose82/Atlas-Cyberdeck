package com.noahrose.pocketlab.feature.linux.rootfs.provision

import com.noahrose.pocketlab.feature.linux.runtime.platform.LinuxRuntimeAbi

object LinuxRootfsRegistry {

    val UBUNTU_NOBLE_ARM64 =
        LinuxRootfsDescriptor(
            id =
                "ubuntu-noble-arm64",

            distribution =
                "Ubuntu",

            release =
                "24.04.4 LTS",

            codename =
                "Noble Numbat",

            abi =
                LinuxRuntimeAbi.ARM64_V8A,

            archiveName =
                "ubuntu-base-24.04.4-base-arm64.tar.gz",

            downloadUrl =
                "https://cdimage.ubuntu.com/ubuntu-base/releases/24.04/release/ubuntu-base-24.04.4-base-arm64.tar.gz",

            sha256 =
                "04207713ece899c3740823d33690441ad3a7f0ded1101aca744e2b0f37ac7ff2"
        )

    fun getForAbi(
        abi: LinuxRuntimeAbi
    ): LinuxRootfsDescriptor? {

        return when (abi) {

            LinuxRuntimeAbi.ARM64_V8A ->
                UBUNTU_NOBLE_ARM64

            else ->
                null
        }
    }
}