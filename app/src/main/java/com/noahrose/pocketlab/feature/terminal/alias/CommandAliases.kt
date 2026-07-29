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

        val parts = command.trim().split(
            Regex("\\s+"),
            limit = 2
        )

        val alias = aliases[parts[0].lowercase()]

        return if (alias == null) {
            command
        } else {
            if (parts.size == 1) {
                alias
            } else {
                "$alias ${parts[1]}"
            }
        }
    }
}