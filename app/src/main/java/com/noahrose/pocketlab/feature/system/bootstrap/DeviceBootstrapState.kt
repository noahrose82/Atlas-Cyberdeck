package com.noahrose.pocketlab.feature.system.bootstrap

import com.noahrose.pocketlab.feature.system.DeviceProfile
import com.noahrose.pocketlab.feature.system.capability.AtlasFeature
import com.noahrose.pocketlab.feature.system.capability.DeviceCapabilities
import com.noahrose.pocketlab.feature.system.capability.FeatureGateResult

data class DeviceBootstrapState(

    val profile: DeviceProfile,

    val capabilities: DeviceCapabilities,

    val featureGates: List<FeatureGateResult>,

    val ready: Boolean
) {

    fun featureGate(
        feature: AtlasFeature
    ): FeatureGateResult? {

        return featureGates.firstOrNull { result ->

            result.feature == feature
        }
    }

    fun isFeatureAvailable(
        feature: AtlasFeature
    ): Boolean {

        return featureGate(
            feature
        )?.available == true
    }
}