package com.noahrose.pocketlab.feature.terminal

import com.noahrose.pocketlab.feature.filesystem.VirtualFileSystem
import com.noahrose.pocketlab.feature.terminal.alias.CommandAliases
import com.noahrose.pocketlab.feature.terminal.environment.VariableExpander
import com.noahrose.pocketlab.feature.terminal.history.CommandHistory
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

            commandName == "help" -> {

                output.add("Available commands:")
                output.add("")
                output.add("tree")
                output.add("help")
                output.add("history")
                output.add("!!")
                output.add("!<number>")
                output.add("cp")
                output.add("mv")
                output.add("find")
                output.add("clear")
                output.add("whoami")
                output.add("pwd")
                output.add("ls")
                output.add("mkdir")
                output.add("touch")
                output.add("cat")
                output.add("echo")
                output.add("rmdir")
                output.add("rm")
                output.add("cd")
                output.add("status")
                output.add("neofetch")
            }

            commandName == "history" -> {

                val history =
                    CommandHistory.getHistory()

                if (history.isEmpty()) {

                    output.add(
                        "No commands in history."
                    )

                } else {

                    history.forEachIndexed {
                            index,
                            historyCommand ->

                        output.add(
                            "${index + 1}  $historyCommand"
                        )
                    }
                }
            }

            commandName == "clear" -> {

                output.clear()
            }

            commandName == "whoami" -> {

                output.add("atlas")
            }

            commandName == "pwd" -> {

                output.add(
                    VirtualFileSystem
                        .currentPath
                        .value
                        .replace(
                            "~",
                            "/home/atlas"
                        )
                )
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

            commandName == "cp" -> {

                if (
                    parts.size < 2 ||
                    parts[1].isBlank()
                ) {

                    output.add(
                        "Usage: cp <source...> <destination>"
                    )

                } else {

                    val arguments =
                        parts[1]
                            .trim()
                            .split(
                                Regex("\\s+")
                            )

                    if (arguments.size < 2) {

                        output.add(
                            "Usage: cp <source...> <destination>"
                        )

                    } else {

                        val destination =
                            arguments.last()

                        val sources =
                            arguments.dropLast(1)

                        var copiedCount = 0

                        sources.forEach { source ->

                            val copied =
                                VirtualFileSystem.copyFile(
                                    sourceName = source,
                                    destinationName = destination
                                )

                            if (copied) {

                                copiedCount++

                                output.add(
                                    "Copied '$source' to '$destination'"
                                )

                            } else {

                                output.add(
                                    "cp: failed to copy '$source'"
                                )
                            }
                        }

                        if (copiedCount == 0) {

                            output.add(
                                "cp: no files copied"
                            )
                        }
                    }
                }
            }

            commandName == "mv" -> {

                if (
                    parts.size < 2 ||
                    parts[1].isBlank()
                ) {

                    output.add(
                        "Usage: mv <source> <destination>"
                    )

                } else {

                    val arguments =
                        parts[1]
                            .trim()
                            .split(
                                Regex("\\s+"),
                                limit = 2
                            )

                    if (arguments.size < 2) {

                        output.add(
                            "Usage: mv <source> <destination>"
                        )

                    } else {

                        val source =
                            arguments[0]

                        val destination =
                            arguments[1]

                        val moved =
                            VirtualFileSystem.moveFile(
                                sourceName = source,
                                destinationName = destination
                            )

                        if (moved) {

                            output.add(
                                "Renamed '$source' to '$destination'"
                            )

                        } else {

                            output.add(
                                "mv: failed to rename '$source'"
                            )
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

            commandName == "touch" -> {

                if (
                    parts.size < 2 ||
                    parts[1].isBlank()
                ) {

                    output.add(
                        "Usage: touch <filename>"
                    )

                } else {

                    val fileName =
                        parts[1].trim()

                    val created =
                        VirtualFileSystem
                            .createFile(
                                fileName
                            )

                    if (created) {

                        output.add(
                            "File created: $fileName"
                        )

                    } else {

                        output.add(
                            "touch: '$fileName' already exists"
                        )
                    }
                }
            }

            commandName == "cat" -> {

                if (
                    parts.size < 2 ||
                    parts[1].isBlank()
                ) {

                    output.add(
                        "Usage: cat <filename>"
                    )

                } else {

                    val fileNames =
                        parts[1]
                            .trim()
                            .split(
                                Regex("\\s+")
                            )

                    fileNames.forEachIndexed { index, fileName ->

                        val content =
                            VirtualFileSystem.readFile(
                                fileName
                            )

                        if (content == null) {

                            output.add(
                                "cat: $fileName: No such file"
                            )

                        } else {

                            if (fileNames.size > 1) {

                                output.add(
                                    "----- $fileName -----"
                                )
                            }

                            if (content.isEmpty()) {

                                output.add("<empty>")

                            } else {

                                output.add(content)
                            }

                            if (
                                fileNames.size > 1 &&
                                index != fileNames.lastIndex
                            ) {
                                output.add("")
                            }
                        }
                    }
                }
            }

            commandName == "echo" -> {

                val input =
                    if (parts.size < 2) {
                        ""
                    } else {
                        parts[1].trim()
                    }

                if (input.isBlank()) {

                    output.add("")

                } else if (!input.contains(">")) {

                    output.add(input)

                } else {

                    val pieces =
                        input.split(
                            ">",
                            limit = 2
                        )

                    val text =
                        pieces[0].trim()

                    val fileName =
                        pieces[1].trim()

                    if (fileName.isBlank()) {

                        output.add(
                            "Usage: echo <text> > <filename>"
                        )

                    } else {

                        val success =
                            VirtualFileSystem.writeFile(
                                name = fileName,
                                content = text
                            )

                        if (success) {

                            output.add(
                                "Wrote to $fileName"
                            )

                        } else {

                            output.add(
                                "echo: $fileName: No such file"
                            )
                        }
                    }
                }
            }

            commandName == "rm" -> {

                if (
                    parts.size < 2 ||
                    parts[1].isBlank()
                ) {

                    output.add(
                        "Usage: rm <filename>"
                    )

                } else {

                    val fileNames =
                        parts[1]
                            .trim()
                            .split(
                                Regex("\\s+")
                            )

                    var deletedCount = 0

                    fileNames.forEach { fileName ->

                        val deleted =
                            VirtualFileSystem.deleteFile(
                                fileName
                            )

                        if (deleted) {

                            output.add(
                                "Deleted: $fileName"
                            )

                            deletedCount++

                        } else {

                            output.add(
                                "rm: $fileName: No such file"
                            )
                        }
                    }

                    if (deletedCount == 0) {

                        output.add(
                            "No files were deleted."
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

            commandName == "status" -> {

                output.add("Atlas Cyberdeck")
                output.add("Status : ONLINE")
                output.add("Linux : INSTALLED")
                output.add("Terminal : ACTIVE")
            }

            commandName == "neofetch" -> {

                output.add(
                    "Atlas Cyberdeck v0.9.0 \"Forge\""
                )
                output.add(
                    "OS      : Atlas Linux"
                )
                output.add(
                    "Kernel  : 6.1"
                )
                output.add(
                    "Shell   : Atlas Terminal"
                )
                output.add(
                    "User    : atlas"
                )
            }

            else -> {

                output.add(
                    "Command not found: $trimmedCommand"
                )
            }
        }
    }
}