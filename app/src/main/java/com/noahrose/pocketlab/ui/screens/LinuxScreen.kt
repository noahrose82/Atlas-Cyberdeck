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
import androidx.compose.runtime.collectAsState
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
import com.noahrose.pocketlab.feature.linux.runtime.safety.LinuxRuntimeCircuitBreaker
import com.noahrose.pocketlab.feature.linux.runtime.safety.LinuxRuntimeSafetyMode

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

    val runtimeMessage by
    linuxViewModel.runtimeMessage

    val runtimeBusy by
    linuxViewModel.runtimeBusy

    /*
     * H4E — Linux controls observe the same runtime
     * safety state used by Terminal and the app shell.
     */
    val safetySnapshot by
    LinuxRuntimeCircuitBreaker
        .snapshotFlow
        .collectAsState()

    val safetyMode =
        safetySnapshot
            .mode

    val safeModeActive =
        safetyMode ==
                LinuxRuntimeSafetyMode.SAFE_MODE

    val recoveryModeActive =
        safetyMode ==
                LinuxRuntimeSafetyMode.RECOVERY_ARMED

    val normalModeActive =
        safetyMode ==
                LinuxRuntimeSafetyMode.NORMAL

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
         * ------------------------------------------------
         * H4E — RUNTIME SAFETY STATE
         * ------------------------------------------------
         *
         * SAFE_MODE blocks runtime startup.
         * RECOVERY_ARMED permits startup only for the
         * controlled recovery command path.
         */
        if (
            safetyMode !=
            LinuxRuntimeSafetyMode.NORMAL
        ) {

            val safetyColor =
                when (
                    safetyMode
                ) {

                    LinuxRuntimeSafetyMode.SAFE_MODE ->
                        Color(
                            0xFFFFD600
                        )

                    LinuxRuntimeSafetyMode.RECOVERY_ARMED ->
                        Color(
                            0xFFFFA000
                        )

                    LinuxRuntimeSafetyMode.NORMAL ->
                        MaterialTheme
                            .colorScheme
                            .onSurfaceVariant
                }

            Text(
                text =
                    when (
                        safetyMode
                    ) {

                        LinuxRuntimeSafetyMode.SAFE_MODE ->
                            "Runtime Safety: SAFE MODE"

                        LinuxRuntimeSafetyMode.RECOVERY_ARMED ->
                            "Runtime Safety: RECOVERY ARMED"

                        LinuxRuntimeSafetyMode.NORMAL ->
                            "Runtime Safety: NORMAL"
                    },

                color =
                    safetyColor,

                style =
                    MaterialTheme
                        .typography
                        .titleMedium
            )

            Text(
                text =
                    when (
                        safetyMode
                    ) {

                        LinuxRuntimeSafetyMode.SAFE_MODE ->
                            "Linux startup is blocked. Open Terminal and run 'safety recover' to begin controlled recovery."

                        LinuxRuntimeSafetyMode.RECOVERY_ARMED ->
                            "Linux may start for controlled recovery. Guest commands remain restricted to approved diagnostics and repair operations."

                        LinuxRuntimeSafetyMode.NORMAL ->
                            ""
                    },

                modifier =
                    Modifier
                        .fillMaxWidth()
                        .widthIn(
                            max = 500.dp
                        ),

                color =
                    safetyColor,

                style =
                    MaterialTheme
                        .typography
                        .bodyMedium
            )

            safetySnapshot
                .reason
                ?.let { reason ->

                    Text(
                        text =
                            "Safety reason: $reason",

                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .widthIn(
                                    max = 500.dp
                                ),

                        color =
                            safetyColor,

                        style =
                            MaterialTheme
                                .typography
                                .bodySmall
                    )
                }

            HorizontalDivider(
                modifier =
                    Modifier
                        .widthIn(
                            max = 500.dp
                        )
            )
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
                                    ).toInt()
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
                            step =
                                step,

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
                    when (
                        safetyMode
                    ) {

                        LinuxRuntimeSafetyMode.SAFE_MODE ->
                            "Ubuntu is installed, but runtime startup is blocked."

                        LinuxRuntimeSafetyMode.RECOVERY_ARMED ->
                            "Ubuntu is available for controlled recovery."

                        LinuxRuntimeSafetyMode.NORMAL ->
                            "Ubuntu is ready to use."
                    },

                color =
                    when (
                        safetyMode
                    ) {

                        LinuxRuntimeSafetyMode.SAFE_MODE ->
                            Color(
                                0xFFFFD600
                            )

                        LinuxRuntimeSafetyMode.RECOVERY_ARMED ->
                            Color(
                                0xFFFFA000
                            )

                        LinuxRuntimeSafetyMode.NORMAL ->
                            Color(
                                0xFF00C853
                            )
                    },

                style =
                    MaterialTheme
                        .typography
                        .titleMedium
            )

            Text(
                text =
                    when {

                        runtimeBusy &&
                                !installation.running ->
                            "Runtime: Starting..."

                        runtimeBusy &&
                                installation.running ->
                            "Runtime: Stopping..."

                        installation.running ->
                            "Runtime: Running"

                        else ->
                            "Runtime: Stopped"
                    },

                color =
                    when {

                        runtimeBusy ->
                            Color(
                                0xFFFFC107
                            )

                        installation.running ->
                            Color(
                                0xFF00C853
                            )

                        else ->
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
         * Runtime status/error feedback.
         */
        if (
            !runtimeMessage
                .isNullOrBlank()
        ) {

            val isError =
                runtimeMessage
                    ?.contains(
                        "failed",
                        ignoreCase = true
                    ) == true ||
                        runtimeMessage
                            ?.contains(
                                "could not",
                                ignoreCase = true
                            ) == true ||
                        runtimeMessage
                            ?.contains(
                                "exited",
                                ignoreCase = true
                            ) == true

            Text(
                text =
                    runtimeMessage
                        ?: "",

                modifier =
                    Modifier
                        .fillMaxWidth()
                        .widthIn(
                            max = 500.dp
                        ),

                color =
                    if (isError) {

                        MaterialTheme
                            .colorScheme
                            .error

                    } else {

                        MaterialTheme
                            .colorScheme
                            .onSurfaceVariant
                    },

                style =
                    MaterialTheme
                        .typography
                        .bodyMedium
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
                            },

                            enabled =
                                !runtimeBusy
                        ) {

                            Text(
                                if (runtimeBusy) {
                                    "Stopping..."
                                } else {
                                    "Stop Linux"
                                }
                            )
                        }

                    } else {

                        Button(
                            onClick = {

                                linuxViewModel
                                    .startLinux()
                            },

                            enabled =
                                !runtimeBusy &&
                                        !safeModeActive
                        ) {

                            Text(
                                when {

                                    runtimeBusy ->
                                        "Starting..."

                                    safeModeActive ->
                                        "Start Blocked — Safe Mode"

                                    recoveryModeActive ->
                                        "Start Recovery Linux"

                                    else ->
                                        "Start Linux"
                                }
                            )
                        }
                    }

                    Button(
                        onClick = {

                            linuxViewModel
                                .removeLinux()
                        },

                        enabled =
                            !runtimeBusy &&
                                    normalModeActive
                    ) {

                        Text(
                            if (normalModeActive) {
                                "Remove Linux"
                            } else {
                                "Remove Linux — Safety Locked"
                            }
                        )
                    }
                }
            }

            else -> {

                Button(
                    onClick = {

                        linuxViewModel
                            .installLinux()
                    },

                    enabled =
                        normalModeActive
                ) {

                    Text(
                        if (normalModeActive) {
                            "Install Ubuntu"
                        } else {
                            "Install Ubuntu — Safety Locked"
                        }
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

            currentProgress >
                    stepProgress ->
                "✓"

            currentStep ==
                    step ->
                "▶"

            else ->
                "○"
        }

    val statusColor =
        when {

            currentProgress >
                    stepProgress ->
                Color(
                    0xFF00C853
                )

            currentStep ==
                    step ->
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
