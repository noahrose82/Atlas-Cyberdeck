package com.noahrose.pocketlab.feature.terminal.parsing

object CommandTokenizer {

    fun tokenize(
        input: String
    ): List<String> {

        val tokens =
            mutableListOf<String>()

        val current =
            StringBuilder()

        var insideQuotes = false

        input.forEach { character ->

            when {

                character == '"' -> {

                    insideQuotes =
                        !insideQuotes
                }

                character.isWhitespace() &&
                        !insideQuotes -> {

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

        if (current.isNotEmpty()) {

            tokens.add(
                current.toString()
            )
        }

        return tokens
    }
}