package com.noahrose.pocketlab.feature.terminal.parsing

object CommandTokenizer {

    private enum class QuoteMode {
        NONE,
        SINGLE,
        DOUBLE
    }

    fun tokenize(
        input: String
    ): List<String> {

        return tokenizeOrNull(
            input
        ) ?: emptyList()
    }

    fun tokenizeOrNull(
        input: String
    ): List<String>? {

        val tokens =
            mutableListOf<String>()

        val current =
            StringBuilder()

        var quoteMode =
            QuoteMode.NONE

        var escaping = false

        input.forEach { character ->

            when {

                escaping -> {

                    current.append(
                        character
                    )

                    escaping = false
                }

                character == '\\' &&
                        quoteMode != QuoteMode.SINGLE -> {

                    escaping = true
                }

                character == '"' &&
                        quoteMode != QuoteMode.SINGLE -> {

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

                    quoteMode =
                        if (
                            quoteMode == QuoteMode.SINGLE
                        ) {
                            QuoteMode.NONE
                        } else {
                            QuoteMode.SINGLE
                        }
                }

                character.isWhitespace() &&
                        quoteMode == QuoteMode.NONE -> {

                    if (current.isNotEmpty()) {

                        tokens.add(
                            current.toString()
                        )

                        current.clear()
                    }
                }

                else -> {

                    current.append(
                        character
                    )
                }
            }
        }

        if (escaping) {

            current.append('\\')
        }

        if (quoteMode != QuoteMode.NONE) {

            return null
        }

        if (current.isNotEmpty()) {

            tokens.add(
                current.toString()
            )
        }

        return tokens
    }
}