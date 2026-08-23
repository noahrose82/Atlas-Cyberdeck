package com.noahrose.pocketlab.feature.linux.runtime.provision

import com.noahrose.pocketlab.feature.linux.runtime.platform.LinuxRuntimeAbiDetector

object LinuxRuntimeBinarySelector {

    fun getPreferredBinary():
            LinuxRuntimeBinaryDescriptor? {

        val abi =
            LinuxRuntimeAbiDetector
                .getPreferredAbi()
                ?: return null

        return LinuxRuntimeBinaryRegistry
            .getForAbi(
                abi
            )
    }
}