package com.noahrose.pocketlab.feature.terminal.pipe

import com.noahrose.pocketlab.feature.filesystem.VirtualFileSystem
import com.noahrose.pocketlab.feature.terminal.execution.ExecutionStatus
import com.noahrose.pocketlab.feature.terminal.parsing.CommandTokenizer

object PipeEngine {

    fun handle(
        command: String,
        output: MutableList<String>
    ): Boolean {

        val stages =
            splitPipelineStages(
                command
            ) ?: return false

        if (stages.size < 2) {
            return false
        }

        var pipedLines =
            executeInputStage(
                stage = stages.first(),
                output = output
            ) ?: run {

                ExecutionStatus.set(1)

                return true
            }

        stages
            .drop(1)
            .forEach { stage ->

                pipedLines =
                    executeFilterStage(
                        stage = stage,
                        input = pipedLines,
                        output = output
                    ) ?: run {

                        ExecutionStatus.set(1)

                        return true
                    }
            }

        pipedLines.forEach { line ->
            output.add(line)
        }

        ExecutionStatus.set(0)

        return true
    }

    /*
     * Split a pipeline only on a single |
     * appearing outside quoted text.
     *
     * Examples:
     *
     * cat file.txt | grep Atlas
     *      -> pipeline
     *
     * echo "A | B"
     *      -> not a pipeline
     *
     * echo 'A || B'
     *      -> not a pipeline
     *
     * commandA || commandB
     *      -> belongs to conditional chaining,
     *         not the pipe engine
     */
    private fun splitPipelineStages(
        command: String
    ): List<String>? {

        val stages =
            mutableListOf<String>()

        val current =
            StringBuilder()

        var quoteMode =
            QuoteMode.NONE

        var escaping = false
        var index = 0
        var foundPipe = false

        while (index < command.length) {

            val character =
                command[index]

            when {

                escaping -> {

                    current.append(
                        character
                    )

                    escaping = false
                }

                character == '\\' &&
                        quoteMode != QuoteMode.SINGLE -> {

                    current.append(
                        character
                    )

                    escaping = true
                }

                character == '"' &&
                        quoteMode != QuoteMode.SINGLE -> {

                    current.append(
                        character
                    )

                    quoteMode =
                        if (
                            quoteMode ==
                            QuoteMode.DOUBLE
                        ) {
                            QuoteMode.NONE
                        } else {
                            QuoteMode.DOUBLE
                        }
                }

                character == '\'' &&
                        quoteMode != QuoteMode.DOUBLE -> {

                    current.append(
                        character
                    )

                    quoteMode =
                        if (
                            quoteMode ==
                            QuoteMode.SINGLE
                        ) {
                            QuoteMode.NONE
                        } else {
                            QuoteMode.SINGLE
                        }
                }

                quoteMode == QuoteMode.NONE &&
                        character == '|' -> {

                    /*
                     * || belongs to conditional
                     * command chaining.
                     */
                    if (
                        index + 1 <
                        command.length &&
                        command[index + 1] == '|'
                    ) {

                        return null
                    }

                    val stage =
                        current
                            .toString()
                            .trim()

                    if (stage.isBlank()) {

                        ExecutionStatus.set(2)

                        return listOf(
                            ""
                        )
                    }

                    stages.add(
                        stage
                    )

                    current.clear()

                    foundPipe = true
                }

                else -> {

                    current.append(
                        character
                    )
                }
            }

            index++
        }

        if (!foundPipe) {
            return null
        }

        val finalStage =
            current
                .toString()
                .trim()

        if (finalStage.isBlank()) {

            ExecutionStatus.set(2)

            return listOf(
                ""
            )
        }

        stages.add(
            finalStage
        )

        return stages
    }

    private fun executeInputStage(
        stage: String,
        output: MutableList<String>
    ): List<String>? {

        val tokens =
            CommandTokenizer.tokenizeOrNull(
                stage
            )

        if (
            tokens == null ||
            tokens.isEmpty()
        ) {

            output.add(
                "pipe: invalid input command"
            )

            return null
        }

        val commandName =
            tokens.first().lowercase()

        return when (commandName) {

            "cat" -> {

                if (tokens.size < 2) {

                    output.add(
                        "Usage: cat <filename> | <command>"
                    )

                    null

                } else {

                    val fileNames =
                        tokens.drop(1)

                    val lines =
                        mutableListOf<String>()

                    var failed = false

                    fileNames.forEach { fileName ->

                        val content =
                            VirtualFileSystem.readFile(
                                fileName
                            )

                        if (content == null) {

                            failed = true

                            output.add(
                                "cat: $fileName: No such file"
                            )

                        } else {

                            lines.addAll(
                                content.lines()
                            )
                        }
                    }

                    if (failed) {
                        null
                    } else {
                        lines
                    }
                }
            }

            "echo" -> {

                val text =
                    tokens
                        .drop(1)
                        .joinToString(" ")

                listOf(
                    text
                )
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

        val tokens =
            CommandTokenizer.tokenizeOrNull(
                stage
            )

        if (
            tokens == null ||
            tokens.isEmpty()
        ) {

            output.add(
                "pipe: invalid command"
            )

            return null
        }

        val commandName =
            tokens.first().lowercase()

        return when (commandName) {

            "grep" -> {

                if (tokens.size < 2) {

                    output.add(
                        "Usage: <command> | grep <text>"
                    )

                    null

                } else {

                    val searchText =
                        tokens
                            .drop(1)
                            .joinToString(" ")

                    input.filter { line ->
                        line.contains(
                            searchText
                        )
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

    private enum class QuoteMode {
        NONE,
        SINGLE,
        DOUBLE
    }
}