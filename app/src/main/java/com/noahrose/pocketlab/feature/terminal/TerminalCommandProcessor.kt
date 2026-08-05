package com.noahrose.pocketlab.feature.terminal

import com.noahrose.pocketlab.feature.filesystem.VirtualFileSystem
import com.noahrose.pocketlab.feature.terminal.alias.CommandAliases
import com.noahrose.pocketlab.feature.terminal.commands.FileCommands
import com.noahrose.pocketlab.feature.terminal.commands.UtilityCommands
import com.noahrose.pocketlab.feature.terminal.environment.VariableExpander
import com.noahrose.pocketlab.feature.terminal.history.CommandHistory
import com.noahrose.pocketlab.feature.terminal.wildcard.WildcardExpander
import com.noahrose.pocketlab.feature.terminal.commands.DirectoryCommands

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

        if (expandedCommand.contains("|")) {

            val pipeParts =
                expandedCommand.split(
                    "|",
                    limit = 2
                )

            val leftCommand =
                pipeParts[0].trim()

            val rightCommand =
                pipeParts[1].trim()

            if (
                leftCommand.isBlank() ||
                rightCommand.isBlank()
            ) {
                output.add(
                    "Usage: <command> | <command>"
                )
                return
            }

            val leftParts =
                leftCommand.split(
                    Regex("\\s+")
                )

            val rightParts =
                rightCommand.split(
                    Regex("\\s+"),
                    limit = 2
                )

            val leftCommandName =
                leftParts[0].lowercase()

            val rightCommandName =
                rightParts[0].lowercase()

            val pipedLines =
                mutableListOf<String>()

            when (leftCommandName) {

                "cat" -> {

                    if (leftParts.size < 2) {

                        output.add(
                            "Usage: cat <filename> | <command>"
                        )
                        return
                    }

                    val fileNames =
                        leftParts.drop(1)

                    fileNames.forEach { fileName ->

                        val content =
                            VirtualFileSystem.readFile(
                                fileName
                            )

                        if (content == null) {

                            output.add(
                                "cat: $fileName: No such file"
                            )

                        } else {

                            pipedLines.addAll(
                                content.lines()
                            )
                        }
                    }
                }

                "echo" -> {

                    val echoText =
                        leftCommand
                            .removePrefix(leftParts[0])
                            .trim()

                    pipedLines.add(
                        echoText
                    )
                }

                else -> {

                    output.add(
                        "pipe: unsupported input command: $leftCommandName"
                    )
                    return
                }
            }

            when (rightCommandName) {

                "grep" -> {

                    if (
                        rightParts.size < 2 ||
                        rightParts[1].isBlank()
                    ) {

                        output.add(
                            "Usage: <command> | grep <text>"
                        )
                        return
                    }

                    val searchText =
                        rightParts[1].trim()

                    val matches =
                        pipedLines.filter { line ->

                            line.contains(
                                searchText
                            )
                        }

                    if (matches.isEmpty()) {

                        output.add(
                            "No matches found."
                        )

                    } else {

                        matches.forEach { line ->

                            output.add(line)
                        }
                    }
                }

                "head" -> {

                    pipedLines
                        .take(3)
                        .forEach { line ->

                            output.add(line)
                        }
                }

                "tail" -> {

                    pipedLines
                        .takeLast(3)
                        .forEach { line ->

                            output.add(line)
                        }
                }

                else -> {

                    output.add(
                        "pipe: unsupported output command: $rightCommandName"
                    )
                }
            }

            return
        }

        if (
            UtilityCommands.handle(
                commandName = commandName,
                output = output
            )
        ) {
            return
        }

        if (
            FileCommands.handle(
                commandName = commandName,
                parts = parts,
                output = output
            )
        ) {
            return
        }

        if (
            DirectoryCommands.handle(
                commandName = commandName,
                parts = parts,
                output = output
            )
        ) {
            return
        }

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
                        lastCommand,
                        output
                    )
                }
            }

            /*
             * Execute a command by its history number.
             *
             * Example:
             * !2
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
                                historyCommand,
                                output
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
                            historyCommand,
                            output
                        )
                    }
                }
            }

            commandName == "ls" -> {

                val entries =
                    VirtualFileSystem
                        .currentEntries
                        .value

                if (
                    parts.size < 2 ||
                    parts[1].isBlank()
                ) {

                    if (entries.isEmpty()) {

                        output.add("<empty>")

                    } else {

                        entries.forEach { entry ->

                            if (entry.isDirectory) {

                                output.add(
                                    "${entry.name}/"
                                )

                            } else {

                                output.add(
                                    entry.name
                                )
                            }
                        }
                    }

                } else {

                    val requestedNames =
                        parts[1]
                            .trim()
                            .split(
                                Regex("\\s+")
                            )

                    val matchingEntries =
                        requestedNames.mapNotNull { requestedName ->

                            entries.find { entry ->
                                entry.name == requestedName
                            }
                        }

                    if (matchingEntries.isEmpty()) {

                        output.add(
                            "ls: no matching files found"
                        )

                    } else {

                        matchingEntries.forEach { entry ->

                            if (entry.isDirectory) {

                                output.add(
                                    "${entry.name}/"
                                )

                            } else {

                                output.add(
                                    entry.name
                                )
                            }
                        }
                    }
                }
            }

            commandName == "tree" -> {

                val treeLines =
                    VirtualFileSystem.buildTree()

                treeLines.forEach { line ->

                    output.add(line)
                }
            }

            commandName == "find" -> {

                if (
                    parts.size < 2 ||
                    parts[1].isBlank()
                ) {

                    output.add(
                        "Usage: find <name>"
                    )

                } else {

                    val targetName =
                        parts[1].trim()

                    val results =
                        VirtualFileSystem.find(
                            targetName
                        )

                    if (results.isEmpty()) {

                        output.add(
                            "find: '$targetName': No matches found"
                        )

                    } else {

                        results.forEach { path ->

                            output.add(path)
                        }
                    }
                }
            }


            commandName == "mkdir" -> {

                if (
                    parts.size < 2 ||
                    parts[1].isBlank()
                ) {

                    output.add(
                        "Usage: mkdir <directory>"
                    )

                } else {

                    val directoryName =
                        parts[1].trim()

                    val created =
                        VirtualFileSystem
                            .createDirectory(
                                directoryName
                            )

                    if (created) {

                        output.add(
                            "Directory created: $directoryName"
                        )

                    } else {

                        output.add(
                            "mkdir: '$directoryName' already exists"
                        )
                    }
                }
            }

            commandName == "cd" -> {

                if (
                    parts.size < 2 ||
                    parts[1].isBlank()
                ) {

                    output.add(
                        "Usage: cd <directory>"
                    )

                } else {

                    val destination =
                        parts[1].trim()

                    val changed =
                        VirtualFileSystem
                            .changeDirectory(
                                destination
                            )

                    if (!changed) {

                        output.add(
                            "cd: $destination: No such directory"
                        )
                    }
                }
            }


            commandName == "rmdir" -> {

                if (
                    parts.size < 2 ||
                    parts[1].isBlank()
                ) {

                    output.add(
                        "Usage: rmdir <directory>"
                    )

                } else {

                    val directoryName =
                        parts[1].trim()

                    val deleted =
                        VirtualFileSystem
                            .deleteDirectory(
                                directoryName
                            )

                    if (deleted) {

                        output.add(
                            "Directory removed: $directoryName"
                        )

                    } else {

                        output.add(
                            "rmdir: failed to remove '$directoryName'"
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