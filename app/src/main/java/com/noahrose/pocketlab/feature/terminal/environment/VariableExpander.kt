package com.noahrose.pocketlab.feature.terminal.environment

import com.noahrose.pocketlab.feature.terminal.execution.ExecutionStatus

object VariableExpander {

    private val variablePattern =
        Regex("""\$([A-Za-z_][A-Za-z0-9_]*)""")

    fun expand(
        text: String
    ): String {

        var expanded = text

        /*
         * Special shell variable:
         *
         * $? = exit code of the previous command
         */
        if (expanded.contains("\$?")) {

            expanded =
                expanded.replace(
                    "\$?",
                    ExecutionStatus
                        .get()
                        .toString()
                )
        }

        /*
         * Expand all normal shell-style variables.
         *
         * Examples:
         *
         * $USER
         * $HOME
         * $PROJECT
         * $ATLAS_PROJECT
         */
        expanded =
            variablePattern.replace(
                expanded
            ) { matchResult ->

                val variableName =
                    matchResult
                        .groupValues[1]

                EnvironmentVariables.valueOf(
                    variableName
                ) ?: matchResult.value
            }

        return expanded
    }
}