
package com.noahrose.pocketlab.feature.partner.local

/**
 * Phase 3: Atlas native AI foundation.
 *
 * Connects Android Kotlin code to the native
 * C++ library through JNI.
 *
 * The native library currently provides a
 * version test only. AI inference will be
 * integrated in a later step.
 */
object AtlasNativeProbe {

    init {
        System.loadLibrary("atlas_native_probe")
    }

    /**
     * Calls the native C++ test function.
     *
     * Expected result: 1
     */
    external fun nativeVersion(): Int

    /**
     * Verifies the native connection.
     */
    fun isAvailable(): Boolean {
        return try {
            nativeVersion() == 1
        } catch (_: LinkageError) {
            false
        }
    }
}
