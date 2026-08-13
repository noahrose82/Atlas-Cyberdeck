package com.noahrose.pocketlab.feature.terminal.execution

data class CommandResult(
    val handled: Boolean,
    val success: Boolean,
    val exitCode: Int
) {

    companion object {

        fun success(): CommandResult {
            return CommandResult(
                handled = true,
                success = true,
                exitCode = 0
            )
        }

        fun failure(
            exitCode: Int = 1
        ): CommandResult {
            return CommandResult(
                handled = true,
                success = false,
                exitCode = exitCode
            )
        }

        fun notHandled(): CommandResult {
            return CommandResult(
                handled = false,
                success = false,
                exitCode = 127
            )
        }
    }
}