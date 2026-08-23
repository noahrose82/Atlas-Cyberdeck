package com.noahrose.pocketlab.feature.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.noahrose.pocketlab.feature.linux.model.LinuxInstallation
import com.noahrose.pocketlab.feature.linux.model.LinuxRuntimeStatus
import com.noahrose.pocketlab.feature.linux.model.runtimeStatus
import com.noahrose.pocketlab.feature.linux.repository.LinuxRepository
import com.noahrose.pocketlab.feature.linux.runtime.LinuxRuntimeController
import com.noahrose.pocketlab.feature.workspace.WorkspaceRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class DashboardViewModel : ViewModel() {

    val uiState: StateFlow<DashboardUiState> =
        combine(
            LinuxRepository.installation,
            WorkspaceRepository.workspace
        ) { installation, workspace ->

            installation.toDashboardUiState(
                terminalReady =
                    workspace.terminalReady,

                architecture =
                    workspace.architecture
            )

        }.stateIn(
            scope =
                viewModelScope,

            started =
                SharingStarted
                    .WhileSubscribed(
                        5_000
                    ),

            initialValue =
                LinuxRepository
                    .getInstallation()
                    .toDashboardUiState(
                        terminalReady =
                            WorkspaceRepository
                                .workspace
                                .value
                                .terminalReady,

                        architecture =
                            WorkspaceRepository
                                .workspace
                                .value
                                .architecture
                    )
        )

    fun startLinux() {

        LinuxRuntimeController
            .start()
    }

    fun stopLinux() {

        LinuxRuntimeController
            .stop()
    }
}

private fun LinuxInstallation
        .toDashboardUiState(
    terminalReady: Boolean,
    architecture: String
): DashboardUiState {

    return DashboardUiState(

        linuxInstalled =
            installed,

        linuxStatus =
            when (runtimeStatus()) {

                LinuxRuntimeStatus.INSTALLING ->
                    SystemStatus.INSTALLING

                LinuxRuntimeStatus.NOT_INSTALLED ->
                    SystemStatus.NOT_INSTALLED

                LinuxRuntimeStatus.RUNNING ->
                    SystemStatus.RUNNING

                LinuxRuntimeStatus.STOPPED ->
                    SystemStatus.STOPPED
            },

        terminalStatus =
            if (terminalReady) {

                SystemStatus.READY

            } else {

                SystemStatus.OFFLINE
            },

        packageCount =
            packageCount,

        storageUsed =
            formatStorage(
                storageUsedMb
            ),

        architecture =
            architecture
    )
}

private fun formatStorage(
    storageUsedMb: Long
): String {

    return if (
        storageUsedMb >= 1024L
    ) {

        String.format(
            "%.1f GB",
            storageUsedMb / 1024.0
        )

    } else {

        "$storageUsedMb MB"
    }
}