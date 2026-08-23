package com.noahrose.pocketlab.feature.terminal.function

object ShellFunctions {

    /*
     * Runtime shell functions.
     *
     * Each function has a name and a list
     * of commands that make up its body.
     *
     * Example:
     *
     * status {
     *     echo "Atlas Cyberdeck"
     *     echo $MODE
     *     pwd
     * }
     */
    private val functions =
        mutableMapOf<String, List<String>>()

    fun define(
        name: String,
        commands: List<String>
    ): Boolean {

        val cleanName =
            name.trim()

        val cleanCommands =
            commands
                .map { command ->
                    command.trim()
                }
                .filter { command ->
                    command.isNotBlank()
                }

        if (
            !isValidName(cleanName) ||
            cleanCommands.isEmpty()
        ) {
            return false
        }

        functions[cleanName] =
            cleanCommands

        return true
    }

    fun remove(
        name: String
    ): Boolean {

        return functions.remove(
            name.trim()
        ) != null
    }

    fun exists(
        name: String
    ): Boolean {

        return functions.containsKey(
            name.trim()
        )
    }

    fun get(
        name: String
    ): List<String>? {

        return functions[
            name.trim()
        ]?.toList()
    }

    fun getAll(): Map<String, List<String>> {

        return functions
            .mapValues { (_, commands) ->
                commands.toList()
            }
    }

    fun clear() {

        functions.clear()
    }

    private fun isValidName(
        name: String
    ): Boolean {

        if (name.isBlank()) {
            return false
        }

        /*
         * Function names follow the same
         * basic rules as shell identifiers.
         *
         * Valid:
         *
         * status
         * atlas_status
         * status51
         *
         * Invalid:
         *
         * 51status
         * atlas-status
         */
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