package com.noahrose.pocketlab.feature.dashboard

enum class SystemStatus(
    val label: String,
    val symbol: String
) {

    ONLINE(
        label = "Online",
        symbol = "●"
    ),

    READY(
        label = "Ready",
        symbol = "●"
    ),

    RUNNING(
        label = "Running",
        symbol = "●"
    ),

    STOPPED(
        label = "Stopped",
        symbol = "○"
    ),

    OFFLINE(
        label = "Offline",
        symbol = "○"
    ),

    NOT_INSTALLED(
        label = "Not Installed",
        symbol = "○"
    ),

    INSTALLING(
        label = "Installing",
        symbol = "◐"
    ),

    ERROR(
        label = "Error",
        symbol = "!"
    )
}