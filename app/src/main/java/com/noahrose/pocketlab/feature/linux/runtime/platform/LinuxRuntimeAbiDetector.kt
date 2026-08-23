package com.noahrose.pocketlab.feature.linux.runtime.platform

import android.os.Build

object LinuxRuntimeAbiDetector {

    fun getDeviceAbis(): List<String> {

        return Build
            .SUPPORTED_ABIS
            .toList()
    }

    fun getPreferredAbi(): LinuxRuntimeAbi? {

        return Build
            .SUPPORTED_ABIS
            .firstNotNullOfOrNull { abiName ->

                LinuxRuntimeAbi
                    .fromAndroidName(
                        abiName
                    )
            }
    }

    fun isSupported(): Boolean {

        return getPreferredAbi() != null
    }
}