package com.noahrose.pocketlab.feature.linux.rootfs.provision

import com.noahrose.pocketlab.feature.linux.runtime.platform.LinuxRuntimeAbiDetector

object LinuxRootfsSelector {

    fun getPreferredRootfs():
            LinuxRootfsDescriptor? {

        val abi =
            LinuxRuntimeAbiDetector
                .getPreferredAbi()
                ?: return null

        return LinuxRootfsRegistry
            .getForAbi(
                abi
            )
    }
}