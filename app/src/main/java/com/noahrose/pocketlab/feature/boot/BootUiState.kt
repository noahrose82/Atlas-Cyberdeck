package com.noahrose.pocketlab.feature.boot

import com.noahrose.pocketlab.feature.system.capability.DeviceCapabilities

data class BootUiState(

    val currentStep: BootStep =
        BootStep.STARTING,

    val progress: Float =
        0f,

    val isComplete: Boolean =
        false,

    val isDegraded: Boolean =
        false,

    val capabilities: DeviceCapabilities? =
        null
)

enum class BootStep(
    val displayText: String
) {

    STARTING(
        "Initializing Atlas Cyberdeck"
    ),

    WORKSPACE(
        "Loading workspace"
    ),

    DEVICE_PROFILE(
        "Loading device profile"
    ),

    CAPABILITIES(
        "Analyzing device capabilities"
    ),

    LINUX(
        "Loading Linux manager"
    ),

    TERMINAL(
        "Loading terminal"
    ),

    SSH(
        "Loading SSH services"
    ),

    FILE_SYSTEM(
        "Loading file system"
    ),

    READY(
        "System ready"
    ),

    DEGRADED(
        "System degraded - capability limitations detected"
    )
}