package com.noahrose.pocketlab.feature.terminal.commands

import com.noahrose.pocketlab.feature.filesystem.VirtualFileSystem
import com.noahrose.pocketlab.feature.terminal.history.CommandHistory
import com.noahrose.pocketlab.feature.terminal.registry.CommandRegistry
import com.noahrose.pocketlab.feature.terminal.handler.CommandHandler
import com.noahrose.pocketlab.feature.terminal.script.ScriptEngine
object UtilityCommands : CommandHandler {

    override fun handle(
        commandName: String,
        parts: List<String>,
        output: MutableList<String>
    ): Boolean {

        when (commandName) {

            "help" -> {

                output.add("Available commands:")
                output.add("")

                CommandRegistry
                    .getAll()
                    .groupBy { command ->
                        command.category
                    }
                    .toSortedMap()
                    .forEach { (category, commands) ->

                        output.add(category)
                        output.add("-".repeat(category.length))

                        commands.forEach { command ->

                            output.add(
                                "${command.name} - ${command.description}"
                            )
                        }

                        output.add("")
                    }

                return true
            }

            "runscript" -> {

                if (
                    parts.size < 2 ||
                    parts[1].isBlank()
                ) {
                    output.add(
                        "Usage: runscript <script.ash>"
                    )

                    return true
                }

                val scriptName =
                    parts[1].trim()

                if (
                    !scriptName.endsWith(
                        ".ash",
                        ignoreCase = true
                    )
                ) {
                    output.add(
                        "runscript: '$scriptName': Expected an .ash script"
                    )

                    return true
                }

                val scriptContent =
                    VirtualFileSystem.readFile(
                        scriptName
                    )

                if (scriptContent == null) {

                    output.add(
                        "runscript: '$scriptName': Script not found"
                    )

                    return true
                }

                output.add(
                    "Executing script: $scriptName"
                )

                ScriptEngine.execute(
                    script = scriptContent.lines(),
                    output = output
                )

                return true
            }

            "history" -> {

                val history =
                    CommandHistory.getHistory()

                if (history.isEmpty()) {

                    output.add(
                        "No commands in history."
                    )

                } else {

                    history.forEachIndexed { index, command ->

                        output.add(
                            "${index + 1}  $command"
                        )
                    }
                }

                return true
            }

            "clear" -> {

                output.clear()

                return true
            }

            "whoami" -> {

                output.add("atlas")

                return true
            }

            "pwd" -> {

                output.add(
                    VirtualFileSystem
                        .currentPath
                        .value
                        .replace(
                            "~",
                            "/home/atlas"
                        )
                )

                return true
            }

            "status" -> {

                output.add("Atlas Cyberdeck")
                output.add("Status : ONLINE")
                output.add("Linux : INSTALLED")
                output.add("Terminal : ACTIVE")

                return true
            }

            "neofetch" -> {

                output.add(
                    "Atlas Cyberdeck v0.14.0-alpha"
                )
                output.add(
                    "OS      : Atlas Linux"
                )
                output.add(
                    "Kernel  : 6.1"
                )
                output.add(
                    "Shell   : Atlas Terminal"
                )
                output.add(
                    "User    : atlas"
                )

                return true
            }
        }

        return false
    }
}