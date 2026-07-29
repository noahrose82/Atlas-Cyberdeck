package com.noahrose.pocketlab.feature.terminal.environment

object VariableExpander {

    fun expand(text: String): String {

        var expanded = text

        val variables = listOf(
            "USER",
            "HOME",
            "PWD",
            "HOSTNAME",
            "SHELL"
        )

        for (variable in variables) {

            val value =
                EnvironmentVariables.valueOf(variable)
                    ?: continue

            expanded =
                expanded.replace(
                    "\$$variable",
                    value
                )
        }

        return expanded
    }
}