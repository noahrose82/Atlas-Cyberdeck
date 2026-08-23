package com.noahrose.pocketlab.feature.terminal.commands

import com.noahrose.pocketlab.feature.terminal.execution.ExecutionStatus
import com.noahrose.pocketlab.feature.terminal.function.ShellFunctions
import com.noahrose.pocketlab.feature.terminal.handler.CommandHandler

object FunctionCommands : CommandHandler {

    override fun handle(
        commandName: String,
        parts: List<String>,
        output: MutableList<String>
    ): Boolean {

        when (commandName) {

            "functions" -> {

                val functions =
                    ShellFunctions
                        .getAll()
                        .toSortedMap()

                if (functions.isEmpty()) {

                    output.add(
                        "No shell functions defined."
                    )

                } else {

                    functions.forEach { (name, commands) ->

                        output.add(
                            "$name {"
                        )

                        commands.forEach { command ->

                            output.add(
                                "  $command"
                            )
                        }

                        output.add(
                            "}"
                        )
                    }
                }

                ExecutionStatus.set(0)

                return true
            }

            "unfunction" -> {

                if (
                    parts.size < 2 ||
                    parts[1].isBlank()
                ) {

                    ExecutionStatus.set(2)

                    output.add(
                        "Usage: unfunction <name>"
                    )

                    return true
                }

                val name =
                    parts[1].trim()

                if (
                    ShellFunctions.remove(
                        name
                    )
                ) {

                    ExecutionStatus.set(0)

                } else {

                    ExecutionStatus.set(1)

                    output.add(
                        "unfunction: '$name': not found"
                    )
                }

                return true
            }
        }

        return false
    }
}