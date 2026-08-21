package com.noahrose.pocketlab.feature.terminal.alias

import com.noahrose.pocketlab.feature.terminal.persistence.ShellConfigPersistence

object CommandAliases {

    /*
     * Built-in Atlas aliases.
     */
    private val builtInAliases =
        mapOf(
            "ll" to "ls",
            "dir" to "ls",
            "cls" to "clear",
            "md" to "mkdir",
            "rd" to "rmdir"
        )

    /*
     * User-defined aliases.
     */
    private val userAliases =
        mutableMapOf<String, String>()

    fun resolve(
        command: String
    ): String {

        val trimmedCommand =
            command.trim()

        if (trimmedCommand.isBlank()) {
            return command
        }

        val parts =
            trimmedCommand.split(
                Regex("\\s+"),
                limit = 2
            )

        val commandName =
            parts.first()

        /*
         * User aliases override built-ins.
         */
        val resolvedCommand =
            userAliases[commandName]
                ?: builtInAliases[commandName]
                ?: commandName

        return if (parts.size == 2) {

            "$resolvedCommand ${parts[1]}"

        } else {

            resolvedCommand
        }
    }

    fun setAlias(
        name: String,
        command: String
    ): Boolean {

        val cleanName =
            name.trim()

        val cleanCommand =
            command.trim()

        if (
            cleanName.isBlank() ||
            cleanCommand.isBlank()
        ) {
            return false
        }

        userAliases[cleanName] =
            cleanCommand

        persist()

        return true
    }

    fun removeAlias(
        name: String
    ): Boolean {

        val removed =
            userAliases.remove(
                name.trim()
            ) != null

        if (removed) {
            persist()
        }

        return removed
    }

    fun getAlias(
        name: String
    ): String? {

        val cleanName =
            name.trim()

        return userAliases[cleanName]
            ?: builtInAliases[cleanName]
    }

    fun getAllAliases(): Map<String, String> {

        return buildMap {

            putAll(
                builtInAliases
            )

            putAll(
                userAliases
            )
        }
    }

    fun getUserAliases(): Map<String, String> {

        return userAliases.toMap()
    }

    /*
     * Used during application startup.
     *
     * Restores aliases without immediately
     * writing the same data back to disk.
     */
    fun restoreUserAliases(
        aliases: Map<String, String>
    ) {

        userAliases.clear()

        aliases.forEach { (name, command) ->

            val cleanName =
                name.trim()

            val cleanCommand =
                command.trim()

            if (
                cleanName.isNotBlank() &&
                cleanCommand.isNotBlank()
            ) {

                userAliases[cleanName] =
                    cleanCommand
            }
        }
    }

    fun clearUserAliases() {

        userAliases.clear()

        persist()
    }

    private fun persist() {

        ShellConfigPersistence.saveAliases(
            userAliases
        )
    }
}