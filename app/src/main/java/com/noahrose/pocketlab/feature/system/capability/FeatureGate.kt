package com.noahrose.pocketlab.feature.system.capability

object FeatureGate {

    fun evaluate(
        feature: AtlasFeature,
        capabilities: DeviceCapabilities
    ): FeatureGateResult {

        return when (feature) {

            AtlasFeature.TERMINAL -> {

                result(
                    feature = feature,
                    available =
                        capabilities.terminalAvailable,
                    reason =
                        "Terminal requirements are not satisfied."
                )
            }

            AtlasFeature.FILE_SYSTEM -> {

                /*
                 * Atlas' virtual filesystem
                 * requires the core Android API
                 * and usable storage.
                 */
                val available =
                    capabilities.apiSupported &&
                            capabilities.storageReady

                result(
                    feature = feature,
                    available = available,
                    reason =
                        "Filesystem requirements are not satisfied."
                )
            }

            AtlasFeature.LINUX -> {

                result(
                    feature = feature,
                    available =
                        capabilities.linuxCompatible,
                    reason =
                        "Linux runtime requirements are not satisfied."
                )
            }

            AtlasFeature.SSH -> {

                /*
                 * Initial SSH gate.
                 *
                 * Atlas currently requires a
                 * supported API level and a
                 * functioning terminal.
                 *
                 * Network-state detection will
                 * become a separate capability
                 * later.
                 */
                val available =
                    capabilities.apiSupported &&
                            capabilities.terminalAvailable

                result(
                    feature = feature,
                    available = available,
                    reason =
                        "SSH requirements are not satisfied."
                )
            }
        }
    }

    fun isAvailable(
        feature: AtlasFeature,
        capabilities: DeviceCapabilities
    ): Boolean {

        return evaluate(
            feature = feature,
            capabilities = capabilities
        ).available
    }

    fun evaluateAll(
        capabilities: DeviceCapabilities
    ): List<FeatureGateResult> {

        return AtlasFeature.entries.map { feature ->

            evaluate(
                feature = feature,
                capabilities = capabilities
            )
        }
    }

    private fun result(
        feature: AtlasFeature,
        available: Boolean,
        reason: String
    ): FeatureGateResult {

        return FeatureGateResult(

            feature = feature,

            available = available,

            reason =
                if (available) {
                    null
                } else {
                    reason
                }
        )
    }
}