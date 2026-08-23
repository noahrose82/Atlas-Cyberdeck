package com.noahrose.pocketlab.feature.system.capability

import com.noahrose.pocketlab.feature.system.DeviceProfile
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DeviceCapabilityAnalyzerTest {

    private fun createProfile(
        architecture: String = "arm64-v8a",
        apiLevel: Int = 36,
        totalMemoryMb: Long = 8192,
        availableStorageMb: Long = 64000
    ): DeviceProfile {

        return DeviceProfile(

            manufacturer = "Test",

            model = "Atlas Device",

            androidVersion = "16",

            apiLevel = apiLevel,

            architecture = architecture,

            availableProcessors = 8,

            totalMemoryMb = totalMemoryMb,

            availableMemoryMb = 4096,

            totalStorageMb = 128000,

            availableStorageMb =
                availableStorageMb,

            atlasName =
                "Atlas Cyberdeck",

            atlasVersion =
                "0.13.0-alpha",

            atlasBuild =
                "50",

            atlasCodename =
                "Foundation"
        )
    }

    @Test
    fun analyze_supportedDeviceIsReady() {

        val result =
            DeviceCapabilityAnalyzer.analyze(
                createProfile()
            )

        assertTrue(
            result.architectureSupported
        )

        assertTrue(
            result.apiSupported
        )

        assertTrue(
            result.memoryReady
        )

        assertTrue(
            result.storageReady
        )

        assertTrue(
            result.terminalAvailable
        )

        assertTrue(
            result.linuxCompatible
        )

        assertTrue(
            result.overallReady
        )
    }

    @Test
    fun analyze_rejectsUnsupportedArchitecture() {

        val result =
            DeviceCapabilityAnalyzer.analyze(
                createProfile(
                    architecture = "unknown"
                )
            )

        assertFalse(
            result.architectureSupported
        )

        assertFalse(
            result.linuxCompatible
        )

        assertFalse(
            result.overallReady
        )
    }

    @Test
    fun analyze_rejectsOldApiLevel() {

        val result =
            DeviceCapabilityAnalyzer.analyze(
                createProfile(
                    apiLevel = 28
                )
            )

        assertFalse(
            result.apiSupported
        )

        assertFalse(
            result.terminalAvailable
        )

        assertFalse(
            result.linuxCompatible
        )

        assertFalse(
            result.overallReady
        )
    }

    @Test
    fun analyze_detectsInsufficientMemory() {

        val result =
            DeviceCapabilityAnalyzer.analyze(
                createProfile(
                    totalMemoryMb = 1024
                )
            )

        assertFalse(
            result.memoryReady
        )

        assertFalse(
            result.linuxCompatible
        )

        assertFalse(
            result.overallReady
        )
    }

    @Test
    fun analyze_detectsInsufficientStorage() {

        val result =
            DeviceCapabilityAnalyzer.analyze(
                createProfile(
                    availableStorageMb = 512
                )
            )

        assertFalse(
            result.storageReady
        )

        assertFalse(
            result.linuxCompatible
        )

        assertFalse(
            result.overallReady
        )
    }
}