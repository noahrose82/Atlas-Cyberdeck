package com.noahrose.pocketlab.feature.terminal.environment

import com.noahrose.pocketlab.feature.terminal.execution.ExecutionStatus

object VariableExpander {

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

        val variables =
            listOf(
                "USER",
                "HOME",
                "PWD",
                "HOSTNAME",
                "SHELL"
            )

        for (variable in variables) {

            val value =
                EnvironmentVariables.valueOf(
                    variable
                ) ?: continue

            expanded =
                expanded.replace(
                    "\$$variable",
                    value
                )
        }

        return expanded
    }
}