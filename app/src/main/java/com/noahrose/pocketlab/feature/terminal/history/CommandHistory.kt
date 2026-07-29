package com.noahrose.pocketlab.feature.terminal.history

object CommandHistory {

    private val commands = mutableListOf<String>()

    fun add(command: String) {

        val text = command.trim()

        if (text.isNotBlank()) {
            commands.add(text)
        }
    }

    fun getHistory(): List<String> =
        commands.toList()

    fun lastCommand(): String? {

        return commands.lastOrNull()
    }

    fun findLastStartingWith(prefix: String): String? {

        return commands
            .asReversed()
            .firstOrNull {
                it.startsWith(
                    prefix,
                    ignoreCase = true
                )
            }
    }

    fun getCommand(index: Int): String? {

        return commands.getOrNull(index)
    }

    fun clear() {
        commands.clear()
    }

    fun size(): Int =
        commands.size
}