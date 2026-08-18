package com.noahrose.pocketlab.feature.terminal.sequential

object SequentialCommandParser {

    fun parse(
        command: String
    ): List<String>? {

        val commands =
            mutableListOf<String>()

        val current =
            StringBuilder()

        var quoteMode =
            QuoteMode.NONE

        var escaping = false
        var foundSeparator = false
        var index = 0

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
                            quoteMode == QuoteMode.DOUBLE
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
                            quoteMode == QuoteMode.SINGLE
                        ) {
                            QuoteMode.NONE
                        } else {
                            QuoteMode.SINGLE
                        }
                }

                character == ';' &&
                        quoteMode == QuoteMode.NONE -> {

                    val segment =
                        current
                            .toString()
                            .trim()

                    if (segment.isBlank()) {
                        return null
                    }

                    commands.add(
                        segment
                    )

                    current.clear()

                    foundSeparator = true
                }

                else -> {

                    current.append(
                        character
                    )
                }
            }

            index++
        }

        /*
         * No unquoted semicolon means this
         * is not a sequential command.
         */
        if (!foundSeparator) {
            return null
        }

        /*
         * Let the main tokenizer handle
         * malformed quotation errors.
         */
        if (quoteMode != QuoteMode.NONE) {
            return null
        }

        val finalCommand =
            current
                .toString()
                .trim()

        if (finalCommand.isBlank()) {
            return null
        }

        commands.add(
            finalCommand
        )

        return commands
    }

    private enum class QuoteMode {
        NONE,
        SINGLE,
        DOUBLE
    }
}