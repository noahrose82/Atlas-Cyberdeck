package com.noahrose.pocketlab.feature.terminal

import com.noahrose.pocketlab.feature.filesystem.VirtualFileSystem
import com.noahrose.pocketlab.feature.linux.runtime.command.LinuxGuestCommandResult
import com.noahrose.pocketlab.feature.linux.runtime.command.LinuxShellMode
import com.noahrose.pocketlab.feature.terminal.alias.CommandAliases
import com.noahrose.pocketlab.feature.terminal.chaining.ConditionalChainEngine
import com.noahrose.pocketlab.feature.terminal.commands.TextCommands
import com.noahrose.pocketlab.feature.terminal.dispatch.CommandDispatcher
import com.noahrose.pocketlab.feature.terminal.environment.VariableExpander
import com.noahrose.pocketlab.feature.terminal.execution.ExecutionStatus
import com.noahrose.pocketlab.feature.terminal.function.ShellFunctionEngine
import com.noahrose.pocketlab.feature.terminal.history.CommandHistory
import com.noahrose.pocketlab.feature.terminal.parsing.CommandTokenizer
import com.noahrose.pocketlab.feature.terminal.pipe.PipeEngine
import com.noahrose.pocketlab.feature.terminal.redirection.RedirectionEngine
import com.noahrose.pocketlab.feature.terminal.redirection.RedirectionParser
import com.noahrose.pocketlab.feature.terminal.redirection.RedirectionType
import com.noahrose.pocketlab.feature.terminal.sequential.SequentialCommandEngine
import com.noahrose.pocketlab.feature.terminal.wildcard.WildcardExpander

object TerminalCommandProcessor {

    fun process(
        command: String,
        output: MutableList<String>,
        recordHistory: Boolean = true,
        showPrompt: Boolean = true
    ) {

        /*
         * ------------------------------------------------
         * REAL UBUNTU SHELL MODE
         * ------------------------------------------------
         *
         * This must run before the Atlas parser.
         *
         * Ubuntu commands must receive their own:
         *
         * variables
         * pipes
         * redirection
         * &&
         * ||
         * wildcards
         * quoting
         *
         * without Atlas rewriting them first.
         */
        if (
            LinuxShellMode
                .isActive()
        ) {

            val guestCommand =
                command.trim()

            if (
                recordHistory &&
                guestCommand.isNotBlank()
            ) {

                CommandHistory
                    .add(
                        guestCommand
                    )
            }

            if (showPrompt) {

                output.add(
                    "${LinuxShellMode.getPrompt()} $guestCommand"
                )
            }

            if (
                guestCommand.isBlank()
            ) {

                return
            }

            /*
             * "exit" switches back to Atlas.
             *
             * It intentionally does NOT get sent
             * to Ubuntu because that would kill
             * the persistent guest shell process.
             */
            if (
                guestCommand.equals(
                    "exit",
                    ignoreCase = true
                )
            ) {

                LinuxShellMode
                    .exit()

                ExecutionStatus
                    .set(
                        0
                    )

                output.add(
                    "Returned to Atlas shell."
                )

                return
            }

            when (
                val result =
                    LinuxShellMode
                        .execute(
                            guestCommand
                        )
            ) {

                is LinuxGuestCommandResult.Success -> {

                    ExecutionStatus
                        .set(
                            result.exitCode
                        )

                    if (
                        result.output
                            .isNotBlank()
                    ) {

                        result.output
                            .lines()
                            .forEach { line ->

                                output.add(
                                    line
                                )
                            }
                    }

                    if (
                        result.errorOutput
                            .isNotBlank()
                    ) {

                        result.errorOutput
                            .lines()
                            .forEach { line ->

                                output.add(
                                    line
                                )
                            }
                    }
                }

                is LinuxGuestCommandResult.Failure -> {

                    ExecutionStatus
                        .set(
                            1
                        )

                    output.add(
                        "linux: ${result.message}"
                    )

                    if (
                        result.output
                            .isNotBlank()
                    ) {

                        result.output
                            .lines()
                            .forEach { line ->

                                output.add(
                                    line
                                )
                            }
                    }

                    if (
                        result.errorOutput
                            .isNotBlank()
                    ) {

                        result.errorOutput
                            .lines()
                            .forEach { line ->

                                output.add(
                                    line
                                )
                            }
                    }
                }
            }

            return
        }

        /*
         * ------------------------------------------------
         * ATLAS SHELL
         * ------------------------------------------------
         */
        val trimmedCommand =
            CommandAliases
                .resolve(
                    command
                )
                .trim()

        val expandedCommand =
            WildcardExpander.expand(
                VariableExpander.expand(
                    trimmedCommand
                )
            )

        /*
         * Store normal user commands.
         */
        if (
            recordHistory &&
            trimmedCommand.isNotBlank() &&
            !trimmedCommand.startsWith("!")
        ) {

            CommandHistory.add(
                trimmedCommand
            )
        }

        /*
         * Sequential execution.
         *
         * commandA ; commandB
         */
        if (
            SequentialCommandEngine.execute(
                command =
                    expandedCommand
            ) { sequentialCommand ->

                process(
                    command =
                        sequentialCommand,

                    output =
                        output,

                    recordHistory =
                        false,

                    showPrompt =
                        showPrompt
                )
            }
        ) {

            return
        }

        /*
         * Conditional chaining.
         *
         * commandA && commandB
         * commandA || commandB
         */
        if (
            ConditionalChainEngine.execute(
                command =
                    expandedCommand
            ) { chainedCommand ->

                process(
                    command =
                        chainedCommand,

                    output =
                        output,

                    recordHistory =
                        false,

                    showPrompt =
                        showPrompt
                )
            }
        ) {

            return
        }

        /*
         * Atlas prompt.
         */
        if (showPrompt) {

            val currentPath =
                VirtualFileSystem
                    .currentPath
                    .value

            output.add(
                "atlas@cyberdeck:$currentPath$ $trimmedCommand"
            )
        }

        if (
            trimmedCommand.isBlank()
        ) {

            return
        }

        /*
         * Commands begin successful unless a
         * handler reports otherwise.
         */
        ExecutionStatus
            .set(
                0
            )

        /*
         * Detect shell redirection.
         */
        val redirection =
            RedirectionParser
                .parse(
                    expandedCommand
                )

        /*
         * Output redirection.
         *
         * >
         * >>
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
                command =
                    redirection.command,

                output =
                    redirectedOutput,

                recordHistory =
                    false,

                showPrompt =
                    false
            )

            val handled =
                RedirectionEngine
                    .handle(
                        command =
                            expandedCommand,

                        commandOutput =
                            redirectedOutput
                    )

            if (!handled) {

                ExecutionStatus
                    .set(
                        1
                    )
            }

            return
        }

        /*
         * Input redirection.
         *
         * <
         */
        if (
            redirection != null &&
            redirection.type ==
            RedirectionType.INPUT
        ) {

            val inputContent =
                VirtualFileSystem
                    .readFile(
                        redirection.target
                    )

            if (
                inputContent == null
            ) {

                ExecutionStatus
                    .set(
                        1
                    )

                output.add(
                    "${redirection.target}: No such file"
                )

                return
            }

            val inputTokens =
                CommandTokenizer
                    .tokenize(
                        redirection.command
                    )

            if (
                inputTokens.isEmpty()
            ) {

                ExecutionStatus
                    .set(
                        1
                    )

                return
            }

            val inputCommandName =
                inputTokens
                    .first()
                    .lowercase()

            val handled =
                TextCommands
                    .handleInput(
                        commandName =
                            inputCommandName,

                        input =
                            inputContent.lines(),

                        output =
                            output
                    )

            if (!handled) {

                ExecutionStatus
                    .set(
                        1
                    )

                output.add(
                    "Input redirection is not supported for: $inputCommandName"
                )
            }

            return
        }

        /*
         * Quote-aware tokenization.
         */
        val tokens =
            CommandTokenizer
                .tokenizeOrNull(
                    expandedCommand
                )

        if (
            tokens == null
        ) {

            ExecutionStatus
                .set(
                    2
                )

            output.add(
                "syntax error: unmatched quote"
            )

            return
        }

        if (
            tokens.isEmpty()
        ) {

            return
        }

        val commandName =
            tokens
                .first()
                .lowercase()

        /*
         * cp and mv require individual arguments.
         *
         * Existing handlers otherwise expect:
         *
         * parts[0] = command
         * parts[1] = remaining argument text
         */
        val parts =
            when (commandName) {

                "cp",
                "mv" -> {

                    tokens
                }

                else -> {

                    if (
                        tokens.size == 1
                    ) {

                        listOf(
                            commandName
                        )

                    } else {

                        listOf(
                            commandName,

                            tokens
                                .drop(1)
                                .joinToString(
                                    " "
                                )
                        )
                    }
                }
            }

        /*
         * Pipe engine.
         */
        if (
            PipeEngine
                .handle(
                    command =
                        expandedCommand,

                    output =
                        output
                )
        ) {

            return
        }

        /*
 * Atlas shell functions.
 *
 * Function arguments are preserved so:
 *
 * myFunction arg1 arg2
 *
 * receives:
 *
 * [arg1, arg2]
 */
        if (
            ShellFunctionEngine
                .execute(
                    name =
                        commandName,

                    arguments =
                        tokens.drop(1),

                    output =
                        output,

                    showPrompt =
                        showPrompt
                )
        ) {

            ExecutionStatus
                .set(
                    0
                )

            return
        }

        /*
         * Normal command dispatch.
         */
        if (
            CommandDispatcher
                .dispatch(
                    commandName =
                        commandName,

                    parts =
                        parts,

                    output =
                        output
                )
        ) {

            return
        }

        /*
         * History expansion.
         */
        when {

            commandName ==
                    "!!" -> {

                val lastCommand =
                    CommandHistory
                        .lastCommand()

                if (
                    lastCommand == null
                ) {

                    ExecutionStatus
                        .set(
                            1
                        )

                    output.add(
                        "No previous command found."
                    )

                } else {

                    output.add(
                        "Executing: $lastCommand"
                    )

                    process(
                        command =
                            lastCommand,

                        output =
                            output,

                        recordHistory =
                            false,

                        showPrompt =
                            showPrompt
                    )
                }
            }

            commandName
                .startsWith(
                    "!"
                ) -> {

                val historyReference =
                    commandName
                        .drop(
                            1
                        )

                val historyNumber =
                    historyReference
                        .toIntOrNull()

                if (
                    historyNumber != null
                ) {

                    if (
                        historyNumber < 1
                    ) {

                        ExecutionStatus
                            .set(
                                1
                            )

                        output.add(
                            "History numbers begin at 1."
                        )

                    } else {

                        val historyCommand =
                            CommandHistory
                                .getCommand(
                                    historyNumber -
                                            1
                                )

                        if (
                            historyCommand == null
                        ) {

                            ExecutionStatus
                                .set(
                                    1
                                )

                            output.add(
                                "No such history entry: $historyNumber"
                            )

                        } else {

                            output.add(
                                "Executing: $historyCommand"
                            )

                            process(
                                command =
                                    historyCommand,

                                output =
                                    output,

                                recordHistory =
                                    false,

                                showPrompt =
                                    showPrompt
                            )
                        }
                    }

                } else {

                    val historyCommand =
                        CommandHistory
                            .findLastStartingWith(
                                historyReference
                            )

                    if (
                        historyCommand == null
                    ) {

                        ExecutionStatus
                            .set(
                                1
                            )

                        output.add(
                            "No command starts with '$historyReference'"
                        )

                    } else {

                        output.add(
                            "Executing: $historyCommand"
                        )

                        process(
                            command =
                                historyCommand,

                            output =
                                output,

                            recordHistory =
                                false,

                            showPrompt =
                                showPrompt
                        )
                    }
                }
            }

            else -> {

                ExecutionStatus
                    .set(
                        127
                    )

                output.add(
                    "Command not found: $trimmedCommand"
                )
            }
        }
    }
}