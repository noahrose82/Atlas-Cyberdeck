package com.noahrose.pocketlab.feature.terminal.commands

import com.noahrose.pocketlab.feature.filesystem.VirtualFileSystem
import com.noahrose.pocketlab.feature.terminal.history.CommandHistory

object UtilityCommands {

    fun handle(
        commandName: String,
        output: MutableList<String>
    ): Boolean {

        when (commandName) {

            "help" -> {

                output.add("Available commands:")
                output.add("")
                output.add("tree")
                output.add("help")
                output.add("history")
                output.add("!!")
                output.add("!<number>")
                output.add("cp")
                output.add("mv")
                output.add("find")
                output.add("clear")
                output.add("whoami")
                output.add("pwd")
                output.add("ls")
                output.add("grep")
                output.add("head")
                output.add("tail")
                output.add("mkdir")
                output.add("touch")
                output.add("cat")
                output.add("echo")
                output.add("rmdir")
                output.add("rm")
                output.add("status")
                output.add("neofetch")

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
                    VirtualFileSystem.currentPath.value.replace(
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

                output.add("Atlas Cyberdeck v0.10.0-alpha")
                output.add("OS      : Atlas Linux")
                output.add("Kernel  : 6.1")
                output.add("Shell   : Atlas Terminal")
                output.add("User    : atlas")

                return true
            }
        }

        return false
    }
}