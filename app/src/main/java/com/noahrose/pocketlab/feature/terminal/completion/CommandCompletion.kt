package com.noahrose.pocketlab.feature.terminal.completion

import com.noahrose.pocketlab.feature.terminal.registry.CommandRegistry

object CommandCompletion {

    fun complete(
        input: String
    ): String? {

        val prefix =
            input.trim()

        if (prefix.isBlank()) {
            return null
        }

        val matches =
            CommandRegistry
                .getAll()
                .map { command ->
                    command.name
                }
                .filter { commandName ->

                    commandName.startsWith(
                        prefix,
                        ignoreCase = true
                    )
                }

        return when {

            matches.isEmpty() ->
                null

            matches.size == 1 ->
                matches.first()

            else ->
                null
        }
    }
}