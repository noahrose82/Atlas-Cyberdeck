package com.noahrose.pocketlab.feature.system.bootstrap

import com.noahrose.pocketlab.feature.system.DeviceInfoProvider
import com.noahrose.pocketlab.feature.system.DeviceProfile
import com.noahrose.pocketlab.feature.system.capability.AtlasFeature
import com.noahrose.pocketlab.feature.system.capability.DeviceCapabilities
import com.noahrose.pocketlab.feature.system.capability.DeviceCapabilityAnalyzer
import com.noahrose.pocketlab.feature.system.capability.FeatureGate
import com.noahrose.pocketlab.feature.system.capability.FeatureGateResult

object DeviceBootstrapManager {

    private var currentState: DeviceBootstrapState? =
        null

    fun bootstrap(): Boolean {

        val profile =
            DeviceProfilePersistence.load()
                ?: createAndSaveProfile()
                ?: return false

        currentState =
            buildState(
                profile
            )

        return true
    }

    fun getState(): DeviceBootstrapState? {

        currentState?.let {
            return it
        }

        val profile =
            DeviceProfilePersistence.load()
                ?: return null

        val state =
            buildState(
                profile
            )

        currentState =
            state

        return state
    }

    fun getProfile(): DeviceProfile? {

        return getState()
            ?.profile
    }

    fun getCapabilities(): DeviceCapabilities? {

        return getState()
            ?.capabilities
    }

    fun getFeatureGates(): List<FeatureGateResult> {

        return getState()
            ?.featureGates
            ?: emptyList()
    }

    fun getFeatureGate(
        feature: AtlasFeature
    ): FeatureGateResult? {

        return getState()
            ?.featureGate(
                feature
            )
    }

    fun isFeatureAvailable(
        feature: AtlasFeature
    ): Boolean {

        return getState()
            ?.isFeatureAvailable(
                feature
            )
            ?: false
    }

    fun isReady(): Boolean {

        return getState()
            ?.ready
            ?: false
    }

    fun isBootstrapped(): Boolean {

        return DeviceProfilePersistence.exists()
    }

    fun refresh(): Boolean {

        val detectedProfile =
            DeviceInfoProvider.getProfile()
                ?: return false

        val saved =
            DeviceProfilePersistence.save(
                detectedProfile
            )

        if (!saved) {
            return false
        }

        currentState =
            buildState(
                detectedProfile
            )

        return true
    }

    fun reset(): Boolean {

        currentState =
            null

        return DeviceProfilePersistence.clear()
    }

    private fun createAndSaveProfile(): DeviceProfile? {

        val detectedProfile =
            DeviceInfoProvider.getProfile()
                ?: return null

        val saved =
            DeviceProfilePersistence.save(
                detectedProfile
            )

        if (!saved) {
            return null
        }

        return detectedProfile
    }

    private fun buildState(
        profile: DeviceProfile
    ): DeviceBootstrapState {

        val capabilities =
            DeviceCapabilityAnalyzer.analyze(
                profile
            )

        val featureGates =
            FeatureGate.evaluateAll(
                capabilities
            )

        return DeviceBootstrapState(

            profile =
                profile,

            capabilities =
                capabilities,

            featureGates =
                featureGates,

            ready =
                capabilities.overallReady
        )
    }
}