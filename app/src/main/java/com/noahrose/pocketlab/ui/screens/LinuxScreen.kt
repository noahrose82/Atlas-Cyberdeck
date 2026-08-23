package com.noahrose.pocketlab.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.noahrose.pocketlab.feature.linux.LinuxViewModel

@Composable
fun LinuxScreen(
    onBack: () -> Unit,
    linuxViewModel: LinuxViewModel = viewModel()
) {

    val installation by
    linuxViewModel.installation

    val linuxAvailable by
    linuxViewModel.linuxAvailable

    val blockedReason by
    linuxViewModel.blockedReason

    val lifecycleOwner =
        LocalLifecycleOwner.current

    DisposableEffect(
        lifecycleOwner,
        linuxViewModel
    ) {

        val observer =
            LifecycleEventObserver { _, event ->

                if (
                    event ==
                    Lifecycle.Event.ON_RESUME
                ) {

                    linuxViewModel
                        .refreshFeatureGate()
                }
            }

        lifecycleOwner
            .lifecycle
            .addObserver(
                observer
            )

        linuxViewModel
            .refreshFeatureGate()

        onDispose {

            lifecycleOwner
                .lifecycle
                .removeObserver(
                    observer
                )
        }
    }

    val installationSteps =
        listOf(
            "Preparing installation...",
            "Downloading packages...",
            "Installing packages...",
            "Configuring system...",
            "Cleaning up..."
        )

    Button(
        onClick = onBack
    ) {

        Text(
            "Back to Dashboard"
        )
    }

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(24.dp),

        verticalArrangement =
            Arrangement.spacedBy(
                space = 16.dp,
                alignment =
                    Alignment.CenterVertically
            ),

        horizontalAlignment =
            Alignment.CenterHorizontally
    ) {

        Text(
            text =
                installation
                    .distribution
                    .displayName,

            style =
                MaterialTheme
                    .typography
                    .headlineMedium
        )

        Text(
            text =
                "Version ${installation.version}",

            style =
                MaterialTheme
                    .typography
                    .bodyLarge
        )

        /*
         * Feature-gated device state.
         */
        if (!linuxAvailable) {

            Text(
                text =
                    "Status: Unavailable",

                color =
                    MaterialTheme
                        .colorScheme
                        .error,

                style =
                    MaterialTheme
                        .typography
                        .titleMedium
            )

            HorizontalDivider(
                modifier =
                    Modifier
                        .widthIn(
                            max = 500.dp
                        )
            )

            Text(
                text =
                    "Linux runtime is not available on this device.",

                color =
                    MaterialTheme
                        .colorScheme
                        .error,

                style =
                    MaterialTheme
                        .typography
                        .titleMedium
            )

            Text(
                text =
                    blockedReason
                        ?: "This device does not meet the requirements for the Atlas Linux environment.",

                style =
                    MaterialTheme
                        .typography
                        .bodyMedium
            )

            Text(
                text =
                    "Atlas Cyberdeck will continue operating with supported features.",

                style =
                    MaterialTheme
                        .typography
                        .bodyMedium,

                color =
                    MaterialTheme
                        .colorScheme
                        .onSurfaceVariant
            )

            return@Column
        }

        /*
         * Linux installation state.
         */
        Text(
            text =
                when {

                    installation.isInstalling ->
                        "Status: Installing..."

                    installation.installed ->
                        "Status: Installed ✓"

                    else ->
                        "Status: Not Installed"
                },

            color =
                when {

                    installation.isInstalling ->
                        Color(
                            0xFFFFC107
                        )

                    installation.installed ->
                        Color(
                            0xFF00C853
                        )

                    else ->
                        Color(
                            0xFFD32F2F
                        )
                },

            style =
                MaterialTheme
                    .typography
                    .titleMedium
        )

        HorizontalDivider(
            modifier =
                Modifier
                    .widthIn(
                        max = 500.dp
                    )
        )

        /*
         * Installation progress.
         */
        if (installation.isInstalling) {

            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .widthIn(
                            max = 500.dp
                        ),

                verticalArrangement =
                    Arrangement.spacedBy(
                        10.dp
                    )
            ) {

                Text(
                    text =
                        "Current Task",

                    style =
                        MaterialTheme
                            .typography
                            .labelLarge
                )

                Text(
                    text =
                        installation
                            .installationStep,

                    style =
                        MaterialTheme
                            .typography
                            .titleMedium
                )

                LinearProgressIndicator(
                    progress = {
                        installation
                            .installationProgress
                    },

                    modifier =
                        Modifier
                            .fillMaxWidth()
                )

                Text(
                    text =
                        "${
                            (
                                    installation
                                        .installationProgress *
                                            100
                                    )
                                .toInt()
                        }% complete",

                    style =
                        MaterialTheme
                            .typography
                            .bodyMedium
                )

                HorizontalDivider()

                Text(
                    text =
                        "Installation Steps",

                    style =
                        MaterialTheme
                            .typography
                            .labelLarge
                )

                installationSteps
                    .forEach { step ->

                        InstallationStepRow(
                            step = step,
                            currentStep =
                                installation
                                    .installationStep,
                            currentProgress =
                                installation
                                    .installationProgress
                        )
                    }
            }
        }

        /*
         * Installed Linux runtime state.
         */
        if (
            installation.installed &&
            !installation.isInstalling
        ) {

            Text(
                text =
                    "Ubuntu is ready to use.",

                color =
                    Color(
                        0xFF00C853
                    ),

                style =
                    MaterialTheme
                        .typography
                        .titleMedium
            )

            Text(
                text =
                    if (installation.running) {
                        "Runtime: Running"
                    } else {
                        "Runtime: Stopped"
                    },

                color =
                    if (installation.running) {
                        Color(
                            0xFF00C853
                        )
                    } else {
                        MaterialTheme
                            .colorScheme
                            .onSurfaceVariant
                    },

                style =
                    MaterialTheme
                        .typography
                        .titleMedium
            )
        }

        /*
         * Installation statistics.
         */
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .widthIn(
                        max = 500.dp
                    ),

            verticalArrangement =
                Arrangement.spacedBy(
                    8.dp
                )
        ) {

            Text(
                "Packages: ${installation.packageCount}"
            )

            Text(
                "Storage Used: ${installation.storageUsedMb} MB"
            )
        }

        /*
         * Linux controls.
         */
        when {

            installation.isInstalling -> {

                Button(
                    onClick = {},
                    enabled = false
                ) {

                    Text(
                        "Installing..."
                    )
                }
            }

            installation.installed -> {

                Column(
                    horizontalAlignment =
                        Alignment.CenterHorizontally,

                    verticalArrangement =
                        Arrangement.spacedBy(
                            10.dp
                        )
                ) {

                    if (installation.running) {

                        Button(
                            onClick = {

                                linuxViewModel
                                    .stopLinux()
                            }
                        ) {

                            Text(
                                "Stop Linux"
                            )
                        }

                    } else {

                        Button(
                            onClick = {

                                linuxViewModel
                                    .startLinux()
                            }
                        ) {

                            Text(
                                "Start Linux"
                            )
                        }
                    }

                    Button(
                        onClick = {

                            linuxViewModel
                                .removeLinux()
                        }
                    ) {

                        Text(
                            "Remove Linux"
                        )
                    }
                }
            }

            else -> {

                Button(
                    onClick = {

                        linuxViewModel
                            .installLinux()
                    }
                ) {

                    Text(
                        "Install Ubuntu"
                    )
                }
            }
        }
    }
}

@Composable
private fun InstallationStepRow(
    step: String,
    currentStep: String,
    currentProgress: Float
) {

    val stepProgress =
        when (step) {

            "Preparing installation..." ->
                0.20f

            "Downloading packages..." ->
                0.40f

            "Installing packages..." ->
                0.65f

            "Configuring system..." ->
                0.85f

            "Cleaning up..." ->
                0.95f

            else ->
                1f
        }

    val statusSymbol =
        when {

            currentProgress > stepProgress ->
                "✓"

            currentStep == step ->
                "▶"

            else ->
                "○"
        }

    val statusColor =
        when {

            currentProgress > stepProgress ->
                Color(
                    0xFF00C853
                )

            currentStep == step ->
                Color(
                    0xFFFFC107
                )

            else ->
                MaterialTheme
                    .colorScheme
                    .onSurfaceVariant
        }

    Text(
        text =
            "$statusSymbol  $step",

        color =
            statusColor,

        style =
            MaterialTheme
                .typography
                .bodyMedium
    )
}