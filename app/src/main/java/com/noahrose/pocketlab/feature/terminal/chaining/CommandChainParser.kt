package com.noahrose.pocketlab.feature.terminal.chaining

object CommandChainParser {

    fun parse(
        command: String
    ): CommandChain? {

        if (!command.contains("&&")) {
            return null
        }

        val commands =
            command
                .split("&&")
                .map { part ->
                    part.trim()
                }
                .filter { part ->
                    part.isNotBlank()
                }

        if (commands.size < 2) {
            return null
        }

        return CommandChain(
            commands = commands
        )
    }
}