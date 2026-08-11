package com.noahrose.pocketlab.feature.terminal.commands

import com.noahrose.pocketlab.feature.filesystem.VirtualFileSystem
import com.noahrose.pocketlab.feature.terminal.history.CommandHistory
import com.noahrose.pocketlab.feature.terminal.registry.CommandRegistry
import com.noahrose.pocketlab.feature.terminal.handler.CommandHandler
import com.noahrose.pocketlab.feature.terminal.script.ScriptEngine
import com.noahrose.pocketlab.feature.terminal.plugin.PluginRegistry
import com.noahrose.pocketlab.feature.system.VersionInfo
import com.noahrose.pocketlab.feature.terminal.handler.HandlerRegistry
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

            "plugins" -> {

                output.add("Installed Plugins")
                output.add("")

                PluginRegistry
                    .getAll()
                    .forEach { plugin ->

                        output.add(plugin.info.name)
                        output.add("Version : ${plugin.info.version}")
                        output.add("Author  : ${plugin.info.author}")
                        output.add("Description : ${plugin.info.description}")
                        output.add("")
                    }

                return true
            }

            "version" -> {

                output.add(VersionInfo.NAME)
                output.add("Version  : ${VersionInfo.VERSION}")
                output.add("Build    : ${VersionInfo.BUILD}")
                output.add("Codename : ${VersionInfo.CODENAME}")
                output.add("Author   : ${VersionInfo.AUTHOR}")

                return true
            }

            "diagnostics" -> {

                val commandCount =
                    CommandRegistry.getAll().size

                val handlerCount =
                    HandlerRegistry.getAll().size

                val pluginCount =
                    PluginRegistry.getAll().size

                output.add("Atlas Cyberdeck Diagnostics")
                output.add("")
                output.add("Version          : ${VersionInfo.VERSION}")
                output.add("Filesystem       : ONLINE")
                output.add("Command Registry : ONLINE")
                output.add("Handlers         : ONLINE")
                output.add("Plugins          : ONLINE")
                output.add("")
                output.add("Commands         : $commandCount")
                output.add("Handlers         : $handlerCount")
                output.add("Plugins          : $pluginCount")
                output.add("")
                output.add("Overall Status   : HEALTHY")

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
                    "${VersionInfo.NAME} ${VersionInfo.VERSION}"
                )

                output.add(
                    "Build   : ${VersionInfo.BUILD}"
                )

                output.add(
                    "Codename: ${VersionInfo.CODENAME}"
                )

                output.add(
                    "Author  : ${VersionInfo.AUTHOR}"
                )

                return true
            }
        }

        return false
    }
}