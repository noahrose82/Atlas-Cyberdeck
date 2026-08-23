package com.noahrose.pocketlab.feature.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.noahrose.pocketlab.ui.components.ActionCard
import com.noahrose.pocketlab.ui.components.AtlasTopBar
import com.noahrose.pocketlab.ui.components.SectionHeader
import com.noahrose.pocketlab.ui.components.SmallStatusCard
import com.noahrose.pocketlab.ui.components.StatusCard

@Composable
fun DashboardScreen(
    onNavigateToTerminal: () -> Unit,
    onNavigateToLinux: () -> Unit,
    onNavigateToFiles: () -> Unit,
    onNavigateToSettings: () -> Unit,
    dashboardViewModel: DashboardViewModel =
        viewModel()
) {

    val uiState by
    dashboardViewModel
        .uiState
        .collectAsState()

    Scaffold { innerPadding ->

        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(
                        innerPadding
                    )
                    .verticalScroll(
                        rememberScrollState()
                    )
                    .padding(
                        20.dp
                    )
        ) {

            AtlasTopBar(
                title =
                    "Atlas Cyberdeck",

                subtitle =
                    "Build with Purpose."
            )

            SectionHeader(
                title =
                    "System Status"
            )

            Spacer(
                modifier =
                    Modifier.height(
                        12.dp
                    )
            )

            StatusCard(
                title =
                    "Linux Environment",

                status =
                    uiState.linuxStatus
            )

            Spacer(
                modifier =
                    Modifier.height(
                        12.dp
                    )
            )

            Row(
                modifier =
                    Modifier.fillMaxWidth(),

                horizontalArrangement =
                    Arrangement.spacedBy(
                        12.dp
                    )
            ) {

                SmallStatusCard(
                    modifier =
                        Modifier.weight(
                            1f
                        ),

                    title =
                        "Packages",

                    value =
                        uiState
                            .packageCount
                            .toString(),

                    symbol =
                        "□"
                )

                SmallStatusCard(
                    modifier =
                        Modifier.weight(
                            1f
                        ),

                    title =
                        "Storage",

                    value =
                        uiState.storageUsed,

                    symbol =
                        "▰"
                )
            }

            Spacer(
                modifier =
                    Modifier.height(
                        12.dp
                    )
            )

            Row(
                modifier =
                    Modifier.fillMaxWidth(),

                horizontalArrangement =
                    Arrangement.spacedBy(
                        12.dp
                    )
            ) {

                SmallStatusCard(
                    modifier =
                        Modifier.weight(
                            1f
                        ),

                    title =
                        "Terminal",

                    value =
                        uiState
                            .terminalStatus
                            .label,

                    symbol =
                        ">_"
                )

                SmallStatusCard(
                    modifier =
                        Modifier.weight(
                            1f
                        ),

                    title =
                        "Architecture",

                    value =
                        uiState.architecture,

                    symbol =
                        "◇"
                )
            }

            Spacer(
                modifier =
                    Modifier.height(
                        28.dp
                    )
            )

            Button(
                modifier =
                    Modifier.fillMaxWidth(),

                enabled =
                    uiState.linuxStatus !=
                            SystemStatus.INSTALLING,

                onClick = {

                    when (
                        uiState.linuxStatus
                    ) {

                        SystemStatus.RUNNING -> {

                            dashboardViewModel
                                .stopLinux()
                        }

                        SystemStatus.STOPPED -> {

                            dashboardViewModel
                                .startLinux()
                        }

                        SystemStatus.NOT_INSTALLED -> {

                            onNavigateToLinux()
                        }

                        else -> {

                            onNavigateToLinux()
                        }
                    }
                }
            ) {

                Text(
                    text =
                        when (
                            uiState.linuxStatus
                        ) {

                            SystemStatus.INSTALLING ->
                                "Installing Linux..."

                            SystemStatus.RUNNING ->
                                "Stop Linux"

                            SystemStatus.STOPPED ->
                                "Start Linux"

                            SystemStatus.NOT_INSTALLED ->
                                "Open Linux Manager"

                            else ->
                                "Open Linux Manager"
                        }
                )
            }

            Spacer(
                modifier =
                    Modifier.height(
                        12.dp
                    )
            )

            Text(
                text =
                    when (
                        uiState.linuxStatus
                    ) {

                        SystemStatus.RUNNING ->
                            "The Atlas Linux environment is installed and currently running."

                        SystemStatus.STOPPED ->
                            "The Atlas Linux environment is installed and ready to start."

                        SystemStatus.INSTALLING ->
                            "The Atlas Linux environment is currently being installed."

                        SystemStatus.NOT_INSTALLED ->
                            "Linux is not installed. Open Linux Manager to configure the environment."

                        else ->
                            "Linux runtime status is being managed by Atlas Cyberdeck."
                    },

                style =
                    MaterialTheme
                        .typography
                        .bodySmall,

                color =
                    MaterialTheme
                        .colorScheme
                        .onSurfaceVariant
            )

            Spacer(
                modifier =
                    Modifier.height(
                        28.dp
                    )
            )

            SectionHeader(
                title =
                    "Quick Actions"
            )

            Spacer(
                modifier =
                    Modifier.height(
                        12.dp
                    )
            )

            Row(
                modifier =
                    Modifier.fillMaxWidth(),

                horizontalArrangement =
                    Arrangement.spacedBy(
                        12.dp
                    )
            ) {

                ActionCard(
                    modifier =
                        Modifier.weight(
                            1f
                        ),

                    title =
                        "Terminal",

                    symbol =
                        ">_",

                    onClick =
                        onNavigateToTerminal
                )

                ActionCard(
                    modifier =
                        Modifier.weight(
                            1f
                        ),

                    title =
                        "Linux",

                    symbol =
                        "◆",

                    onClick =
                        onNavigateToLinux
                )
            }

            Spacer(
                modifier =
                    Modifier.height(
                        12.dp
                    )
            )

            Row(
                modifier =
                    Modifier.fillMaxWidth(),

                horizontalArrangement =
                    Arrangement.spacedBy(
                        12.dp
                    )
            ) {

                ActionCard(
                    modifier =
                        Modifier.weight(
                            1f
                        ),

                    title =
                        "Files",

                    symbol =
                        "▣",

                    onClick =
                        onNavigateToFiles
                )

                ActionCard(
                    modifier =
                        Modifier.weight(
                            1f
                        ),

                    title =
                        "Settings",

                    symbol =
                        "⚙",

                    onClick =
                        onNavigateToSettings
                )
            }

            Spacer(
                modifier =
                    Modifier.height(
                        24.dp
                    )
            )

            Text(
                text =
                    "Atlas Cyberdeck v0.5.1 \"Forge\""
            )
        }
    }
}