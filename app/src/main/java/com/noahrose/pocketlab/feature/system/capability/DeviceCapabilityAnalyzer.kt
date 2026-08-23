package com.noahrose.pocketlab.feature.system.capability

import com.noahrose.pocketlab.feature.system.DeviceProfile

object DeviceCapabilityAnalyzer {

    /*
     * Atlas compatibility thresholds.
     *
     * These are intentionally conservative
     * starting values and can evolve as the
     * Cyberdeck gains more capabilities.
     */
    private const val MIN_API_LEVEL =
        29

    private const val MIN_MEMORY_MB =
        2048L

    private const val MIN_AVAILABLE_STORAGE_MB =
        1024L

    fun analyze(
        profile: DeviceProfile
    ): DeviceCapabilities {

        val architectureSupported =
            isArchitectureSupported(
                profile.architecture
            )

        val apiSupported =
            profile.apiLevel >=
                    MIN_API_LEVEL

        val memoryReady =
            profile.totalMemoryMb >=
                    MIN_MEMORY_MB

        val storageReady =
            profile.availableStorageMb >=
                    MIN_AVAILABLE_STORAGE_MB

        /*
         * The Atlas terminal is implemented
         * inside the application itself.
         */
        val terminalAvailable =
            apiSupported

        /*
         * Linux compatibility currently means
         * the device meets the baseline needed
         * for the future Atlas Linux runtime.
         *
         * This does NOT claim Linux is already
         * installed or running.
         */
        val linuxCompatible =
            architectureSupported &&
                    apiSupported &&
                    memoryReady &&
                    storageReady

        val overallReady =
            architectureSupported &&
                    apiSupported &&
                    memoryReady &&
                    storageReady &&
                    terminalAvailable

        return DeviceCapabilities(

            architectureSupported =
                architectureSupported,

            apiSupported =
                apiSupported,

            memoryReady =
                memoryReady,

            storageReady =
                storageReady,

            terminalAvailable =
                terminalAvailable,

            linuxCompatible =
                linuxCompatible,

            overallReady =
                overallReady
        )
    }

    private fun isArchitectureSupported(
        architecture: String
    ): Boolean {

        return when (
            architecture.lowercase()
        ) {

            "arm64-v8a",
            "armeabi-v7a",
            "x86_64" ->
                true

            else ->
                false
        }
    }
}