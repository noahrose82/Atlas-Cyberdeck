package com.noahrose.pocketlab.feature.terminal.chaining

object ConditionalChainParser {

    fun parse(
        command: String
    ): List<ConditionalCommand>? {

        if (
            !command.contains("&&") &&
            !command.contains("||")
        ) {
            return null
        }

        val tokens =
            Regex("""\s*(&&|\|\|)\s*""")
                .split(command)
                .map {
                    it.trim()
                }

        val operators =
            Regex("""&&|\|\|""")
                .findAll(command)
                .map { match ->
                    match.value
                }
                .toList()

        if (tokens.size < 2) {
            return null
        }

        val result =
            mutableListOf<ConditionalCommand>()

        tokens.forEachIndexed { index, token ->

            if (token.isBlank()) {
                return null
            }

            val operatorBefore =
                if (index == 0) {

                    null

                } else {

                    when (operators[index - 1]) {

                        "&&" ->
                            ConditionalOperator.AND

                        "||" ->
                            ConditionalOperator.OR

                        else ->
                            return null
                    }
                }

            result.add(
                ConditionalCommand(
                    command = token,
                    operatorBefore = operatorBefore
                )
            )
        }

        return result
    }
}