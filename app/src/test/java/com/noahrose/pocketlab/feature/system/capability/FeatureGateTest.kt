package com.noahrose.pocketlab.feature.system.capability

import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class FeatureGateTest {

    private fun readyCapabilities() =
        DeviceCapabilities(

            architectureSupported = true,

            apiSupported = true,

            memoryReady = true,

            storageReady = true,

            terminalAvailable = true,

            linuxCompatible = true,

            overallReady = true
        )

    @Test
    fun terminal_isAvailableWhenTerminalCapabilityIsReady() {

        val result =
            FeatureGate.evaluate(
                feature =
                    AtlasFeature.TERMINAL,
                capabilities =
                    readyCapabilities()
            )

        assertTrue(
            result.available
        )

        assertNull(
            result.reason
        )
    }

    @Test
    fun linux_isBlockedWhenLinuxIsIncompatible() {

        val capabilities =
            readyCapabilities().copy(
                linuxCompatible = false,
                overallReady = false
            )

        val result =
            FeatureGate.evaluate(
                feature =
                    AtlasFeature.LINUX,
                capabilities =
                    capabilities
            )

        assertFalse(
            result.available
        )

        assertTrue(
            result.reason
                ?.contains(
                    "Linux"
                ) == true
        )
    }

    @Test
    fun filesystem_isBlockedWithoutStorage() {

        val capabilities =
            readyCapabilities().copy(
                storageReady = false,
                overallReady = false
            )

        val result =
            FeatureGate.evaluate(
                feature =
                    AtlasFeature.FILE_SYSTEM,
                capabilities =
                    capabilities
            )

        assertFalse(
            result.available
        )
    }

    @Test
    fun ssh_isBlockedWhenTerminalIsUnavailable() {

        val capabilities =
            readyCapabilities().copy(
                terminalAvailable = false,
                overallReady = false
            )

        val result =
            FeatureGate.evaluate(
                feature =
                    AtlasFeature.SSH,
                capabilities =
                    capabilities
            )

        assertFalse(
            result.available
        )
    }

    @Test
    fun evaluateAll_returnsEveryAtlasFeature() {

        val results =
            FeatureGate.evaluateAll(
                readyCapabilities()
            )

        assertTrue(
            results.size ==
                    AtlasFeature.entries.size
        )

        assertTrue(
            results.all { result ->
                result.available
            }
        )
    }
}