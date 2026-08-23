package com.noahrose.pocketlab.feature.system.capability

object DeviceCapabilityFormatter {

    fun format(
        capabilities: DeviceCapabilities
    ): List<String> {

        fun status(
            value: Boolean
        ): String {

            return if (value) {
                "READY"
            } else {
                "NOT READY"
            }
        }

        return listOf(

            "Atlas Device Compatibility",

            "",

            "Architecture : ${status(capabilities.architectureSupported)}",

            "Android API  : ${status(capabilities.apiSupported)}",

            "Memory       : ${status(capabilities.memoryReady)}",

            "Storage      : ${status(capabilities.storageReady)}",

            "Terminal     : ${status(capabilities.terminalAvailable)}",

            "Linux        : ${status(capabilities.linuxCompatible)}",

            "",

            "Overall      : ${
                if (capabilities.overallReady) {
                    "READY"
                } else {
                    "NOT READY"
                }
            }"
        )
    }
}