package com.noahrose.pocketlab.feature.terminal.script

import com.noahrose.pocketlab.feature.terminal.TerminalCommandProcessor

object ScriptEngine {

    fun execute(
        script: List<String>,
        output: MutableList<String>
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
                output = output
            )
        }
    }
}