package com.noahrose.pocketlab.feature.system.capability

import org.junit.Assert.assertTrue
import org.junit.Test

class DeviceCapabilityFormatterTest {

    @Test
    fun format_readyDeviceShowsReadyStatuses() {

        val capabilities =
            DeviceCapabilities(

                architectureSupported = true,

                apiSupported = true,

                memoryReady = true,

                storageReady = true,

                terminalAvailable = true,

                linuxCompatible = true,

                overallReady = true
            )

        val output =
            DeviceCapabilityFormatter.format(
                capabilities
            )

        assertTrue(
            output.contains(
                "Architecture : READY"
            )
        )

        assertTrue(
            output.contains(
                "Linux        : READY"
            )
        )

        assertTrue(
            output.contains(
                "Overall      : READY"
            )
        )
    }

    @Test
    fun format_incompatibleDeviceShowsNotReady() {

        val capabilities =
            DeviceCapabilities(

                architectureSupported = false,

                apiSupported = true,

                memoryReady = true,

                storageReady = true,

                terminalAvailable = true,

                linuxCompatible = false,

                overallReady = false
            )

        val output =
            DeviceCapabilityFormatter.format(
                capabilities
            )

        assertTrue(
            output.contains(
                "Architecture : NOT READY"
            )
        )

        assertTrue(
            output.contains(
                "Linux        : NOT READY"
            )
        )

        assertTrue(
            output.contains(
                "Overall      : NOT READY"
            )
        )
    }
}