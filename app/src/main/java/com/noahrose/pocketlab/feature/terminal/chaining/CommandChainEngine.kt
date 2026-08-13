package com.noahrose.pocketlab.feature.terminal.chaining

object CommandChainEngine {

    fun execute(
        command: String,
        executor: (String) -> Unit
    ): Boolean {

        val chain =
            CommandChainParser.parse(command)
                ?: return false

        chain.commands.forEach { chainedCommand ->

            executor(
                chainedCommand
            )
        }

        return true
    }
}