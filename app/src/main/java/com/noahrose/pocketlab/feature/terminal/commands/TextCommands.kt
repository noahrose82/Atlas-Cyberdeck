package com.noahrose.pocketlab.feature.terminal.commands

import com.noahrose.pocketlab.feature.filesystem.VirtualFileSystem
import com.noahrose.pocketlab.feature.terminal.handler.CommandHandler
object TextCommands : CommandHandler {

    override fun handle(
        commandName: String,
        parts: List<String>,
        output: MutableList<String>
    ): Boolean {

        when (commandName) {

            "wc" -> {

                if (
                    parts.size < 2 ||
                    parts[1].isBlank()
                ) {

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

                    output.add(
                        "wc: $fileName: No such file"
                    )

                    return true
                }

                val lines =
                    if (content.isEmpty()) {
                        0
                    } else {
                        content.lines().size
                    }

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

                output.add(
                    "Lines      : $lines"
                )

                output.add(
                    "Words      : $words"
                )

                output.add(
                    "Characters : $characters"
                )

                return true
            }

            "sort" -> {

                if (
                    parts.size < 2 ||
                    parts[1].isBlank()
                ) {

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

                    output.add(
                        "sort: $fileName: No such file"
                    )

                    return true
                }

                content
                    .lines()
                    .sorted()
                    .forEach { line ->

                        output.add(line)
                    }

                return true
            }

            "uniq" -> {

                if (
                    parts.size < 2 ||
                    parts[1].isBlank()
                ) {

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

                    output.add(
                        "uniq: $fileName: No such file"
                    )

                    return true
                }

                content
                    .lines()
                    .distinct()
                    .forEach { line ->

                        output.add(line)
                    }

                return true
            }
        }
            return false

        }
    }
