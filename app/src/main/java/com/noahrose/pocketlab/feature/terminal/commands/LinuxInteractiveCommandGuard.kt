package com.noahrose.pocketlab.feature.linux.runtime.command

object LinuxInteractiveCommandGuard {

    private val interactiveCommands =
        setOf(
            "nano",
            "vi",
            "vim",
            "top",
            "htop",
            "less",
            "more",
            "man",
            "passwd",
            "ssh",
            "sftp",
            "ftp",
            "telnet",
            "su",
            "login"
        )

    fun requiresPty(
        command: String
    ): Boolean {

        val trimmed =
            command.trim()

        if (trimmed.isBlank()) {
            return false
        }

        val commandName =
            trimmed
                .substringBefore(" ")
                .substringAfterLast("/")
                .lowercase()

        return commandName in
                interactiveCommands
    }

    fun message(
        command: String
    ): String {

        val commandName =
            command
                .trim()
                .substringBefore(" ")
                .substringAfterLast("/")

        return buildString {

            appendLine(
                "$commandName requires an interactive terminal."
            )

            append(
                "Atlas Ubuntu shell does not support PTY applications yet."
            )
        }
    }
}