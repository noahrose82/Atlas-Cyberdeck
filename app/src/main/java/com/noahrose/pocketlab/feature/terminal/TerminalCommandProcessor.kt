package com.noahrose.pocketlab.feature.terminal

import com.noahrose.pocketlab.feature.filesystem.VirtualFileSystem

object TerminalCommandProcessor {

    fun process(
        command: String,
        output: MutableList<String>
    ) {

        val currentPath = VirtualFileSystem.currentPath.value

        output.add("atlas@cyberdeck:$currentPath$ $command")

        val parts = command.trim().split(" ", limit = 2)

        when (parts[0].lowercase()) {

            "help" -> {
                output.add("Available commands:")
                output.add("")
                output.add("tree")
                output.add("help")
                output.add("cp")
                output.add("mv")
                output.add("find")
                output.add("clear")
                output.add("whoami")
                output.add("pwd")
                output.add("ls")
                output.add("mkdir")
                output.add("rmdir")
                output.add("rm")
                output.add("cd")
                output.add("status")
                output.add("neofetch")
            }

            "clear" -> {
                output.clear()
            }

            "whoami" -> {
                output.add("atlas")
            }

            "pwd" -> {
                output.add(
                    VirtualFileSystem.currentPath.value
                        .replace("~", "/home/atlas")
                )
            }

            "ls" -> {

                val entries =
                    VirtualFileSystem.currentEntries.value

                if (entries.isEmpty()) {

                    output.add("<empty>")

                } else {

                    entries.forEach { entry ->

                        if (entry.isDirectory) {
                            output.add("${entry.name}/")
                        } else {
                            output.add(entry.name)

                        }
                    }
                }
            }
            "tree" -> {

                val treeLines =
                    VirtualFileSystem.buildTree()

                treeLines.forEach { line ->
                    output.add(line)
                }
            }

            "find" -> {

                if (parts.size < 2 || parts[1].isBlank()) {

                    output.add("Usage: find <name>")

                } else {

                    val targetName = parts[1].trim()

                    val results =
                        VirtualFileSystem.find(targetName)

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

            "cp" -> {

                if (parts.size < 2) {

                    output.add(
                        "Usage: cp <source> <destination>"
                    )

                } else {

                    val arguments =
                        parts[1].trim().split(
                            Regex("\\s+"),
                            limit = 2
                        )

                    if (arguments.size < 2) {

                        output.add(
                            "Usage: cp <source> <destination>"
                        )

                    } else {

                        val source =
                            arguments[0]

                        val destination =
                            arguments[1]

                        val copied =
                            VirtualFileSystem.copyFile(
                                sourceName = source,
                                destinationName = destination
                            )

                        if (copied) {

                            output.add(
                                "Copied '$source' to '$destination'"
                            )

                        } else {

                            output.add(
                                "cp: failed to copy '$source'"
                            )
                        }
                    }
                }
            }

            "mv" -> {

                if (parts.size < 2) {

                    output.add(
                        "Usage: mv <source> <destination>"
                    )

                } else {

                    val arguments =
                        parts[1].trim().split(
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

            "mkdir" -> {

                if (parts.size < 2 || parts[1].isBlank()) {

                    output.add("Usage: mkdir <directory>")

                } else {

                    val directoryName = parts[1].trim()

                    val created =
                        VirtualFileSystem.createDirectory(directoryName)

                    if (created) {

                        output.add("Directory created: $directoryName")

                    } else {

                        output.add(
                            "mkdir: '$directoryName' already exists"
                        )

                    }
                }
            }

            "cd" -> {
                if (parts.size < 2 || parts[1].isBlank()) {
                    output.add("Usage: cd <directory>")
                } else {
                    val destination = parts[1].trim()

                    val changed =
                        VirtualFileSystem.changeDirectory(destination)

                    if (!changed) {
                        output.add(
                            "cd: $destination: No such directory"
                        )
                    }
                }
            }
            "touch" -> {

                if (parts.size < 2 || parts[1].isBlank()) {

                    output.add("Usage: touch <filename>")

                } else {

                    val fileName = parts[1].trim()

                    val created =
                        VirtualFileSystem.createFile(fileName)

                    if (created) {

                        output.add("File created: $fileName")

                    } else {

                        output.add(
                            "touch: '$fileName' already exists"
                        )
                    }
                }
            }
            "cat" -> {

                if (parts.size < 2 || parts[1].isBlank()) {

                    output.add("Usage: cat <filename>")

                } else {

                    val fileName = parts[1].trim()

                    val content =
                        VirtualFileSystem.readFile(fileName)

                    if (content == null) {

                        output.add(
                            "cat: $fileName: No such file"
                        )

                    } else if (content.isEmpty()) {

                        output.add("<empty>")

                    } else {

                        output.add(content)
                    }
                }
            }

            "echo" -> {

                val input =
                    command.removePrefix("echo").trim()

                if (!input.contains(">")) {

                    output.add(
                        "Usage: echo <text> > <filename>"
                    )

                } else {

                    val pieces =
                        input.split(">", limit = 2)

                    val text =
                        pieces[0].trim()

                    val fileName =
                        pieces[1].trim()

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

            "rm" -> {

                if (parts.size < 2 || parts[1].isBlank()) {

                    output.add("Usage: rm <filename>")

                } else {

                    val fileName = parts[1].trim()

                    val deleted =
                        VirtualFileSystem.deleteFile(fileName)

                    if (deleted) {

                        output.add("Deleted: $fileName")

                    } else {

                        output.add(
                            "rm: $fileName: No such file"
                        )
                    }
                }
            }

            "rmdir" -> {

                if (parts.size < 2 || parts[1].isBlank()) {

                    output.add("Usage: rmdir <directory>")

                } else {

                    val directoryName = parts[1].trim()

                    val deleted =
                        VirtualFileSystem.deleteDirectory(directoryName)

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

            "status" -> {
                output.add("Atlas Cyberdeck")
                output.add("Status : ONLINE")
                output.add("Linux : INSTALLED")
                output.add("Terminal : ACTIVE")
            }

            "neofetch" -> {
                output.add("Atlas Cyberdeck v0.6.0 \"Forge\"")
                output.add("OS      : Atlas Linux")
                output.add("Kernel  : 6.1")
                output.add("Shell   : Atlas Terminal")
                output.add("User    : atlas")
            }

            else -> {
                output.add("Command not found: $command")
            }
        }
    }
}