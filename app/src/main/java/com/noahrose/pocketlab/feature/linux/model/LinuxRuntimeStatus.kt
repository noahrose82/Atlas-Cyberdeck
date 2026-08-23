package com.noahrose.pocketlab.feature.linux.model

enum class LinuxRuntimeStatus(
    val label: String
) {
    NOT_INSTALLED(
        label = "NOT INSTALLED"
    ),

    INSTALLING(
        label = "INSTALLING"
    ),

    STOPPED(
        label = "STOPPED"
    ),

    RUNNING(
        label = "RUNNING"
    )
}

fun LinuxInstallation.runtimeStatus(): LinuxRuntimeStatus {

    return when {

        isInstalling ->
            LinuxRuntimeStatus.INSTALLING

        !installed ->
            LinuxRuntimeStatus.NOT_INSTALLED

        running ->
            LinuxRuntimeStatus.RUNNING

        else ->
            LinuxRuntimeStatus.STOPPED
    }
}