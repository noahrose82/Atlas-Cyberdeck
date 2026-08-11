package com.noahrose.pocketlab.feature.terminal.dispatch

import com.noahrose.pocketlab.feature.terminal.commands.DirectoryCommands
import com.noahrose.pocketlab.feature.terminal.commands.FileCommands
import com.noahrose.pocketlab.feature.terminal.commands.TextCommands
import com.noahrose.pocketlab.feature.terminal.commands.UtilityCommands

object CommandDispatcher {

    fun dispatch(
        commandName: String,
        parts: List<String>,
        output: MutableList<String>
    ): Boolean {

        if (
            UtilityCommands.handle(
                commandName = commandName,
                output = output
            )
        ) {
            return true
        }

        if (
            FileCommands.handle(
                commandName = commandName,
                parts = parts,
                output = output
            )
        ) {
            return true
        }

        if (
            DirectoryCommands.handle(
                commandName = commandName,
                parts = parts,
                output = output
            )
        ) {
            return true
        }

        if (
            TextCommands.handle(
                commandName = commandName,
                parts = parts,
                output = output
            )
        ) {
            return true
        }

        return false
    }
}