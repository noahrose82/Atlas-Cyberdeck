package com.noahrose.pocketlab.feature.terminal.commands

import com.noahrose.pocketlab.feature.filesystem.VirtualFileSystem
import com.noahrose.pocketlab.feature.terminal.execution.ExecutionStatus
import com.noahrose.pocketlab.feature.terminal.handler.CommandHandler

object TextCommands : CommandHandler {

    override fun handle(
        commandName: String,
        parts: List<String>,
        output: MutableList<String>
    ): Boolean {

        when (commandName) {

            "sort" -> {

                if (
                    parts.size < 2 ||
                    parts[1].isBlank()
                ) {

                    ExecutionStatus.set(1)

                    output.add(
                        "Usage: sort <filename>"
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
                        "sort: $fileName: No such file"
                    )

                } else {

                    ExecutionStatus.set(0)

                    content
                        .lines()
                        .sorted()
                        .forEach(
                            output::add
                        )
                }

                return true
            }

            "uniq" -> {

                if (
                    parts.size < 2 ||
                    parts[1].isBlank()
                ) {

                    ExecutionStatus.set(1)

                    output.add(
                        "Usage: uniq <filename>"
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
                        "uniq: $fileName: No such file"
                    )

                } else {

                    ExecutionStatus.set(0)

                    content
                        .lines()
                        .distinct()
                        .forEach(
                            output::add
                        )
                }

                return true
            }

            "wc" -> {

                if (
                    parts.size < 2 ||
                    parts[1].isBlank()
                ) {

                    ExecutionStatus.set(1)

                    output.add(
                        "Usage: wc <filename>"
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
                        "wc: $fileName: No such file"
                    )

                } else {

                    val lines =
                        content.lines().size

                    val words =
                        content
                            .trim()
                            .split(
                                Regex("\\s+")
                            )
                            .filter {
                                it.isNotBlank()
                            }
                            .size

                    val characters =
                        content.length

                    ExecutionStatus.set(0)

                    output.add(
                        "Lines      : $lines"
                    )

                    output.add(
                        "Words      : $words"
                    )

                    output.add(
                        "Characters : $characters"
                    )
                }

                return true
            }
        }

        return false
    }

    /*
     * Handles text supplied through stdin-style
     * input redirection.
     *
     * Examples:
     *
     * sort < names.txt
     * uniq < names.txt
     * wc < names.txt
     */
    fun handleInput(
        commandName: String,
        input: List<String>,
        output: MutableList<String>
    ): Boolean {

        return when (commandName) {

            "sort" -> {

                input
                    .sorted()
                    .forEach(
                        output::add
                    )

                ExecutionStatus.set(0)

                true
            }

            "uniq" -> {

                input
                    .distinct()
                    .forEach(
                        output::add
                    )

                ExecutionStatus.set(0)

                true
            }

            "wc" -> {

                val text =
                    input.joinToString("\n")

                val lines =
                    input.size

                val words =
                    text
                        .trim()
                        .split(
                            Regex("\\s+")
                        )
                        .filter {
                            it.isNotBlank()
                        }
                        .size

                val characters =
                    text.length

                output.add(
                    "Lines      : $lines"
                )

                output.add(
                    "Words      : $words"
                )

                output.add(
                    "Characters : $characters"
                )

                ExecutionStatus.set(0)

                true
            }

            else -> {

                ExecutionStatus.set(1)

                false
            }
        }
    }
}