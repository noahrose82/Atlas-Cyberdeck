package com.noahrose.pocketlab.feature.boot

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.noahrose.pocketlab.feature.system.capability.DeviceCapabilities

@Composable
fun BootScreen(
    onBootComplete: () -> Unit,
    bootViewModel: BootViewModel = viewModel()
) {

    val uiState by
    bootViewModel
        .uiState
        .collectAsState()

    LaunchedEffect(
        uiState.isComplete
    ) {

        if (uiState.isComplete) {

            onBootComplete()
        }
    }

    Surface(
        modifier =
            Modifier.fillMaxSize(),
        color =
            MaterialTheme
                .colorScheme
                .background
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    horizontal = 32.dp
                ),
            verticalArrangement =
                Arrangement.Center,
            horizontalAlignment =
                Alignment.Start
        ) {

            AnimatedVisibility(
                visible = true,
                enter = fadeIn()
            ) {

                Column {

                    Text(
                        text = "ATLAS",
                        style =
                            MaterialTheme
                                .typography
                                .displayMedium,
                        fontWeight =
                            FontWeight.Bold
                    )

                    Text(
                        text = "CYBERDECK",
                        style =
                            MaterialTheme
                                .typography
                                .titleLarge,
                        color =
                            MaterialTheme
                                .colorScheme
                                .primary
                    )
                }
            }

            Spacer(
                modifier =
                    Modifier.height(
                        36.dp
                    )
            )

            Text(
                text =
                    uiState
                        .currentStep
                        .displayText,
                style =
                    MaterialTheme
                        .typography
                        .bodyLarge
            )

            Spacer(
                modifier =
                    Modifier.height(
                        16.dp
                    )
            )

            LinearProgressIndicator(
                progress = {
                    uiState.progress
                },
                modifier =
                    Modifier
                        .fillMaxWidth()
            )

            Spacer(
                modifier =
                    Modifier.height(
                        12.dp
                    )
            )

            Text(
                text =
                    "${
                        (
                                uiState.progress *
                                        100
                                )
                            .toInt()
                    }%",
                style =
                    MaterialTheme
                        .typography
                        .labelMedium,
                color =
                    MaterialTheme
                        .colorScheme
                        .onSurfaceVariant
            )

            /*
             * Capability checks become visible
             * once Atlas reaches the device
             * analysis stage.
             */
            uiState.capabilities
                ?.let { capabilities ->

                    Spacer(
                        modifier =
                            Modifier.height(
                                28.dp
                            )
                    )

                    Text(
                        text =
                            "DEVICE CAPABILITY CHECK",
                        style =
                            MaterialTheme
                                .typography
                                .titleSmall,
                        color =
                            MaterialTheme
                                .colorScheme
                                .primary,
                        fontWeight =
                            FontWeight.Bold
                    )

                    Spacer(
                        modifier =
                            Modifier.height(
                                12.dp
                            )
                    )

                    CapabilityChecks(
                        capabilities =
                            capabilities
                    )
                }

            if (uiState.isComplete) {

                Spacer(
                    modifier =
                        Modifier.height(
                            28.dp
                        )
                )

                Text(
                    text =
                        if (
                            uiState.isDegraded
                        ) {

                            "SYSTEM DEGRADED"

                        } else {

                            "SYSTEM READY"
                        },
                    style =
                        MaterialTheme
                            .typography
                            .titleLarge,
                    color =
                        MaterialTheme
                            .colorScheme
                            .primary,
                    fontWeight =
                        FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun CapabilityChecks(
    capabilities: DeviceCapabilities
) {

    CapabilityRow(
        label = "Architecture",
        ready =
            capabilities
                .architectureSupported
    )

    CapabilityRow(
        label = "Android API",
        ready =
            capabilities
                .apiSupported
    )

    CapabilityRow(
        label = "Memory",
        ready =
            capabilities
                .memoryReady
    )

    CapabilityRow(
        label = "Storage",
        ready =
            capabilities
                .storageReady
    )

    CapabilityRow(
        label = "Terminal",
        ready =
            capabilities
                .terminalAvailable
    )

    CapabilityRow(
        label = "Linux",
        ready =
            capabilities
                .linuxCompatible
    )
}

@Composable
private fun CapabilityRow(
    label: String,
    ready: Boolean
) {

    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(
                    vertical = 3.dp
                ),
        horizontalArrangement =
            Arrangement.SpaceBetween
    ) {

        Text(
            text = label,
            style =
                MaterialTheme
                    .typography
                    .bodyMedium
        )

        Text(
            text =
                if (ready) {
                    "READY"
                } else {
                    "NOT READY"
                },
            style =
                MaterialTheme
                    .typography
                    .bodyMedium,
            color =
                if (ready) {

                    MaterialTheme
                        .colorScheme
                        .primary

                } else {

                    MaterialTheme
                        .colorScheme
                        .error
                },
            fontWeight =
                FontWeight.Bold
        )
    }
}