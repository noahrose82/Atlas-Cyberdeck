package com.noahrose.pocketlab.feature.terminal

import com.noahrose.pocketlab.feature.filesystem.VirtualFileSystem
import com.noahrose.pocketlab.feature.terminal.alias.CommandAliases
import com.noahrose.pocketlab.feature.terminal.chaining.CommandChainEngine
import com.noahrose.pocketlab.feature.terminal.commands.TextCommands
import com.noahrose.pocketlab.feature.terminal.dispatch.CommandDispatcher
import com.noahrose.pocketlab.feature.terminal.environment.VariableExpander
import com.noahrose.pocketlab.feature.terminal.execution.ExecutionStatus
import com.noahrose.pocketlab.feature.terminal.history.CommandHistory
import com.noahrose.pocketlab.feature.terminal.pipe.PipeEngine
import com.noahrose.pocketlab.feature.terminal.redirection.RedirectionEngine
import com.noahrose.pocketlab.feature.terminal.redirection.RedirectionParser
import com.noahrose.pocketlab.feature.terminal.redirection.RedirectionType
import com.noahrose.pocketlab.feature.terminal.wildcard.WildcardExpander

object TerminalCommandProcessor {

    fun process(
        command: String,
        output: MutableList<String>,
        recordHistory: Boolean = true,
        showPrompt: Boolean = true
    ) {

        val trimmedCommand =
            CommandAliases
                .resolve(command)
                .trim()

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
         *
         * !!
         * !2
         * !mkdir
         */
        if (
            recordHistory &&
            !trimmedCommand.startsWith("!")
        ) {

            CommandHistory.add(
                trimmedCommand
            )
        }

        /*
         * Display the shell prompt for normal
         * user-entered commands.
         *
         * Internal command execution can disable
         * prompt rendering.
         */
        if (showPrompt) {

            val currentPath =
                VirtualFileSystem.currentPath.value

            output.add(
                "atlas@cyberdeck:$currentPath$ $trimmedCommand"
            )
        }

        if (trimmedCommand.isBlank()) {
            return
        }

        /*
         * Every command begins with a successful
         * execution status.
         *
         * Individual handlers may replace this
         * with a non-zero exit code if execution fails.
         */
        ExecutionStatus.set(0)

        /*
         * Command chaining.
         *
         * Example:
         *
         * mkdir Area51 && cd Area51 && touch classified.txt
         *
         * Each chained command is passed back through
         * the normal command processor so shell features
         * such as redirection and expansion still work.
         *
         * CommandChainEngine checks ExecutionStatus
         * after each command and stops the chain when
         * a command reports failure.
         */
        if (
            CommandChainEngine.execute(
                command = expandedCommand
            ) { chainedCommand ->

                process(
                    command = chainedCommand,
                    output = output,
                    recordHistory = false
                )
            }
        ) {
            return
        }

        /*
         * Detect shell redirection before normal
         * command parsing and dispatch.
         */
        val redirection =
            RedirectionParser.parse(
                expandedCommand
            )

        /*
         * Output redirection.
         *
         * Examples:
         *
         * echo Area51 > alien.txt
         * echo Classified >> alien.txt
         *
         * The command on the left side is executed
         * silently and its output is written into
         * the target file.
         */
        if (
            redirection != null &&
            (
                    redirection.type ==
                            RedirectionType.OVERWRITE ||
                            redirection.type ==
                            RedirectionType.APPEND
                    )
        ) {

            val redirectedOutput =
                mutableListOf<String>()

            process(
                command = redirection.command,
                output = redirectedOutput,
                recordHistory = false,
                showPrompt = false
            )

            val handled =
                RedirectionEngine.handle(
                    command = expandedCommand,
                    commandOutput = redirectedOutput
                )

            if (!handled) {
                ExecutionStatus.set(1)
            }

            return
        }

        /*
         * Input redirection.
         *
         * Examples:
         *
         * sort < names.txt
         * uniq < names.txt
         * wc < names.txt
         *
         * File contents are passed directly to
         * text-processing commands as input.
         */
        if (
            redirection != null &&
            redirection.type ==
            RedirectionType.INPUT
        ) {

            val inputContent =
                VirtualFileSystem.readFile(
                    redirection.target
                )

            if (inputContent == null) {

                ExecutionStatus.set(1)

                output.add(
                    "${redirection.target}: No such file"
                )

                return
            }

            val inputParts =
                redirection.command.split(
                    Regex("\\s+"),
                    limit = 2
                )

            val inputCommandName =
                inputParts[0].lowercase()

            val handled =
                TextCommands.handleInput(
                    commandName = inputCommandName,
                    input = inputContent.lines(),
                    output = output
                )

            if (!handled) {

                ExecutionStatus.set(1)

                output.add(
                    "Input redirection is not supported for: $inputCommandName"
                )
            }

            return
        }

        /*
         * Normal command parsing.
         */
        val parts =
            expandedCommand.split(
                Regex("\\s+"),
                limit = 2
            )

        val commandName =
            parts[0].lowercase()

        /*
         * Pipe execution is handled before
         * normal command dispatch.
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
         * Normal commands are delegated to the
         * centralized command dispatcher.
         */
        if (
            CommandDispatcher.dispatch(
                commandName = commandName,
                parts = parts,
                output = output
            )
        ) {
            return
        }

        /*
         * History expansion remains here because
         * these commands recursively invoke process().
         */
        when {

            /*
             * Repeat the most recent command.
             */
            commandName == "!!" -> {

                val lastCommand =
                    CommandHistory.lastCommand()

                if (lastCommand == null) {

                    ExecutionStatus.set(1)

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

                        ExecutionStatus.set(1)

                        output.add(
                            "History numbers begin at 1."
                        )

                    } else {

                        val historyCommand =
                            CommandHistory.getCommand(
                                historyNumber - 1
                            )

                        if (historyCommand == null) {

                            ExecutionStatus.set(1)

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
                        CommandHistory
                            .findLastStartingWith(
                                historyReference
                            )

                    if (historyCommand == null) {

                        ExecutionStatus.set(1)

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

            /*
             * Unknown command.
             *
             * Exit code 127 is the conventional
             * shell code for "command not found."
             */
            else -> {

                ExecutionStatus.set(127)

                output.add(
                    "Command not found: $trimmedCommand"
                )
            }
        }
    }
}