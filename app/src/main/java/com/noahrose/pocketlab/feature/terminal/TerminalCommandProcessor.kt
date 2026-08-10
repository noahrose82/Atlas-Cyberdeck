package com.noahrose.pocketlab.feature.terminal

import com.noahrose.pocketlab.feature.filesystem.VirtualFileSystem
import com.noahrose.pocketlab.feature.terminal.alias.CommandAliases
import com.noahrose.pocketlab.feature.terminal.commands.DirectoryCommands
import com.noahrose.pocketlab.feature.terminal.commands.FileCommands
import com.noahrose.pocketlab.feature.terminal.commands.TextCommands
import com.noahrose.pocketlab.feature.terminal.commands.UtilityCommands
import com.noahrose.pocketlab.feature.terminal.environment.VariableExpander
import com.noahrose.pocketlab.feature.terminal.history.CommandHistory
import com.noahrose.pocketlab.feature.terminal.pipe.PipeEngine
import com.noahrose.pocketlab.feature.terminal.wildcard.WildcardExpander

object TerminalCommandProcessor {

    fun process(
        command: String,
        output: MutableList<String>
    ) {

        val trimmedCommand =
            CommandAliases.resolve(command).trim()

        val expandedCommand =
            WildcardExpander.expand(
                VariableExpander.expand(
                    trimmedCommand
                )
            )

        /*
         * History expansion commands are not stored directly.
         *
         * Examples:
         * !!
         * !2
         * !mkdir
         */
        if (!trimmedCommand.startsWith("!")) {
            CommandHistory.add(trimmedCommand)
        }

        val currentPath =
            VirtualFileSystem.currentPath.value

        output.add(
            "atlas@cyberdeck:$currentPath$ $trimmedCommand"
        )

        if (trimmedCommand.isBlank()) {
            return
        }

        val parts =
            expandedCommand.split(
                Regex("\\s+"),
                limit = 2
            )

        val commandName =
            parts[0].lowercase()

        /*
         * Pipe execution is handled before normal
         * command dispatch.
         */
        if (
            PipeEngine.handle(
                command = expandedCommand,
                output = output
            )
        ) {
            return
        }

        /*
         * Utility commands.
         */
        if (
            UtilityCommands.handle(
                commandName = commandName,
                output = output
            )
        ) {
            return
        }

        /*
         * File commands.
         */
        if (
            FileCommands.handle(
                commandName = commandName,
                parts = parts,
                output = output
            )
        ) {
            return
        }

        /*
         * Directory commands.
         */
        if (
            DirectoryCommands.handle(
                commandName = commandName,
                parts = parts,
                output = output
            )
        ) {
            return
        }

        /*
         * Text-processing commands.
         */
        if (
            TextCommands.handle(
                commandName = commandName,
                parts = parts,
                output = output
            )
        ) {
            return
        }

        /*
         * History expansion commands remain here
         * because they recursively invoke process().
         */
        when {

            /*
             * Repeat the most recent command.
             */
            commandName == "!!" -> {

                val lastCommand =
                    CommandHistory.lastCommand()

                if (lastCommand == null) {

                    output.add(
                        "No previous command found."
                    )

                } else {

                    output.add(
                        "Executing: $lastCommand"
                    )

                    process(
                        command = lastCommand,
                        output = output
                    )
                }
            }

            /*
             * Execute a command by history number
             * or command prefix.
             *
             * Examples:
             *
             * !2
             * !mkdir
             */
            commandName.startsWith("!") -> {

                val historyReference =
                    commandName.drop(1)

                val historyNumber =
                    historyReference.toIntOrNull()

                if (historyNumber != null) {

                    if (historyNumber < 1) {

                        output.add(
                            "History numbers begin at 1."
                        )

                    } else {

                        val historyCommand =
                            CommandHistory.getCommand(
                                historyNumber - 1
                            )

                        if (historyCommand == null) {

                            output.add(
                                "No such history entry: $historyNumber"
                            )

                        } else {

                            output.add(
                                "Executing: $historyCommand"
                            )

                            process(
                                command = historyCommand,
                                output = output
                            )
                        }
                    }

                } else {

                    val historyCommand =
                        CommandHistory.findLastStartingWith(
                            historyReference
                        )

                    if (historyCommand == null) {

                        output.add(
                            "No command starts with '$historyReference'"
                        )

                    } else {

                        output.add(
                            "Executing: $historyCommand"
                        )

                        process(
                            command = historyCommand,
                            output = output
                        )
                    }
                }
            }

            else -> {

                output.add(
                    "Command not found: $trimmedCommand"
                )
            }
        }
    }
}