package com.noahrose.pocketlab.feature.terminal.registry

object CommandRegistry {

    private val commands =
        mutableListOf<CommandInfo>()

    init {
        registerBuiltInCommands()
    }

    fun register(command: CommandInfo) {

        if (find(command.name) == null) {
            commands.add(command)
        }
    }

    fun getAll(): List<CommandInfo> {

        return commands.sortedBy {
            it.name
        }
    }

    fun find(name: String): CommandInfo? {

        return commands.firstOrNull {
            it.name.equals(
                name,
                ignoreCase = true
            )
        }
    }

    private fun registerBuiltInCommands() {

        val builtIns = listOf(

            CommandInfo(
                name = "cat",
                description = "Display file contents",
                usage = "cat <filename>",
                category = "Filesystem"
            ),

            CommandInfo(
                name = "cd",
                description = "Change the current directory",
                usage = "cd <directory>",
                category = "Filesystem"
            ),

            CommandInfo(
                name = "clear",
                description = "Clear terminal output",
                usage = "clear",
                category = "Utility"
            ),

            CommandInfo(
                name = "cp",
                description = "Copy files",
                usage = "cp <source...> <destination>",
                category = "Filesystem"
            ),

            CommandInfo(
                name = "echo",
                description = "Display text or write text to a file",
                usage = "echo <text>",
                category = "Filesystem"
            ),

            CommandInfo(
                name = "find",
                description = "Search the virtual filesystem",
                usage = "find <name>",
                category = "Filesystem"
            ),

            CommandInfo(
                name = "grep",
                description = "Search text for matching content",
                usage = "grep <text> <filename>",
                category = "Text"
            ),

            CommandInfo(
                name = "head",
                description = "Display the first lines of a file",
                usage = "head <filename>",
                category = "Text"
            ),

            CommandInfo(
                name = "help",
                description = "Display available terminal commands",
                usage = "help",
                category = "Utility"
            ),

            CommandInfo(
                name = "history",
                description = "Display command history",
                usage = "history",
                category = "Utility"
            ),

            CommandInfo(
                name = "ls",
                description = "List directory contents",
                usage = "ls",
                category = "Filesystem"
            ),

            CommandInfo(
                name = "mkdir",
                description = "Create a directory",
                usage = "mkdir <directory>",
                category = "Filesystem"
            ),

            CommandInfo(
                name = "mv",
                description = "Move or rename files",
                usage = "mv <source...> <destination>",
                category = "Filesystem"
            ),

            CommandInfo(
                name = "linux",
                description = "Manage the Linux runtime session",
                usage = "linux [status|start|stop]",
                category = "Linux"
            ),

            CommandInfo(
                name = "neofetch",
                description = "Display Atlas Cyberdeck system information",
                usage = "neofetch",
                category = "System"
            ),

            CommandInfo(
                name = "pwd",
                description = "Display the current working directory",
                usage = "pwd",
                category = "Filesystem"
            ),

            CommandInfo(
                name = "rm",
                description = "Delete files",
                usage = "rm <filename...>",
                category = "Filesystem"
            ),

            CommandInfo(
                name = "rmdir",
                description = "Remove an empty directory",
                usage = "rmdir <directory>",
                category = "Filesystem"
            ),

            CommandInfo(
                name = "sort",
                description = "Sort lines of text",
                usage = "sort <filename>",
                category = "Text"
            ),

            CommandInfo(
                name = "status",
                description = "Display Atlas Cyberdeck status",
                usage = "status",
                category = "System"
            ),

            CommandInfo(
                name = "tail",
                description = "Display the last lines of a file",
                usage = "tail <filename>",
                category = "Text"
            ),

            CommandInfo(
                name = "touch",
                description = "Create an empty file",
                usage = "touch <filename>",
                category = "Filesystem"
            ),

            CommandInfo(
                name = "tree",
                description = "Display the directory tree",
                usage = "tree",
                category = "Filesystem"
            ),

            CommandInfo(
                name = "uniq",
                description = "Remove duplicate lines",
                usage = "uniq <filename>",
                category = "Text"
            ),

            CommandInfo(
                name = "wc",
                description = "Count lines, words, and characters",
                usage = "wc <filename>",
                category = "Text"
            ),

            CommandInfo(
                name = "whoami",
                description = "Display the current terminal user",
                usage = "whoami",
                category = "Utility"
            ),

            CommandInfo(
                name = "runscript",
                description = "Execute an Atlas shell script",
                usage = "runscript <script.ash>",
                category = "System"
            ),

            CommandInfo(
                name = "plugins",
                description = "Display installed plugins",
                usage = "plugins",
                category = "System"
            ),

            CommandInfo(
                name = "diagnostics",
                description = "Display Atlas Cyberdeck diagnostic information",
                usage = "diagnostics",
                category = "System"
            ),

            CommandInfo(
                name = "version",
                description = "Display Atlas Cyberdeck version information",
                usage = "version",
                category = "System"
            )

        )

        builtIns.forEach { command ->
            register(command)
        }
    }
}