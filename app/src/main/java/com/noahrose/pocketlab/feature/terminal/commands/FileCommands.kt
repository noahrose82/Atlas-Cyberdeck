package com.noahrose.pocketlab.feature.terminal.commands

import com.noahrose.pocketlab.feature.filesystem.VirtualFileSystem
import com.noahrose.pocketlab.feature.terminal.execution.ExecutionStatus
import com.noahrose.pocketlab.feature.terminal.handler.CommandHandler

object FileCommands : CommandHandler {

    override fun handle(
        commandName: String,
        parts: List<String>,
        output: MutableList<String>
    ): Boolean {

        when (commandName) {

            "touch" -> {

                if (
                    parts.size < 2 ||
                    parts[1].isBlank()
                ) {

                    ExecutionStatus.set(1)

                    output.add(
                        "Usage: touch <filename>"
                    )

                    return true
                }

                val fileName =
                    parts[1].trim()

                val created =
                    VirtualFileSystem.createFile(
                        fileName
                    )

                if (created) {

                    ExecutionStatus.set(0)

                    output.add(
                        "File created: $fileName"
                    )

                } else {

                    ExecutionStatus.set(1)

                    output.add(
                        "touch: '$fileName' already exists"
                    )
                }

                return true
            }

            "cat" -> {

                if (
                    parts.size < 2 ||
                    parts[1].isBlank()
                ) {

                    ExecutionStatus.set(1)

                    output.add(
                        "Usage: cat <filename>"
                    )

                    return true
                }

                val fileName =
                    parts[1].trim()

                val content =
                    VirtualFileSystem.readFile(
                        fileName
                    )

                if (content == null) {

                    ExecutionStatus.set(1)

                    output.add(
                        "cat: $fileName: No such file"
                    )

                } else {

                    ExecutionStatus.set(0)

                    if (content.isEmpty()) {

                        output.add("<empty>")

                    } else {

                        output.add(content)
                    }
                }

                return true
            }

            "echo" -> {

                val input =
                    if (parts.size < 2) {
                        ""
                    } else {
                        parts[1].trim()
                    }

                ExecutionStatus.set(0)

                output.add(input)

                return true
            }

            "rm" -> {

                if (
                    parts.size < 2 ||
                    parts[1].isBlank()
                ) {

                    ExecutionStatus.set(1)

                    output.add(
                        "Usage: rm <filename>"
                    )

                    return true
                }

                val fileNames =
                    parts[1]
                        .trim()
                        .split(
                            Regex("\\s+")
                        )

                var deletedCount = 0
                var failed = false

                fileNames.forEach { fileName ->

                    val deleted =
                        VirtualFileSystem.deleteFile(
                            fileName
                        )

                    if (deleted) {

                        deletedCount++

                        output.add(
                            "Deleted: $fileName"
                        )

                    } else {

                        failed = true

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

                ExecutionStatus.set(
                    if (failed) 1 else 0
                )

                return true
            }

            /*
             * Quote-aware copy command.
             *
             * parts example:
             *
             * [0] cp
             * [1] classified document.txt
             * [2] backup documents.txt
             */
            "cp" -> {

                if (parts.size < 3) {

                    ExecutionStatus.set(1)

                    output.add(
                        "Usage: cp <source...> <destination>"
                    )

                    return true
                }

                val destination =
                    parts.last()

                val sources =
                    parts
                        .drop(1)
                        .dropLast(1)

                var copiedCount = 0
                var failed = false

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

                        failed = true

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

                ExecutionStatus.set(
                    if (failed) 1 else 0
                )

                return true
            }

            /*
             * Quote-aware move command.
             */
            "mv" -> {

                if (parts.size < 3) {

                    ExecutionStatus.set(1)

                    output.add(
                        "Usage: mv <source...> <destination>"
                    )

                    return true
                }

                val destination =
                    parts.last()

                val sources =
                    parts
                        .drop(1)
                        .dropLast(1)

                var movedCount = 0
                var failed = false

                sources.forEach { source ->

                    val moved =
                        VirtualFileSystem.moveFile(
                            sourceName = source,
                            destinationName = destination
                        )

                    if (moved) {

                        movedCount++

                        output.add(
                            "Moved '$source' to '$destination'"
                        )

                    } else {

                        failed = true

                        output.add(
                            "mv: failed to move '$source'"
                        )
                    }
                }

                if (movedCount == 0) {

                    output.add(
                        "mv: no files moved"
                    )
                }

                ExecutionStatus.set(
                    if (failed) 1 else 0
                )

                return true
            }

            "grep" -> {

                if (
                    parts.size < 2 ||
                    parts[1].isBlank()
                ) {

                    ExecutionStatus.set(1)

                    output.add(
                        "Usage: grep <text> <filename>"
                    )

                    return true
                }

                val arguments =
                    parts[1]
                        .trim()
                        .split(
                            Regex("\\s+"),
                            limit = 2
                        )

                if (arguments.size < 2) {

                    ExecutionStatus.set(1)

                    output.add(
                        "Usage: grep <text> <filename>"
                    )

                    return true
                }

                val searchText =
                    arguments[0]

                val fileName =
                    arguments[1]

                val content =
                    VirtualFileSystem.readFile(
                        fileName
                    )

                if (content == null) {

                    ExecutionStatus.set(1)

                    output.add(
                        "grep: $fileName: No such file"
                    )

                } else {

                    val matches =
                        content
                            .lines()
                            .filter {
                                it.contains(searchText)
                            }

                    if (matches.isEmpty()) {

                        ExecutionStatus.set(1)

                        output.add(
                            "No matches found."
                        )

                    } else {

                        ExecutionStatus.set(0)

                        matches.forEach(
                            output::add
                        )
                    }
                }

                return true
            }

            "head" -> {

                if (
                    parts.size < 2 ||
                    parts[1].isBlank()
                ) {

                    ExecutionStatus.set(1)

                    output.add(
                        "Usage: head <filename>"
                    )

                    return true
                }

                val fileName =
                    parts[1].trim()

                val content =
                    VirtualFileSystem.readFile(
                        fileName
                    )

                if (content == null) {

                    ExecutionStatus.set(1)

                    output.add(
                        "head: $fileName: No such file"
                    )

                } else {

                    ExecutionStatus.set(0)

                    content
                        .lines()
                        .take(3)
                        .forEach(
                            output::add
                        )
                }

                return true
            }

            "tail" -> {

                if (
                    parts.size < 2 ||
                    parts[1].isBlank()
                ) {

                    ExecutionStatus.set(1)

                    output.add(
                        "Usage: tail <filename>"
                    )

                    return true
                }

                val fileName =
                    parts[1].trim()

                val content =
                    VirtualFileSystem.readFile(
                        fileName
                    )

                if (content == null) {

                    ExecutionStatus.set(1)

                    output.add(
                        "tail: $fileName: No such file"
                    )

                } else {

                    ExecutionStatus.set(0)

                    content
                        .lines()
                        .takeLast(3)
                        .forEach(
                            output::add
                        )
                }

                return true
            }
        }

        return false
    }
}