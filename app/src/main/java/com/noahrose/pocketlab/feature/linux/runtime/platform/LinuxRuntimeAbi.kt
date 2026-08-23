package com.noahrose.pocketlab.feature.linux.runtime.platform

enum class LinuxRuntimeAbi(
    val androidName: String,
    val displayName: String
) {

    ARM64_V8A(
        androidName = "arm64-v8a",
        displayName = "ARM64"
    ),

    ARMEABI_V7A(
        androidName = "armeabi-v7a",
        displayName = "ARMv7"
    ),

    X86_64(
        androidName = "x86_64",
        displayName = "x86_64"
    ),

    X86(
        androidName = "x86",
        displayName = "x86"
    );

    companion object {

        fun fromAndroidName(
            name: String
        ): LinuxRuntimeAbi? {

            return entries
                .firstOrNull { abi ->

                    abi.androidName
                        .equals(
                            name,
                            ignoreCase = true
                        )
                }
        }
    }
}