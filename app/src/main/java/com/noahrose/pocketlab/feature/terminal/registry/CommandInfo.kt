package com.noahrose.pocketlab.feature.terminal.registry

data class CommandInfo(
    val name: String,
    val description: String,
    val usage: String,
    val category: String
)