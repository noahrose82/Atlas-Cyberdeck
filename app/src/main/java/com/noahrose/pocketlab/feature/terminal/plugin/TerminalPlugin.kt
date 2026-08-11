package com.noahrose.pocketlab.feature.terminal.plugin

interface TerminalPlugin {

    val info: PluginInfo

    fun initialize()
}