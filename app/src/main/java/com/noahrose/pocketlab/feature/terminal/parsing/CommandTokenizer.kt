package com.noahrose.pocketlab.feature.terminal.parsing

object CommandTokenizer {

    /*
     * Standard tokenizer entry point.
     *
     * Returns an empty list when the input
     * contains invalid shell syntax.
     */
    fun tokenize(
        input: String
    ): List<String> {

        return tokenizeOrNull(
            input
        ) ?: emptyList()
    }

    /*
     * Quote-aware tokenizer that returns null
     * when malformed syntax is detected.
     *
     * This allows the terminal processor to
     * distinguish:
     *
     * empty command
     *
     * from:
     *
     * echo "Area 51
     */
    fun tokenizeOrNull(
        input: String
    ): List<String>? {

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
         * An unmatched quotation mark is
         * malformed shell syntax.
         */
        if (insideQuotes) {

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