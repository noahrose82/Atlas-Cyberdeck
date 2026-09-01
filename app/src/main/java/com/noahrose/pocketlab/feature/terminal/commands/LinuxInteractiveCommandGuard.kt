package com.noahrose.pocketlab.feature.linux.runtime.command

object LinuxInteractiveCommandGuard {

    /*
     * Commands that normally require a real terminal
     * attached to a PTY.
     *
     * Atlas blocks the interactive forms until the
     * Ubuntu shell gains PTY support.
     */
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

    /*
     * Some applications that are normally interactive
     * provide informational options that do NOT require
     * an interactive terminal.
     *
     * Examples:
     *
     * nano --version
     * vim --version
     * vim --help
     * ssh -V
     */
    private val nonInteractiveOptions =
        setOf(
            "--version",
            "--help",
            "-h",
            "-v"
        )

    fun requiresPty(
        command: String
    ): Boolean {

        val trimmed =
            command.trim()

        if (
            trimmed.isBlank()
        ) {
            return false
        }

        val parts =
            trimmed
                .split(
                    Regex("\\s+")
                )

        val commandName =
            parts
                .first()
                .substringAfterLast("/")
                .lowercase()

        /*
         * Commands Atlas does not classify as
         * interactive are allowed normally.
         */
        if (
            commandName !in
            interactiveCommands
        ) {
            return false
        }

        /*
         * A bare interactive application still
         * requires a PTY.
         *
         * Examples:
         *
         * nano
         * vim
         * top
         */
        if (
            parts.size == 1
        ) {
            return true
        }

        val arguments =
            parts.drop(1)

        /*
         * Permit simple informational invocations.
         *
         * These commands print information and exit
         * without entering their interactive interface.
         */
        if (
            arguments.size == 1 &&
            arguments.first().lowercase() in
            nonInteractiveOptions
        ) {
            return false
        }

        /*
         * Everything else from the interactive command
         * set remains blocked until Atlas has real PTY
         * support.
         *
         * Examples:
         *
         * nano file.txt
         * vim file.txt
         * ssh user@host
         * top
         */
        return true
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