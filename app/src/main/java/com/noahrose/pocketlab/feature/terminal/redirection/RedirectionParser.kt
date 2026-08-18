package com.noahrose.pocketlab.feature.terminal.redirection

import com.noahrose.pocketlab.feature.terminal.parsing.CommandTokenizer

object RedirectionParser {

    fun parse(
        command: String
    ): RedirectionRequest? {

        val trimmed =
            command.trim()

        if (trimmed.isBlank()) {
            return null
        }

        val operatorMatch =
            findRedirectionOperator(
                trimmed
            ) ?: return null

        val commandPart =
            trimmed
                .substring(
                    startIndex = 0,
                    endIndex = operatorMatch.index
                )
                .trim()

        val targetPart =
            trimmed
                .substring(
                    startIndex =
                        operatorMatch.index +
                                operatorMatch.operator.length
                )
                .trim()

        val type =
            when (operatorMatch.operator) {

                ">>" ->
                    RedirectionType.APPEND

                ">" ->
                    RedirectionType.OVERWRITE

                "<" ->
                    RedirectionType.INPUT

                else ->
                    return null
            }

        return createRequest(
            command = commandPart,
            target = targetPart,
            type = type
        )
    }

    private fun createRequest(
        command: String,
        target: String,
        type: RedirectionType
    ): RedirectionRequest? {

        val cleanCommand =
            command.trim()

        if (cleanCommand.isBlank()) {
            return null
        }

        /*
         * Use the shell tokenizer so quoted
         * redirection targets preserve spaces
         * without storing the quotation marks.
         *
         * Examples:
         *
         * > file.txt
         * > "classified notes.txt"
         * > 'classified notes.txt'
         */
        val targetTokens =
            CommandTokenizer.tokenizeOrNull(
                target
            ) ?: return null

        /*
         * Redirection accepts exactly one target.
         */
        if (targetTokens.size != 1) {
            return null
        }

        val cleanTarget =
            targetTokens.first()

        if (cleanTarget.isBlank()) {
            return null
        }

        return RedirectionRequest(
            command = cleanCommand,
            target = cleanTarget,
            type = type
        )
    }

    /*
     * Locate a redirection operator only when
     * it appears outside quoted text.
     *
     * Example:
     *
     * echo "A > B"
     *
     * The > above is text, not redirection.
     */
    private fun findRedirectionOperator(
        command: String
    ): OperatorMatch? {

        var quoteMode =
            QuoteMode.NONE

        var escaping = false
        var index = 0

        while (index < command.length) {

            val character =
                command[index]

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

                quoteMode == QuoteMode.NONE &&
                        character == '>' -> {

                    if (
                        index + 1 < command.length &&
                        command[index + 1] == '>'
                    ) {

                        return OperatorMatch(
                            index = index,
                            operator = ">>"
                        )
                    }

                    return OperatorMatch(
                        index = index,
                        operator = ">"
                    )
                }

                quoteMode == QuoteMode.NONE &&
                        character == '<' -> {

                    return OperatorMatch(
                        index = index,
                        operator = "<"
                    )
                }
            }

            index++
        }

        return null
    }

    private enum class QuoteMode {
        NONE,
        SINGLE,
        DOUBLE
    }

    private data class OperatorMatch(
        val index: Int,
        val operator: String
    )
}