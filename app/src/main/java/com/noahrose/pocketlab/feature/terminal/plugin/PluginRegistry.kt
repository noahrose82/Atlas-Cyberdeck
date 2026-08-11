package com.noahrose.pocketlab.feature.terminal.plugin

object PluginRegistry {

    private val plugins =
        mutableListOf<TerminalPlugin>()

    init {
        register(CorePlugin)
    }

    fun register(
        plugin: TerminalPlugin
    ) {

        if (!plugins.contains(plugin)) {
            plugins.add(plugin)
            plugin.initialize()
        }
    }

    fun getAll(): List<TerminalPlugin> {

        return plugins.toList()
    }
}