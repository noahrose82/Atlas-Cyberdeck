package com.noahrose.pocketlab.feature.linux.runtime

import com.noahrose.pocketlab.feature.linux.repository.LinuxRepository
import com.noahrose.pocketlab.feature.system.bootstrap.DeviceBootstrapManager
import com.noahrose.pocketlab.feature.system.capability.AtlasFeature

object LinuxRuntimeController {

    private val backend: LinuxRuntimeBackend =
        SimulatedLinuxRuntimeBackend

    private var activeSession: LinuxRuntimeSession? =
        null

    fun getSession(): LinuxRuntimeSession? =
        activeSession

    fun start(): LinuxRuntimeControlResult {

        val installation =
            LinuxRepository
                .getInstallation()

        if (installation.isInstalling) {

            return LinuxRuntimeControlResult
                .INSTALLATION_IN_PROGRESS
        }

        if (!installation.installed) {

            return LinuxRuntimeControlResult
                .NOT_INSTALLED
        }

        if (installation.running) {

            return LinuxRuntimeControlResult
                .ALREADY_RUNNING
        }

        val linuxAvailable =
            DeviceBootstrapManager
                .isFeatureAvailable(
                    AtlasFeature.LINUX
                )

        if (!linuxAvailable) {

            return LinuxRuntimeControlResult
                .FEATURE_UNAVAILABLE
        }

        return when (
            val result =
                backend.start()
        ) {

            is LinuxRuntimeBackendResult.Success -> {

                activeSession =
                    result.session

                LinuxRepository
                    .startLinux()

                LinuxRuntimeControlResult
                    .STARTED
            }

            is LinuxRuntimeBackendResult.Failure -> {

                activeSession =
                    null

                LinuxRuntimeControlResult
                    .START_FAILED
            }
        }
    }

    fun stop(): LinuxRuntimeControlResult {

        val installation =
            LinuxRepository
                .getInstallation()

        if (!installation.installed) {

            activeSession =
                null

            return LinuxRuntimeControlResult
                .NOT_INSTALLED
        }

        if (!installation.running) {

            activeSession =
                null

            return LinuxRuntimeControlResult
                .ALREADY_STOPPED
        }

        return when (
            backend.stop()
        ) {

            is LinuxRuntimeBackendResult.Success -> {

                activeSession =
                    null

                LinuxRepository
                    .stopLinux()

                LinuxRuntimeControlResult
                    .STOPPED
            }

            is LinuxRuntimeBackendResult.Failure -> {

                LinuxRuntimeControlResult
                    .STOP_FAILED
            }
        }
    }
}

enum class LinuxRuntimeControlResult {

    STARTED,

    STOPPED,

    ALREADY_RUNNING,

    ALREADY_STOPPED,

    NOT_INSTALLED,

    INSTALLATION_IN_PROGRESS,

    FEATURE_UNAVAILABLE,

    START_FAILED,

    STOP_FAILED
}