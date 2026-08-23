package com.noahrose.pocketlab.feature.linux.runtime.provision

import com.noahrose.pocketlab.feature.linux.runtime.platform.LinuxRuntimeAbi

object LinuxRuntimeBinaryRegistry {

    private val prootSource =
        LinuxRuntimeSourceRegistry
            .PROOT

    private val binaries =
        mapOf(

            LinuxRuntimeAbi.ARM64_V8A to
                    LinuxRuntimeBinaryDescriptor(
                        abi =
                            LinuxRuntimeAbi.ARM64_V8A,

                        assetName =
                            "proot-arm64-v8a",

                        executableName =
                            "libproot_atlas.so",

                        source =
                            prootSource,

                        sha256 =
                            "bf562a87debdf108c9e6373dc93c3e59bb08ec0efaf3da0313c7420631454c67"
                    ),

            LinuxRuntimeAbi.ARMEABI_V7A to
                    LinuxRuntimeBinaryDescriptor(
                        abi =
                            LinuxRuntimeAbi.ARMEABI_V7A,

                        assetName =
                            "proot-armeabi-v7a",

                        executableName =
                            "proot",

                        source =
                            prootSource
                    ),

            LinuxRuntimeAbi.X86_64 to
                    LinuxRuntimeBinaryDescriptor(
                        abi =
                            LinuxRuntimeAbi.X86_64,

                        assetName =
                            "proot-x86_64",

                        executableName =
                            "proot",

                        source =
                            prootSource
                    ),

            LinuxRuntimeAbi.X86 to
                    LinuxRuntimeBinaryDescriptor(
                        abi =
                            LinuxRuntimeAbi.X86,

                        assetName =
                            "proot-x86",

                        executableName =
                            "proot",

                        source =
                            prootSource
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