package com.noahrose.pocketlab.feature.linux.runtime.startup

import com.noahrose.pocketlab.feature.linux.repository.LinuxRepository
import com.noahrose.pocketlab.feature.linux.runtime.LinuxRuntimeControlResult
import com.noahrose.pocketlab.feature.linux.runtime.LinuxRuntimeController
import com.noahrose.pocketlab.feature.linux.runtime.safety.LinuxRuntimeCircuitBreaker
import com.noahrose.pocketlab.feature.linux.runtime.safety.LinuxRuntimeSafetyMode
import com.noahrose.pocketlab.feature.settings.AtlasSettingsRepository
import com.noahrose.pocketlab.feature.system.bootstrap.DeviceBootstrapManager
import com.noahrose.pocketlab.feature.system.capability.AtlasFeature
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object LinuxQuickStartManager {

    suspend fun startIfEligible():
            LinuxQuickStartResult {

        if (
            !AtlasSettingsRepository
                .isLinuxQuickStartEnabled()
        ) {

            return LinuxQuickStartResult.DISABLED
        }

        val installation =
            LinuxRepository
                .getInstallation()

        if (
            installation.isInstalling
        ) {

            return LinuxQuickStartResult.INSTALLATION_IN_PROGRESS
        }

        if (
            !installation.installed
        ) {

            return LinuxQuickStartResult.NOT_INSTALLED
        }

        if (
            installation.running
        ) {

            return LinuxQuickStartResult.ALREADY_RUNNING
        }

        /*
         * Quick Start is intentionally stricter than
         * manual runtime startup.
         *
         * RECOVERY_ARMED may permit controlled manual
         * startup, but Atlas must never enter recovery
         * automatically.
         */
        val safetySnapshot =
            LinuxRuntimeCircuitBreaker
                .getSnapshot()

        if (
            safetySnapshot.mode !=
            LinuxRuntimeSafetyMode.NORMAL
        ) {

            return LinuxQuickStartResult.SAFETY_BLOCKED
        }

        if (
            !DeviceBootstrapManager
                .isFeatureAvailable(
                    AtlasFeature.LINUX
                )
        ) {

            return LinuxQuickStartResult.FEATURE_UNAVAILABLE
        }

        val result =
            withContext(
                Dispatchers.IO
            ) {

                LinuxRuntimeController
                    .start()
            }

        return when (
            result
        ) {

            LinuxRuntimeControlResult.STARTED ->
                LinuxQuickStartResult.STARTED

            LinuxRuntimeControlResult.ALREADY_RUNNING ->
                LinuxQuickStartResult.ALREADY_RUNNING

            LinuxRuntimeControlResult.NOT_INSTALLED ->
                LinuxQuickStartResult.NOT_INSTALLED

            LinuxRuntimeControlResult.INSTALLATION_IN_PROGRESS ->
                LinuxQuickStartResult.INSTALLATION_IN_PROGRESS

            LinuxRuntimeControlResult.FEATURE_UNAVAILABLE ->
                LinuxQuickStartResult.FEATURE_UNAVAILABLE

            LinuxRuntimeControlResult.SAFE_MODE_BLOCKED ->
                LinuxQuickStartResult.SAFETY_BLOCKED

            else ->
                LinuxQuickStartResult.START_FAILED
        }
    }
}

enum class LinuxQuickStartResult {

    STARTED,

    DISABLED,

    NOT_INSTALLED,

    INSTALLATION_IN_PROGRESS,

    ALREADY_RUNNING,

    FEATURE_UNAVAILABLE,

    SAFETY_BLOCKED,

    START_FAILED
}