package com.noahrose.pocketlab.feature.terminal.alias

object CommandAliases {

    private val aliases = mapOf(

        "ll" to "ls",

        "dir" to "ls",

        "cls" to "clear",

        "md" to "mkdir",

        "rd" to "rmdir"
    )

    fun resolve(command: String): String {

        val parts =
            command.trim().split(
                Regex("\\s+"),
                limit = 2
            )

        if (parts.isEmpty()) {
            return command
        }

        val resolvedCommand =
            aliases[parts[0]] ?: parts[0]

        return if (parts.size == 2) {

            "$resolvedCommand ${parts[1]}"

        } else {

            resolvedCommand
        }
    }
}