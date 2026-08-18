package com.noahrose.pocketlab.feature.terminal.chaining

object ConditionalChainParser {

    fun parse(
        command: String
    ): List<ConditionalCommand>? {

        val segments =
            mutableListOf<String>()

        val operators =
            mutableListOf<ConditionalOperator>()

        val current =
            StringBuilder()

        var quoteMode =
            QuoteMode.NONE

        var escaping = false
        var index = 0

        while (index < command.length) {

            val character =
                command[index]

            when {

                escaping -> {

                    current.append(character)
                    escaping = false
                }

                character == '\\' &&
                        quoteMode != QuoteMode.SINGLE -> {

                    current.append(character)
                    escaping = true
                }

                character == '"' &&
                        quoteMode != QuoteMode.SINGLE -> {

                    current.append(character)

                    quoteMode =
                        if (quoteMode == QuoteMode.DOUBLE) {
                            QuoteMode.NONE
                        } else {
                            QuoteMode.DOUBLE
                        }
                }

                character == '\'' &&
                        quoteMode != QuoteMode.DOUBLE -> {

                    current.append(character)

                    quoteMode =
                        if (quoteMode == QuoteMode.SINGLE) {
                            QuoteMode.NONE
                        } else {
                            QuoteMode.SINGLE
                        }
                }

                quoteMode == QuoteMode.NONE &&
                        character == '&' &&
                        index + 1 < command.length &&
                        command[index + 1] == '&' -> {

                    val segment =
                        current
                            .toString()
                            .trim()

                    if (segment.isBlank()) {
                        return null
                    }

                    segments.add(segment)
                    operators.add(
                        ConditionalOperator.AND
                    )

                    current.clear()

                    index++
                }

                quoteMode == QuoteMode.NONE &&
                        character == '|' &&
                        index + 1 < command.length &&
                        command[index + 1] == '|' -> {

                    val segment =
                        current
                            .toString()
                            .trim()

                    if (segment.isBlank()) {
                        return null
                    }

                    segments.add(segment)
                    operators.add(
                        ConditionalOperator.OR
                    )

                    current.clear()

                    index++
                }

                else -> {

                    current.append(character)
                }
            }

            index++
        }

        /*
         * This parser only owns conditional syntax.
         *
         * If no && or || operator was discovered,
         * the normal shell processor should handle
         * the command.
         */
        if (operators.isEmpty()) {
            return null
        }

        val finalSegment =
            current
                .toString()
                .trim()

        if (finalSegment.isBlank()) {
            return null
        }

        segments.add(finalSegment)

        if (
            segments.size !=
            operators.size + 1
        ) {
            return null
        }

        return segments.mapIndexed { segmentIndex, segment ->

            ConditionalCommand(
                command = segment,
                operatorBefore =
                    if (segmentIndex == 0) {

                        null

                    } else {

                        operators[
                            segmentIndex - 1
                        ]
                    }
            )
        }
    }

    private enum class QuoteMode {
        NONE,
        SINGLE,
        DOUBLE
    }
}