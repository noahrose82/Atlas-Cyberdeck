package com.noahrose.pocketlab.feature.terminal.plugin

object CorePlugin : TerminalPlugin {

    override val info =
        PluginInfo(
            name = "Core",
            version = "1.0.0",
            author = "Atlas Labs",
            description = "Core Atlas Cyberdeck terminal services."
        )

    override fun initialize() {
        // Core plugin initialization.
    }
}