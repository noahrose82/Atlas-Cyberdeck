package com.noahrose.pocketlab.feature.terminal.handler

import com.noahrose.pocketlab.feature.terminal.commands.DirectoryCommands
import com.noahrose.pocketlab.feature.terminal.commands.FileCommands
import com.noahrose.pocketlab.feature.terminal.commands.FunctionCommands
import com.noahrose.pocketlab.feature.terminal.commands.ShellConfigCommands
import com.noahrose.pocketlab.feature.terminal.commands.TextCommands
import com.noahrose.pocketlab.feature.terminal.commands.UtilityCommands

object HandlerRegistry {

    private val handlers =
        mutableListOf<CommandHandler>()

    init {

        register(UtilityCommands)
        register(ShellConfigCommands)
        register(FunctionCommands)
        register(FileCommands)
        register(DirectoryCommands)
        register(TextCommands)
    }

    fun register(
        handler: CommandHandler
    ) {

        if (!handlers.contains(handler)) {

            handlers.add(
                handler
            )
        }
    }

    fun getAll(): List<CommandHandler> {

        return handlers.toList()
    }
}