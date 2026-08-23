package com.noahrose.pocketlab.feature.dashboard

data class DashboardUiState(

    val linuxInstalled: Boolean = false,

    val linuxStatus: SystemStatus =
        SystemStatus.NOT_INSTALLED,

    val terminalStatus: SystemStatus =
        SystemStatus.READY,

    val packageCount: Int = 0,

    val storageUsed: String =
        "0 MB",

    val architecture: String =
        "ARM64"
)