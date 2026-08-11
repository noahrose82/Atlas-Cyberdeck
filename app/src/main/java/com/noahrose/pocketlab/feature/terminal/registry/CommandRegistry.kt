package com.noahrose.pocketlab.feature.terminal.registry

object CommandRegistry {

    private val commands = mutableListOf<CommandInfo>()

    fun register(command: CommandInfo) {

        if (find(command.name) == null) {
            commands.add(command)
        }
    }

    fun getAll(): List<CommandInfo> {

        return commands.sortedBy { it.name }
    }

    fun find(name: String): CommandInfo? {

        return commands.firstOrNull {

            it.name.equals(
                name,
                ignoreCase = true
            )
        }
    }
}