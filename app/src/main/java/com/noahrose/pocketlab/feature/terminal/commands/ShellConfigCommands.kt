package com.noahrose.pocketlab.feature.terminal.commands

import com.noahrose.pocketlab.feature.terminal.alias.CommandAliases
import com.noahrose.pocketlab.feature.terminal.environment.EnvironmentVariables
import com.noahrose.pocketlab.feature.terminal.execution.ExecutionStatus
import com.noahrose.pocketlab.feature.terminal.handler.CommandHandler

object ShellConfigCommands : CommandHandler {

    override fun handle(
        commandName: String,
        parts: List<String>,
        output: MutableList<String>
    ): Boolean {

        when (commandName) {

            "alias" -> {

                if (
                    parts.size < 2 ||
                    parts[1].isBlank()
                ) {

                    val aliases =
                        CommandAliases
                            .getAllAliases()
                            .toSortedMap()

                    aliases.forEach { (name, command) ->

                        output.add(
                            "$name='$command'"
                        )
                    }

                    ExecutionStatus.set(0)

                    return true
                }

                val input =
                    parts[1].trim()

                val equalsIndex =
                    input.indexOf('=')

                if (equalsIndex <= 0) {

                    ExecutionStatus.set(2)

                    output.add(
                        "Usage: alias <name>=<command>"
                    )

                    return true
                }

                val name =
                    input
                        .substring(
                            0,
                            equalsIndex
                        )
                        .trim()

                var aliasCommand =
                    input
                        .substring(
                            equalsIndex + 1
                        )
                        .trim()

                aliasCommand =
                    removeMatchingQuotes(
                        aliasCommand
                    )

                val created =
                    CommandAliases.setAlias(
                        name = name,
                        command = aliasCommand
                    )

                if (created) {

                    ExecutionStatus.set(0)

                } else {

                    ExecutionStatus.set(2)

                    output.add(
                        "alias: invalid alias"
                    )
                }

                return true
            }

            "unalias" -> {

                if (
                    parts.size < 2 ||
                    parts[1].isBlank()
                ) {

                    ExecutionStatus.set(2)

                    output.add(
                        "Usage: unalias <name>"
                    )

                    return true
                }

                val name =
                    parts[1].trim()

                val removed =
                    CommandAliases.removeAlias(
                        name
                    )

                if (removed) {

                    ExecutionStatus.set(0)

                } else {

                    ExecutionStatus.set(1)

                    output.add(
                        "unalias: '$name': not found"
                    )
                }

                return true
            }

            "export" -> {

                if (
                    parts.size < 2 ||
                    parts[1].isBlank()
                ) {

                    val variables =
                        EnvironmentVariables
                            .getUserVariables()
                            .toSortedMap()

                    variables.forEach { (name, value) ->

                        output.add(
                            "$name=$value"
                        )
                    }

                    ExecutionStatus.set(0)

                    return true
                }

                val input =
                    parts[1].trim()

                val equalsIndex =
                    input.indexOf('=')

                if (equalsIndex <= 0) {

                    ExecutionStatus.set(2)

                    output.add(
                        "Usage: export <NAME>=<value>"
                    )

                    return true
                }

                val name =
                    input
                        .substring(
                            0,
                            equalsIndex
                        )
                        .trim()

                var value =
                    input
                        .substring(
                            equalsIndex + 1
                        )
                        .trim()

                value =
                    removeMatchingQuotes(
                        value
                    )

                val created =
                    EnvironmentVariables.set(
                        name = name,
                        value = value
                    )

                if (created) {

                    ExecutionStatus.set(0)

                } else {

                    ExecutionStatus.set(2)

                    output.add(
                        "export: invalid variable name"
                    )
                }

                return true
            }

            "unset" -> {

                if (
                    parts.size < 2 ||
                    parts[1].isBlank()
                ) {

                    ExecutionStatus.set(2)

                    output.add(
                        "Usage: unset <NAME>"
                    )

                    return true
                }

                val name =
                    parts[1].trim()

                val removed =
                    EnvironmentVariables.remove(
                        name
                    )

                if (removed) {

                    ExecutionStatus.set(0)

                } else {

                    ExecutionStatus.set(1)

                    output.add(
                        "unset: '$name': not found"
                    )
                }

                return true
            }
        }

        return false
    }

    private fun removeMatchingQuotes(
        value: String
    ): String {

        if (value.length < 2) {
            return value
        }

        val first =
            value.first()

        val last =
            value.last()

        return if (
            (
                    first == '"' &&
                            last == '"'
                    ) ||
            (
                    first == '\'' &&
                            last == '\''
                    )
        ) {

            value.substring(
                1,
                value.length - 1
            )

        } else {

            value
        }
    }
}