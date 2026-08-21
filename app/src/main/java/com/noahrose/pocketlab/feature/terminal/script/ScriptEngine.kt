package com.noahrose.pocketlab.feature.terminal.script

import com.noahrose.pocketlab.feature.terminal.TerminalCommandProcessor

object ScriptEngine {

    fun execute(
        script: List<String>,
        output: MutableList<String>,
        showPrompts: Boolean = true
    ) {

        script.forEach { line ->

            val command =
                line.trim()

            if (
                command.isBlank() ||
                command.startsWith("#")
            ) {
                return@forEach
            }

            TerminalCommandProcessor.process(
                command = command,
                output = output,
                recordHistory = false,
                showPrompt = showPrompts
            )
        }
    }
}