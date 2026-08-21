package com.noahrose.pocketlab.feature.terminal.environment

import com.noahrose.pocketlab.feature.filesystem.VirtualFileSystem
import com.noahrose.pocketlab.feature.terminal.persistence.ShellConfigPersistence

object EnvironmentVariables {

    /*
     * User-defined environment variables.
     */
    private val userVariables =
        mutableMapOf<String, String>()

    fun valueOf(
        name: String
    ): String? {

        val normalizedName =
            name
                .trim()
                .uppercase()

        /*
         * User variables take priority.
         */
        userVariables[normalizedName]
            ?.let {
                return it
            }

        return when (normalizedName) {

            "USER" ->
                "atlas"

            "HOME" ->
                "/home/atlas"

            "PWD" ->
                VirtualFileSystem
                    .currentPath
                    .value
                    .replace(
                        "~",
                        "/home/atlas"
                    )

            "HOSTNAME" ->
                "cyberdeck"

            "SHELL" ->
                "/bin/atlas"

            else ->
                null
        }
    }

    fun set(
        name: String,
        value: String
    ): Boolean {

        val cleanName =
            name
                .trim()
                .uppercase()

        if (
            cleanName.isBlank() ||
            !isValidName(cleanName)
        ) {
            return false
        }

        userVariables[cleanName] =
            value

        persist()

        return true
    }

    fun remove(
        name: String
    ): Boolean {

        val cleanName =
            name
                .trim()
                .uppercase()

        val removed =
            userVariables.remove(
                cleanName
            ) != null

        if (removed) {
            persist()
        }

        return removed
    }

    fun getUserVariables(): Map<String, String> {

        return userVariables.toMap()
    }

    /*
     * Used during application startup.
     *
     * Restores variables without immediately
     * rewriting the persistence file.
     */
    fun restoreUserVariables(
        variables: Map<String, String>
    ) {

        userVariables.clear()

        variables.forEach { (name, value) ->

            val cleanName =
                name
                    .trim()
                    .uppercase()

            if (
                cleanName.isNotBlank() &&
                isValidName(cleanName)
            ) {

                userVariables[cleanName] =
                    value
            }
        }
    }

    fun clearUserVariables() {

        userVariables.clear()

        persist()
    }

    private fun persist() {

        ShellConfigPersistence
            .saveEnvironmentVariables(
                userVariables
            )
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
}