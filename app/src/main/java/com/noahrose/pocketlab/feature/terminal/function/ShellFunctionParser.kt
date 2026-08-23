package com.noahrose.pocketlab.feature.terminal.function

data class ShellFunctionDefinition(
    val name: String,
    val commands: List<String>
)

object ShellFunctionParser {

    fun parse(
        input: String
    ): ShellFunctionDefinition? {

        val trimmed =
            input.trim()

        if (!trimmed.startsWith("function ")) {
            return null
        }

        val openingBrace =
            findOpeningBrace(
                trimmed
            )

        if (openingBrace < 0) {
            return null
        }

        val closingBrace =
            findClosingBrace(
                trimmed
            )

        if (
            closingBrace < 0 ||
            closingBrace <= openingBrace
        ) {
            return null
        }

        /*
         * Nothing except whitespace may appear
         * after the closing brace.
         */
        if (
            trimmed
                .substring(
                    closingBrace + 1
                )
                .isNotBlank()
        ) {
            return null
        }

        val name =
            trimmed
                .substring(
                    "function ".length,
                    openingBrace
                )
                .trim()

        if (!isValidName(name)) {
            return null
        }

        val body =
            trimmed.substring(
                openingBrace + 1,
                closingBrace
            )

        val commands =
            splitCommands(
                body
            ) ?: return null

        if (commands.isEmpty()) {
            return null
        }

        return ShellFunctionDefinition(
            name = name,
            commands = commands
        )
    }

    private fun findOpeningBrace(
        input: String
    ): Int {

        var quoteMode =
            QuoteMode.NONE

        var escaping = false

        input.forEachIndexed { index, character ->

            when {

                escaping -> {
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

                character == '{' &&
                        quoteMode == QuoteMode.NONE -> {

                    return index
                }
            }
        }

        return -1
    }

    private fun findClosingBrace(
        input: String
    ): Int {

        var quoteMode =
            QuoteMode.NONE

        var escaping = false

        for (
        index in input.indices.reversed()
        ) {

            val character =
                input[index]

            /*
             * Closing-brace detection is kept
             * intentionally simple because nested
             * function blocks are not supported.
             */
            if (
                character == '}' &&
                quoteMode == QuoteMode.NONE
            ) {
                return index
            }

            if (escaping) {
                escaping = false
                continue
            }

            when (character) {

                '"' -> {

                    if (
                        quoteMode !=
                        QuoteMode.SINGLE
                    ) {

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
                }

                '\'' -> {

                    if (
                        quoteMode !=
                        QuoteMode.DOUBLE
                    ) {

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
                }
            }
        }

        return -1
    }

    private fun splitCommands(
        body: String
    ): List<String>? {

        val commands =
            mutableListOf<String>()

        val current =
            StringBuilder()

        var quoteMode =
            QuoteMode.NONE

        var escaping = false

        body.forEach { character ->

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

                character == ';' &&
                        quoteMode == QuoteMode.NONE -> {

                    addCommand(
                        current = current,
                        commands = commands
                    )
                }

                else -> {

                    current.append(
                        character
                    )
                }
            }
        }

        if (
            quoteMode != QuoteMode.NONE ||
            escaping
        ) {
            return null
        }

        addCommand(
            current = current,
            commands = commands
        )

        return commands
    }

    private fun addCommand(
        current: StringBuilder,
        commands: MutableList<String>
    ) {

        val command =
            current
                .toString()
                .trim()

        if (command.isNotBlank()) {

            commands.add(
                command
            )
        }

        current.clear()
    }

    private fun isValidName(
        name: String
    ): Boolean {

        if (name.isBlank()) {
            return false
        }

        if (
            !name.first().isLetter() &&
            name.first() != '_'
        ) {
            return false
        }

        return name.all { character ->

            character.isLetterOrDigit() ||
                    character == '_'
        }
    }

    private enum class QuoteMode {
        NONE,
        SINGLE,
        DOUBLE
    }
}