package com.noahrose.pocketlab.feature.terminal.completion

object CommandCompletion {

    private val commands =
        listOf(

            "help",
            "history",
            "clear",
            "whoami",
            "pwd",
            "ls",
            "tree",
            "find",
            "cp",
            "mv",
            "grep",
            "head",
            "tail",
            "mkdir",
            "touch",
            "cat",
            "echo",
            "rm",
            "rmdir",
            "cd",
            "status",
            "neofetch"

        )

    fun complete(
        input: String
    ): String? {

        val prefix =
            input.trim()

        if (prefix.isBlank()) {
            return null
        }

        val matches =
            commands.filter {

                it.startsWith(
                    prefix,
                    ignoreCase = true
                )
            }

        return when {

            matches.isEmpty() ->
                null

            matches.size == 1 ->
                matches.first()

            else ->
                null
        }
    }
}