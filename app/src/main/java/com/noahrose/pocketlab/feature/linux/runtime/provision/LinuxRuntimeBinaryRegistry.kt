package com.noahrose.pocketlab.feature.linux.runtime.provision

import com.noahrose.pocketlab.feature.linux.runtime.platform.LinuxRuntimeAbi

object LinuxRuntimeBinaryRegistry {

    private val binaries =
        mapOf(
            LinuxRuntimeAbi.ARM64_V8A to
                    LinuxRuntimeBinaryDescriptor(
                        abi =
                            LinuxRuntimeAbi.ARM64_V8A,
                        assetName =
                            "proot-arm64-v8a",
                        executableName =
                            "proot"
                    ),

            LinuxRuntimeAbi.ARMEABI_V7A to
                    LinuxRuntimeBinaryDescriptor(
                        abi =
                            LinuxRuntimeAbi.ARMEABI_V7A,
                        assetName =
                            "proot-armeabi-v7a",
                        executableName =
                            "proot"
                    ),

            LinuxRuntimeAbi.X86_64 to
                    LinuxRuntimeBinaryDescriptor(
                        abi =
                            LinuxRuntimeAbi.X86_64,
                        assetName =
                            "proot-x86_64",
                        executableName =
                            "proot"
                    ),

            LinuxRuntimeAbi.X86 to
                    LinuxRuntimeBinaryDescriptor(
                        abi =
                            LinuxRuntimeAbi.X86,
                        assetName =
                            "proot-x86",
                        executableName =
                            "proot"
                    )
        )

    fun getForAbi(
        abi: LinuxRuntimeAbi
    ): LinuxRuntimeBinaryDescriptor? {

        return binaries[
            abi
        ]
    }
}