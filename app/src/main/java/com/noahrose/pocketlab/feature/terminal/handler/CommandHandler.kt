package com.noahrose.pocketlab.feature.terminal.handler

interface CommandHandler {

    fun handle(
        commandName: String,
        parts: List<String>,
        output: MutableList<String>
    ): Boolean
}