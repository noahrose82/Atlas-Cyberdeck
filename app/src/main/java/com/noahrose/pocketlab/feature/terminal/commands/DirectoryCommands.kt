package com.noahrose.pocketlab.feature.terminal.commands

import com.noahrose.pocketlab.feature.filesystem.VirtualFileSystem
import com.noahrose.pocketlab.feature.terminal.execution.ExecutionStatus
import com.noahrose.pocketlab.feature.terminal.handler.CommandHandler

object DirectoryCommands : CommandHandler {

    override fun handle(
        commandName: String,
        parts: List<String>,
        output: MutableList<String>
    ): Boolean {

        when (commandName) {

            "ls" -> {

                val entries =
                    VirtualFileSystem.currentEntries.value

                if (
                    parts.size < 2 ||
                    parts[1].isBlank()
                ) {

                    ExecutionStatus.set(0)

                    if (entries.isEmpty()) {

                        output.add("<empty>")

                    } else {

                        entries.forEach { entry ->

                            output.add(
                                if (entry.isDirectory) {
                                    "${entry.name}/"
                                } else {
                                    entry.name
                                }
                            )
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

                                entry.name.equals(
                                    requestedName,
                                    ignoreCase = true
                                )
                            }
                        }

                    if (matchingEntries.isEmpty()) {

                        ExecutionStatus.set(1)

                        output.add(
                            "ls: no matching files found"
                        )

                    } else {

                        ExecutionStatus.set(0)

                        matchingEntries.forEach { entry ->

                            output.add(
                                if (entry.isDirectory) {
                                    "${entry.name}/"
                                } else {
                                    entry.name
                                }
                            )
                        }
                    }
                }

                return true
            }

            "tree" -> {

                ExecutionStatus.set(0)

                VirtualFileSystem
                    .buildTree()
                    .forEach(
                        output::add
                    )

                return true
            }

            "find" -> {

                if (
                    parts.size < 2 ||
                    parts[1].isBlank()
                ) {

                    ExecutionStatus.set(1)

                    output.add(
                        "Usage: find <name>"
                    )

                } else {

                    val target =
                        parts[1].trim()

                    val results =
                        VirtualFileSystem.find(
                            target
                        )

                    if (results.isEmpty()) {

                        ExecutionStatus.set(1)

                        output.add(
                            "find: '$target': No matches found"
                        )

                    } else {

                        ExecutionStatus.set(0)

                        results.forEach(
                            output::add
                        )
                    }
                }

                return true
            }

            "mkdir" -> {

                if (
                    parts.size < 2 ||
                    parts[1].isBlank()
                ) {

                    ExecutionStatus.set(1)

                    output.add(
                        "Usage: mkdir <directory>"
                    )

                } else {

                    val directory =
                        parts[1].trim()

                    val created =
                        VirtualFileSystem.createDirectory(
                            directory
                        )

                    if (created) {

                        ExecutionStatus.set(0)

                        output.add(
                            "Directory created: $directory"
                        )

                    } else {

                        ExecutionStatus.set(1)

                        output.add(
                            "mkdir: '$directory' already exists"
                        )
                    }
                }

                return true
            }

            "cd" -> {

                if (
                    parts.size < 2 ||
                    parts[1].isBlank()
                ) {

                    ExecutionStatus.set(1)

                    output.add(
                        "Usage: cd <directory>"
                    )

                } else {

                    val destination =
                        parts[1].trim()

                    val changed =
                        VirtualFileSystem.changeDirectory(
                            destination
                        )

                    if (changed) {

                        ExecutionStatus.set(0)

                    } else {

                        ExecutionStatus.set(1)

                        output.add(
                            "cd: $destination: No such directory"
                        )
                    }
                }

                return true
            }

            "rmdir" -> {

                if (
                    parts.size < 2 ||
                    parts[1].isBlank()
                ) {

                    ExecutionStatus.set(1)

                    output.add(
                        "Usage: rmdir <directory>"
                    )

                } else {

                    val directory =
                        parts[1].trim()

                    val deleted =
                        VirtualFileSystem.deleteDirectory(
                            directory
                        )

                    if (deleted) {

                        ExecutionStatus.set(0)

                        output.add(
                            "Directory removed: $directory"
                        )

                    } else {

                        ExecutionStatus.set(1)

                        output.add(
                            "rmdir: failed to remove '$directory'"
                        )
                    }
                }

                return true
            }
        }

        return false
    }
}