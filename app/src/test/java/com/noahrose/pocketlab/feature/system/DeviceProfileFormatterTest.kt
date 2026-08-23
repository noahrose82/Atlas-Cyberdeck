package com.noahrose.pocketlab.feature.system

import org.junit.Assert.assertTrue
import org.junit.Test

class DeviceProfileFormatterTest {

    @Test
    fun format_containsDeviceInformation() {

        val profile =
            DeviceProfile(

                manufacturer = "Google",

                model = "Pixel",

                androidVersion = "16",

                apiLevel = 36,

                architecture = "arm64-v8a",

                availableProcessors = 8,

                totalMemoryMb = 8192,

                availableMemoryMb = 4096,

                totalStorageMb = 256000,

                availableStorageMb = 128000,

                atlasName =
                    "Atlas Cyberdeck",

                atlasVersion =
                    "0.13.0-alpha",

                atlasBuild =
                    "50",

                atlasCodename =
                    "Foundation"
            )

        val output =
            DeviceProfileFormatter.format(
                profile
            )

        assertTrue(
            output.contains(
                "Manufacturer : Google"
            )
        )

        assertTrue(
            output.contains(
                "Model        : Pixel"
            )
        )

        assertTrue(
            output.contains(
                "Architecture : arm64-v8a"
            )
        )

        assertTrue(
            output.contains(
                "Version      : 0.13.0-alpha"
            )
        )
    }
}