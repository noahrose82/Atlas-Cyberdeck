package com.noahrose.pocketlab.feature.terminal.wildcard

import com.noahrose.pocketlab.feature.filesystem.VirtualFileSystem

object WildcardExpander {

    fun expand(command: String): String {

        if (!command.contains("*")) {
            return command
        }

        val tokens =
            command
                .trim()
                .split(
                    Regex("\\s+")
                )

        if (tokens.size < 2) {
            return command
        }

        val commandName =
            tokens.first()

        val arguments =
            tokens.drop(1)

        val expandedArguments =
            arguments.flatMap { argument ->

                if (!argument.contains("*")) {

                    listOf(argument)

                } else {

                    val regexPattern =
                        argument
                            .replace(".", "\\.")
                            .replace("*", ".*")

                    val regex =
                        Regex(
                            "^$regexPattern$",
                            RegexOption.IGNORE_CASE
                        )

                    val matches =
                        VirtualFileSystem.currentEntries.value
                            .filter {
                                !it.isDirectory &&
                                        regex.matches(it.name)
                            }
                            .map {
                                it.name
                            }

                    if (matches.isEmpty()) {
                        listOf(argument)
                    } else {
                        matches
                    }
                }
            }

        return buildString {

            append(commandName)

            if (expandedArguments.isNotEmpty()) {

                append(" ")

                append(
                    expandedArguments.joinToString(" ")
                )
            }
        }
    }
}