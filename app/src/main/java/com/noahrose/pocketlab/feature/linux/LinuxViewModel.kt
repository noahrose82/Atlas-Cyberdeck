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
import com.noahrose.pocketlab.feature.linux.runtime.activity.LinuxRuntimeActivityEntry
import com.noahrose.pocketlab.feature.linux.runtime.activity.LinuxRuntimeActivityReporter
import com.noahrose.pocketlab.feature.linux.runtime.metrics.LinuxInstallationMetricsReader
import com.noahrose.pocketlab.feature.system.bootstrap.DeviceBootstrapManager
import com.noahrose.pocketlab.feature.system.capability.AtlasFeature
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
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
     * Short user-facing runtime summary.
     */
    private val _runtimeMessage =
        mutableStateOf<String?>(
            null
        )

    val runtimeMessage: State<String?> =
        _runtimeMessage

    /*
     * Prevent overlapping start/stop/remove operations.
     */
    private val _runtimeBusy =
        mutableStateOf(
            false
        )

    val runtimeBusy: State<Boolean> =
        _runtimeBusy

    /*
     * Detailed runtime activity history.
     *
     * These entries originate from the real controller
     * and PRoot backend rather than simulated UI progress.
     */
    private val _runtimeActivity =
        mutableStateOf<List<LinuxRuntimeActivityEntry>>(
            emptyList()
        )

    val runtimeActivity:
            State<List<LinuxRuntimeActivityEntry>> =
        _runtimeActivity

    /*
     * RootFS metrics require filesystem traversal.
     */
    private var metricsRefreshInProgress =
        false

    init {

        /*
         * Observe the runtime activity reporter for the
         * lifetime of this ViewModel.
         *
         * Compose receives a normal State<List<...>>,
         * keeping LinuxScreen simple.
         */
        viewModelScope.launch {

            LinuxRuntimeActivityReporter
                .entries
                .collect { entries ->

                    _runtimeActivity.value =
                        entries
                }
        }
    }

    fun installLinux() {

        refreshFeatureGate()

        _runtimeMessage.value =
            null

        if (
            !_linuxAvailable.value
        ) {

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

                            updateInstallation(
                                progress =
                                    progress,

                                step =
                                    step
                            )
                        }
                }

            when (
                result
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

                    refreshInstallationMetrics()
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

        if (
            _runtimeBusy.value
        ) {
            return
        }

        refreshFeatureGate()

        if (
            !_linuxAvailable.value
        ) {

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

            try {

                val result =
                    withContext(
                        Dispatchers.IO
                    ) {

                        LinuxRuntimeController
                            .start()
                    }

                refreshInstallation()

                refreshInstallationMetrics()

                _runtimeMessage.value =
                    when (
                        result
                    ) {

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

                        LinuxRuntimeControlResult.SAFE_MODE_BLOCKED ->
                            "Ubuntu runtime startup is blocked by Safe Mode."

                        LinuxRuntimeControlResult.START_FAILED ->
                            ProotLinuxRuntimeBackend
                                .getLastError()
                                ?: "Ubuntu runtime failed to start."

                        else ->
                            "Ubuntu runtime could not be started."
                    }

            } catch (
                exception: Exception
            ) {

                refreshInstallation()

                _runtimeMessage.value =
                    exception.message
                        ?.takeIf { message ->
                            message.isNotBlank()
                        }
                        ?: "Ubuntu runtime failed to start."

            } finally {

                _runtimeBusy.value =
                    false
            }
        }
    }

    fun stopLinux() {

        if (
            _runtimeBusy.value
        ) {
            return
        }

        _runtimeBusy.value =
            true

        _runtimeMessage.value =
            "Stopping Ubuntu runtime..."

        viewModelScope.launch {

            try {

                val result =
                    withContext(
                        Dispatchers.IO
                    ) {

                        LinuxRuntimeController
                            .stop()
                    }

                refreshInstallation()

                refreshInstallationMetrics()

                _runtimeMessage.value =
                    when (
                        result
                    ) {

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

            } catch (
                exception: Exception
            ) {

                refreshInstallation()

                _runtimeMessage.value =
                    exception.message
                        ?.takeIf { message ->
                            message.isNotBlank()
                        }
                        ?: "Ubuntu runtime failed to stop."

            } finally {

                _runtimeBusy.value =
                    false
            }
        }
    }

    fun removeLinux() {

        if (
            _installation.value.isInstalling ||
            _runtimeBusy.value
        ) {
            return
        }

        _runtimeBusy.value =
            true

        viewModelScope.launch {

            try {

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

                LinuxRuntimeActivityReporter
                    .clear()

                refreshInstallation()

            } finally {

                _runtimeBusy.value =
                    false
            }
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
         * Reconcile repository runtime state against the
         * actual native PRoot process.
         */
        LinuxRuntimeController
            .getSession()

        refreshInstallation()

        refreshInstallationMetrics()
    }

    fun clearRuntimeMessage() {

        _runtimeMessage.value =
            null
    }

    fun clearRuntimeActivity() {

        LinuxRuntimeActivityReporter
            .clear()
    }

    private fun refreshInstallationMetrics() {

        val current =
            LinuxRepository
                .getInstallation()

        if (
            !current.installed ||
            current.isInstalling ||
            metricsRefreshInProgress
        ) {
            return
        }

        metricsRefreshInProgress =
            true

        viewModelScope.launch {

            try {

                val metrics =
                    withContext(
                        Dispatchers.IO
                    ) {

                        LinuxInstallationMetricsReader
                            .read()
                    }

                metrics
                    ?.let { measured ->

                        LinuxRepository
                            .updateMetrics(
                                packageCount =
                                    measured.packageCount,

                                storageUsedMb =
                                    measured.storageUsedMb
                            )

                        refreshInstallation()
                    }

            } finally {

                metricsRefreshInProgress =
                    false
            }
        }
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