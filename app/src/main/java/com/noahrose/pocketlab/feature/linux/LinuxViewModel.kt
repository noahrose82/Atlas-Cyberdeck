package com.noahrose.pocketlab.feature.linux

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.noahrose.pocketlab.feature.linux.model.LinuxInstallation
import com.noahrose.pocketlab.feature.linux.repository.LinuxRepository
import com.noahrose.pocketlab.feature.linux.rootfs.provision.LinuxRootfsProvisionManager
import com.noahrose.pocketlab.feature.linux.rootfs.provision.LinuxRootfsProvisionResult
import com.noahrose.pocketlab.feature.linux.runtime.LinuxRuntimeControlResult
import com.noahrose.pocketlab.feature.linux.runtime.LinuxRuntimeController
import com.noahrose.pocketlab.feature.linux.runtime.ProotLinuxRuntimeBackend
import com.noahrose.pocketlab.feature.system.bootstrap.DeviceBootstrapManager
import com.noahrose.pocketlab.feature.system.capability.AtlasFeature
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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

    /*
     * Visible runtime feedback.
     *
     * This prevents Start Linux failures from
     * disappearing silently.
     */
    private val _runtimeMessage =
        mutableStateOf<String?>(
            null
        )

    val runtimeMessage: State<String?> =
        _runtimeMessage

    /*
     * PRoot startup and guest verification involve
     * native process work and blocking stream I/O.
     *
     * They must never execute on the Compose/UI
     * thread.
     */
    private val _runtimeBusy =
        mutableStateOf(
            false
        )

    val runtimeBusy: State<Boolean> =
        _runtimeBusy

    fun installLinux() {

        refreshFeatureGate()

        _runtimeMessage.value =
            null

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

            val result =
                withContext(
                    Dispatchers.IO
                ) {

                    LinuxRootfsProvisionManager
                        .provision { progress, step ->

                            /*
                             * Repository state updates eventually
                             * reach Compose through refreshes.
                             *
                             * Provisioning itself remains off the
                             * UI thread.
                             */
                            updateInstallation(
                                progress =
                                    progress,

                                step =
                                    step
                            )
                        }
                }

            when (result) {

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

        if (_runtimeBusy.value) {
            return
        }

        refreshFeatureGate()

        if (!_linuxAvailable.value) {

            _runtimeMessage.value =
                _blockedReason.value
                    ?: "Linux runtime is unavailable on this device."

            return
        }

        _runtimeBusy.value =
            true

        _runtimeMessage.value =
            "Starting Ubuntu runtime..."

        viewModelScope.launch {

            val result =
                withContext(
                    Dispatchers.IO
                ) {

                    LinuxRuntimeController
                        .start()
                }

            refreshInstallation()

            _runtimeMessage.value =
                when (result) {

                    LinuxRuntimeControlResult.STARTED ->
                        "Ubuntu runtime started successfully."

                    LinuxRuntimeControlResult.ALREADY_RUNNING ->
                        "Ubuntu runtime is already running."

                    LinuxRuntimeControlResult.NOT_INSTALLED ->
                        "Ubuntu is not installed."

                    LinuxRuntimeControlResult.INSTALLATION_IN_PROGRESS ->
                        "Ubuntu installation is still in progress."

                    LinuxRuntimeControlResult.FEATURE_UNAVAILABLE ->
                        _blockedReason.value
                            ?: "Linux runtime is unavailable on this device."

                    LinuxRuntimeControlResult.START_FAILED ->
                        ProotLinuxRuntimeBackend
                            .getLastError()
                            ?: "Ubuntu runtime failed to start."

                    else ->
                        "Ubuntu runtime could not be started."
                }

            _runtimeBusy.value =
                false
        }
    }

    fun stopLinux() {

        if (_runtimeBusy.value) {
            return
        }

        _runtimeBusy.value =
            true

        _runtimeMessage.value =
            "Stopping Ubuntu runtime..."

        viewModelScope.launch {

            val result =
                withContext(
                    Dispatchers.IO
                ) {

                    LinuxRuntimeController
                        .stop()
                }

            refreshInstallation()

            _runtimeMessage.value =
                when (result) {

                    LinuxRuntimeControlResult.STOPPED ->
                        "Ubuntu runtime stopped."

                    LinuxRuntimeControlResult.ALREADY_STOPPED ->
                        "Ubuntu runtime is already stopped."

                    LinuxRuntimeControlResult.NOT_INSTALLED ->
                        "Ubuntu is not installed."

                    LinuxRuntimeControlResult.STOP_FAILED ->
                        ProotLinuxRuntimeBackend
                            .getLastError()
                            ?: "Ubuntu runtime failed to stop."

                    else ->
                        "Ubuntu runtime could not be stopped."
                }

            _runtimeBusy.value =
                false
        }
    }

    fun removeLinux() {

        if (
            _installation.value.isInstalling ||
            _runtimeBusy.value
        ) {
            return
        }

        viewModelScope.launch {

            _runtimeBusy.value =
                true

            withContext(
                Dispatchers.IO
            ) {

                LinuxRuntimeController
                    .stop()
            }

            LinuxRepository
                .removeLinux()

            _runtimeMessage.value =
                null

            refreshInstallation()

            _runtimeBusy.value =
                false
        }
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

        /*
         * Reconcile a stale repository RUNNING
         * state if the native process exited while
         * the screen was away.
         */
        LinuxRuntimeController
            .getSession()

        refreshInstallation()
    }

    fun clearRuntimeMessage() {

        _runtimeMessage.value =
            null
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