package com.noahrose.pocketlab.feature.terminal.redirection

object RedirectionParser {

    fun parse(
        command: String
    ): RedirectionRequest? {

        val trimmed =
            command.trim()

        if (trimmed.isBlank()) {
            return null
        }

        return when {

            trimmed.contains(">>") -> {

                val parts =
                    trimmed.split(
                        ">>",
                        limit = 2
                    )

                createRequest(
                    command = parts[0],
                    target = parts[1],
                    type = RedirectionType.APPEND
                )
            }

            trimmed.contains(">") -> {

                val parts =
                    trimmed.split(
                        ">",
                        limit = 2
                    )

                createRequest(
                    command = parts[0],
                    target = parts[1],
                    type = RedirectionType.OVERWRITE
                )
            }

            trimmed.contains("<") -> {

                val parts =
                    trimmed.split(
                        "<",
                        limit = 2
                    )

                createRequest(
                    command = parts[0],
                    target = parts[1],
                    type = RedirectionType.INPUT
                )
            }

            else -> null
        }
    }

    private fun createRequest(
        command: String,
        target: String,
        type: RedirectionType
    ): RedirectionRequest? {

        val cleanCommand =
            command.trim()

        val cleanTarget =
            target.trim()

        if (
            cleanCommand.isBlank() ||
            cleanTarget.isBlank()
        ) {
            return null
        }

        return RedirectionRequest(
            command = cleanCommand,
            target = cleanTarget,
            type = type
        )
    }
}