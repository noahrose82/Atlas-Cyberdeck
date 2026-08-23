package com.noahrose.pocketlab.feature.linux

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.noahrose.pocketlab.feature.linux.model.LinuxInstallation
import com.noahrose.pocketlab.feature.linux.repository.LinuxRepository
import com.noahrose.pocketlab.feature.linux.rootfs.provision.LinuxRootfsProvisionManager
import com.noahrose.pocketlab.feature.linux.rootfs.provision.LinuxRootfsProvisionResult
import com.noahrose.pocketlab.feature.linux.runtime.LinuxRuntimeController
import com.noahrose.pocketlab.feature.system.bootstrap.DeviceBootstrapManager
import com.noahrose.pocketlab.feature.system.capability.AtlasFeature
import kotlinx.coroutines.launch

class LinuxViewModel : ViewModel() {

    private val _installation =
        mutableStateOf(
            LinuxRepository.getInstallation()
        )

    val installation: State<LinuxInstallation> =
        _installation

    private val _linuxAvailable =
        mutableStateOf(
            DeviceBootstrapManager
                .isFeatureAvailable(
                    AtlasFeature.LINUX
                )
        )

    val linuxAvailable: State<Boolean> =
        _linuxAvailable

    private val _blockedReason =
        mutableStateOf(
            DeviceBootstrapManager
                .getFeatureGate(
                    AtlasFeature.LINUX
                )
                ?.reason
        )

    val blockedReason: State<String?> =
        _blockedReason

    fun installLinux() {

        refreshFeatureGate()

        if (!_linuxAvailable.value) {

            val current =
                LinuxRepository
                    .getInstallation()

            LinuxRepository
                .updateInstallation(
                    current.copy(
                        isInstalling =
                            false,

                        installationProgress =
                            0f,

                        installationStep =
                            _blockedReason.value
                                ?: "Linux is not available on this device."
                    )
                )

            refreshInstallation()

            return
        }

        if (
            _installation.value.isInstalling ||
            _installation.value.installed
        ) {
            return
        }

        viewModelScope.launch {

            LinuxRepository
                .startInstallation()

            refreshInstallation()

            when (
                val result =
                    LinuxRootfsProvisionManager
                        .provision { progress, step ->

                            updateInstallation(
                                progress =
                                    progress,

                                step =
                                    step
                            )
                        }
            ) {

                LinuxRootfsProvisionResult.Success -> {

                    updateInstallation(
                        progress =
                            1f,

                        step =
                            "Ubuntu installation complete."
                    )

                    LinuxRepository
                        .completeInstallation()

                    refreshInstallation()
                }

                is LinuxRootfsProvisionResult.Failure -> {

                    failInstallation(
                        message =
                            result.message
                    )
                }
            }
        }
    }

    fun startLinux() {

        refreshFeatureGate()

        if (!_linuxAvailable.value) {
            return
        }

        LinuxRuntimeController
            .start()

        refreshInstallation()
    }

    fun stopLinux() {

        LinuxRuntimeController
            .stop()

        refreshInstallation()
    }

    fun removeLinux() {

        if (_installation.value.isInstalling) {
            return
        }

        LinuxRuntimeController
            .stop()

        LinuxRepository
            .removeLinux()

        refreshInstallation()
    }

    fun refreshFeatureGate() {

        _linuxAvailable.value =
            DeviceBootstrapManager
                .isFeatureAvailable(
                    AtlasFeature.LINUX
                )

        _blockedReason.value =
            DeviceBootstrapManager
                .getFeatureGate(
                    AtlasFeature.LINUX
                )
                ?.reason
    }

    private fun updateInstallation(
        progress: Float,
        step: String
    ) {

        val updatedInstallation =
            LinuxRepository
                .getInstallation()
                .copy(
                    isInstalling =
                        true,

                    installationProgress =
                        progress,

                    installationStep =
                        step
                )

        LinuxRepository
            .updateInstallation(
                updatedInstallation
            )

        refreshInstallation()
    }

    private fun failInstallation(
        message: String
    ) {

        val current =
            LinuxRepository
                .getInstallation()

        LinuxRepository
            .updateInstallation(
                current.copy(
                    installed =
                        false,

                    running =
                        false,

                    isInstalling =
                        false,

                    installationProgress =
                        0f,

                    installationStep =
                        message
                )
            )

        refreshInstallation()
    }

    private fun refreshInstallation() {

        _installation.value =
            LinuxRepository
                .getInstallation()
    }
}