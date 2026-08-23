package com.noahrose.pocketlab.feature.terminal.function

import com.noahrose.pocketlab.feature.terminal.TerminalCommandProcessor

object ShellFunctionEngine {

    fun execute(
        name: String,
        arguments: List<String>,
        output: MutableList<String>,
        showPrompt: Boolean = true
    ): Boolean {

        val commands =
            ShellFunctions.get(
                name
            ) ?: return false

        val expandedCommands =
            commands.map { command ->

                expandArguments(
                    command = command,
                    arguments = arguments
                )
            }

        expandedCommands.forEach { command ->

            TerminalCommandProcessor.process(
                command = command,
                output = output,
                recordHistory = false,
                showPrompt = false
            )
        }

        return true
    }

    private fun expandArguments(
        command: String,
        arguments: List<String>
    ): String {

        var expanded =
            command

        /*
         * $# = number of arguments
         *
         * Example:
         *
         * showargs one two three
         *
         * $# -> 3
         */
        expanded =
            expanded.replace(
                "\$#",
                arguments.size.toString()
            )

        /*
         * $@ = all arguments
         *
         * Example:
         *
         * showargs one two three
         *
         * $@ -> one two three
         */
        expanded =
            expanded.replace(
                "\$@",
                arguments.joinToString(" ")
            )

        /*
         * Positional arguments.
         *
         * $1 = first argument
         * $2 = second argument
         * ...
         *
         * Process higher positions first so
         * $1 cannot accidentally alter $10.
         */
        arguments.indices
            .reversed()
            .forEach { index ->

                val position =
                    index + 1

                expanded =
                    expanded.replace(
                        "\$$position",
                        arguments[index]
                    )
            }

        return expanded
    }
}