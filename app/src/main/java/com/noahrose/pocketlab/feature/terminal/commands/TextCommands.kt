package com.noahrose.pocketlab.feature.terminal.commands

import com.noahrose.pocketlab.feature.filesystem.VirtualFileSystem
import com.noahrose.pocketlab.feature.terminal.handler.CommandHandler

object TextCommands : CommandHandler {

    /*
     * Your EXISTING handle() function stays here.
     *
     * Do not replace it.
     */
    override fun handle(
        commandName: String,
        parts: List<String>,
        output: MutableList<String>
    ): Boolean {

        // KEEP YOUR EXISTING:
        // grep
        // head
        // tail
        // sort
        // uniq
        // wc
        //
        // implementation here.

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
                    .forEach(output::add)

                true
            }

            "uniq" -> {

                input
                    .distinct()
                    .forEach(output::add)

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
                        .split(Regex("\\s+"))
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

                true
            }

            else -> false
        }
    }
}