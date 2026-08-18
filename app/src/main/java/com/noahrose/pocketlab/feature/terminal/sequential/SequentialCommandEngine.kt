package com.noahrose.pocketlab.feature.terminal.sequential

object SequentialCommandEngine {

    fun execute(
        command: String,
        executor: (String) -> Unit
    ): Boolean {

        val commands =
            SequentialCommandParser.parse(
                command
            ) ?: return false

        commands.forEach { sequentialCommand ->

            executor(
                sequentialCommand
            )
        }

        return true
    }
}