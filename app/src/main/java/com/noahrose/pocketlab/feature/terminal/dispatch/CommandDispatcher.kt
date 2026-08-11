package com.noahrose.pocketlab.feature.terminal.dispatch

import com.noahrose.pocketlab.feature.terminal.handler.HandlerRegistry

object CommandDispatcher {

    fun dispatch(
        commandName: String,
        parts: List<String>,
        output: MutableList<String>
    ): Boolean {

        HandlerRegistry
            .getAll()
            .forEach { handler ->

                if (
                    handler.handle(
                        commandName = commandName,
                        parts = parts,
                        output = output
                    )
                ) {
                    return true
                }
            }

        return false
    }
}