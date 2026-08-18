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
        var escaping = false

        input.forEach { character ->

            when {

                escaping -> {

                    current.append(
                        character
                    )

                    escaping = false
                }

                character == '\\' -> {

                    escaping = true
                }

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

        /*
         * Preserve a trailing backslash rather
         * than silently discarding it.
         */
        if (escaping) {

            current.append('\\')
        }

        /*
         * Unmatched quotation marks represent
         * malformed shell input.
         */
        if (insideQuotes) {

            return emptyList()
        }

        if (current.isNotEmpty()) {

            tokens.add(
                current.toString()
            )
        }

        return tokens
    }
}