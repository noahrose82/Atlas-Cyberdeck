package com.noahrose.pocketlab.feature.terminal.chaining

import com.noahrose.pocketlab.feature.terminal.execution.ExecutionStatus

object CommandChainEngine {

    fun execute(
        command: String,
        executor: (String) -> Unit
    ): Boolean {

        val chain =
            CommandChainParser.parse(command)
                ?: return false

        chain.commands.forEach { chainedCommand ->

            ExecutionStatus.set(0)

            executor(
                chainedCommand
            )

            if (!ExecutionStatus.wasSuccessful()) {
                return true
            }
        }

        return true
    }
}