package com.noahrose.pocketlab.feature.terminal.commands

import com.noahrose.pocketlab.feature.filesystem.VirtualFileSystem

object DirectoryCommands {

    fun handle(
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

                    if (entries.isEmpty()) {

                        output.add("<empty>")

                    } else {

                        entries.forEach { entry ->

                            output.add(
                                if (entry.isDirectory)
                                    "${entry.name}/"
                                else
                                    entry.name
                            )
                        }
                    }

                } else {

                    val requestedNames =
                        parts[1]
                            .trim()
                            .split(Regex("\\s+"))

                    val matchingEntries =
                        requestedNames.mapNotNull { requestedName ->

                            entries.find {
                                it.name == requestedName
                            }
                        }

                    if (matchingEntries.isEmpty()) {

                        output.add(
                            "ls: no matching files found"
                        )

                    } else {

                        matchingEntries.forEach { entry ->

                            output.add(
                                if (entry.isDirectory)
                                    "${entry.name}/"
                                else
                                    entry.name
                            )
                        }
                    }
                }

                return true
            }

            "tree" -> {

                VirtualFileSystem
                    .buildTree()
                    .forEach(output::add)

                return true
            }

            "find" -> {

                if (
                    parts.size < 2 ||
                    parts[1].isBlank()
                ) {

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

                        output.add(
                            "find: '$target': No matches found"
                        )

                    } else {

                        results.forEach(output::add)
                    }
                }

                return true
            }

            "mkdir" -> {

                if (
                    parts.size < 2 ||
                    parts[1].isBlank()
                ) {

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

                        output.add(
                            "Directory created: $directory"
                        )

                    } else {

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

                    if (!changed) {

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

                        output.add(
                            "Directory removed: $directory"
                        )

                    } else {

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