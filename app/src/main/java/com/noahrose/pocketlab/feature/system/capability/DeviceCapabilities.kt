package com.noahrose.pocketlab.feature.system.capability

data class DeviceCapabilities(

    val architectureSupported: Boolean,

    val apiSupported: Boolean,

    val memoryReady: Boolean,

    val storageReady: Boolean,

    val terminalAvailable: Boolean,

    val linuxCompatible: Boolean,

    val overallReady: Boolean
)