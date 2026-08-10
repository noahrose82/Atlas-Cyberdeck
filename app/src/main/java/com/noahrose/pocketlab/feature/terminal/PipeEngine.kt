package com.noahrose.pocketlab.feature.terminal.pipe

import com.noahrose.pocketlab.feature.filesystem.VirtualFileSystem

object PipeEngine {

    fun handle(
        command: String,
        output: MutableList<String>
    ): Boolean {

        if (!command.contains("|")) {
            return false
        }

        val stages =
            command
                .split("|")
                .map { it.trim() }
                .filter { it.isNotBlank() }

        if (stages.size < 2) {

            output.add(
                "Usage: <command> | <command>"
            )

            return true
        }

        var pipedLines =
            executeInputStage(
                stage = stages.first(),
                output = output
            ) ?: return true

        stages
            .drop(1)
            .forEach { stage ->

                pipedLines =
                    executeFilterStage(
                        stage = stage,
                        input = pipedLines,
                        output = output
                    ) ?: return true
            }

        pipedLines.forEach { line ->
            output.add(line)
        }

        return true
    }

    private fun executeInputStage(
        stage: String,
        output: MutableList<String>
    ): List<String>? {

        val parts =
            stage.split(
                Regex("\\s+"),
                limit = 2
            )

        val commandName =
            parts[0].lowercase()

        return when (commandName) {

            "cat" -> {

                if (
                    parts.size < 2 ||
                    parts[1].isBlank()
                ) {

                    output.add(
                        "Usage: cat <filename> | <command>"
                    )

                    null

                } else {

                    val fileNames =
                        parts[1]
                            .trim()
                            .split(
                                Regex("\\s+")
                            )

                    val lines =
                        mutableListOf<String>()

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

                            lines.addAll(
                                content.lines()
                            )
                        }
                    }

                    lines
                }
            }

            "echo" -> {

                val text =
                    if (parts.size < 2) {
                        ""
                    } else {
                        parts[1]
                    }

                listOf(text)
            }

            else -> {

                output.add(
                    "pipe: unsupported input command: $commandName"
                )

                null
            }
        }
    }

    private fun executeFilterStage(
        stage: String,
        input: List<String>,
        output: MutableList<String>
    ): List<String>? {

        val parts =
            stage.split(
                Regex("\\s+"),
                limit = 2
            )

        val commandName =
            parts[0].lowercase()

        return when (commandName) {

            "grep" -> {

                if (
                    parts.size < 2 ||
                    parts[1].isBlank()
                ) {

                    output.add(
                        "Usage: <command> | grep <text>"
                    )

                    null

                } else {

                    val searchText =
                        parts[1].trim()

                    input.filter { line ->
                        line.contains(searchText)
                    }
                }
            }

            "head" -> {

                input.take(3)
            }

            "tail" -> {

                input.takeLast(3)
            }

            "sort" -> {

                input.sorted()
            }

            "uniq" -> {

                input.distinct()
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

                listOf(
                    "Lines      : $lines",
                    "Words      : $words",
                    "Characters : $characters"
                )
            }

            else -> {

                output.add(
                    "pipe: unsupported command: $commandName"
                )

                null
            }
        }
    }
}