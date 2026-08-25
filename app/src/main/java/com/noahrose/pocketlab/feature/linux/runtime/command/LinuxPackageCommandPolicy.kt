package com.noahrose.pocketlab.feature.linux.runtime.command

sealed interface LinuxPackageCommandPreparation {

    data class Ready(
        val command: String,
        val hardened: Boolean
    ) : LinuxPackageCommandPreparation

    data class Blocked(
        val message: String
    ) : LinuxPackageCommandPreparation
}

object LinuxPackageCommandPolicy {

    private val packageExecutables =
        setOf(
            "apt",
            "apt-get",
            "apt-cache",
            "dpkg"
        )

    /*
     * These APT operations normally ask for package
     * confirmation unless -y / --assume-yes is used.
     *
     * Atlas does not expose a PTY yet, so allowing one
     * of these commands to wait for Y/n input would leave
     * the terminal stuck in Running... until timeout or
     * runtime shutdown.
     */
    private val aptMutationActions =
        setOf(
            "install",
            "remove",
            "purge",
            "upgrade",
            "full-upgrade",
            "dist-upgrade",
            "autoremove",
            "build-dep",
            "satisfy",
            "dselect-upgrade"
        )

    fun prepare(
        command: String
    ): LinuxPackageCommandPreparation {

        val trimmedCommand =
            command.trim()

        if (
            trimmedCommand.isBlank()
        ) {

            return LinuxPackageCommandPreparation
                .Ready(
                    command =
                        command,

                    hardened =
                        false
                )
        }

        val invocations =
            findPackageInvocations(
                trimmedCommand
            )

        if (
            invocations.isEmpty()
        ) {

            return LinuxPackageCommandPreparation
                .Ready(
                    command =
                        command,

                    hardened =
                        false
                )
        }

        val interactiveAptInvocation =
            invocations
                .firstOrNull { invocation ->

                    invocation.requiresAssumeYes() &&
                            !invocation.hasAssumeYes()
                }

        if (
            interactiveAptInvocation != null
        ) {

            return LinuxPackageCommandPreparation
                .Blocked(
                    message =
                        "${interactiveAptInvocation.executable} " +
                                "${interactiveAptInvocation.mutationAction()} " +
                                "requires -y or --assume-yes until " +
                                "Atlas PTY input support is available."
                )
        }

        /*
         * Run package commands inside a subshell so these
         * environment values apply only to this command.
         *
         * This prevents debconf-driven packages such as
         * tzdata from opening a prompt that Atlas cannot
         * answer yet.
         *
         * The user's command remains untouched inside the
         * subshell. Explicit command-local assignments,
         * for example:
         *
         * TZ=America/Phoenix apt install -y tzdata
         *
         * still override the Atlas defaults for that
         * invocation.
         */
        val hardenedCommand =
            buildString {

                append("( ")
                append("export DEBIAN_FRONTEND=noninteractive; ")
                append("export DEBCONF_NONINTERACTIVE_SEEN=true; ")
                append("export APT_LISTCHANGES_FRONTEND=none; ")
                append("export TZ=Etc/UTC; ")
                append(trimmedCommand)
                append(" )")
            }

        return LinuxPackageCommandPreparation
            .Ready(
                command =
                    hardenedCommand,

                hardened =
                    true
            )
    }

    fun isPackageManagementCommand(
        command: String
    ): Boolean {

        return findPackageInvocations(
            command
        ).isNotEmpty()
    }

    private fun findPackageInvocations(
        command: String
    ): List<PackageInvocation> {

        return splitShellSegments(
            command
        )
            .mapNotNull { segment ->

                parsePackageInvocation(
                    segment
                )
            }
    }

    private fun parsePackageInvocation(
        segment: String
    ): PackageInvocation? {

        val tokens =
            tokenizeShellSegment(
                segment
            )

        if (
            tokens.isEmpty()
        ) {

            return null
        }

        var index =
            0

        while (
            index < tokens.size &&
            isEnvironmentAssignment(
                tokens[index]
            )
        ) {

            index++
        }

        if (
            index >= tokens.size
        ) {

            return null
        }

        var token =
            normalizeCommandToken(
                tokens[index]
            )

        /*
         * Support common wrappers without making them a
         * requirement. Ubuntu runs as root, so sudo is not
         * normally needed, but recognizing it makes the
         * policy less surprising.
         */
        if (
            token.equals(
                "sudo",
                ignoreCase = true
            )
        ) {

            index++

            while (
                index < tokens.size &&
                tokens[index].startsWith("-")
            ) {

                index++
            }

            while (
                index < tokens.size &&
                isEnvironmentAssignment(
                    tokens[index]
                )
            ) {

                index++
            }

            if (
                index >= tokens.size
            ) {

                return null
            }

            token =
                normalizeCommandToken(
                    tokens[index]
                )
        }

        if (
            token.equals(
                "env",
                ignoreCase = true
            )
        ) {

            index++

            while (
                index < tokens.size &&
                (
                        tokens[index].startsWith("-") ||
                                isEnvironmentAssignment(
                                    tokens[index]
                                )
                        )
            ) {

                index++
            }

            if (
                index >= tokens.size
            ) {

                return null
            }

            token =
                normalizeCommandToken(
                    tokens[index]
                )
        }

        val executable =
            token
                .substringAfterLast("/")
                .lowercase()

        if (
            executable !in packageExecutables
        ) {

            return null
        }

        return PackageInvocation(
            executable =
                executable,

            arguments =
                tokens.drop(
                    index + 1
                )
        )
    }

    /*
     * Split only on shell control operators that occur
     * outside quotes. This avoids classifying text such as
     *
     * echo 'apt install python3'
     *
     * as a real package command.
     */
    private fun splitShellSegments(
        command: String
    ): List<String> {

        val segments =
            mutableListOf<String>()

        val current =
            StringBuilder()

        var inSingleQuote =
            false

        var inDoubleQuote =
            false

        var escaped =
            false

        var index =
            0

        fun flushSegment() {

            val segment =
                current
                    .toString()
                    .trim()

            if (
                segment.isNotBlank()
            ) {

                segments.add(
                    segment
                )
            }

            current.clear()
        }

        while (
            index < command.length
        ) {

            val character =
                command[index]

            if (
                escaped
            ) {

                current.append(
                    character
                )

                escaped =
                    false

                index++

                continue
            }

            if (
                character == '\\' &&
                !inSingleQuote
            ) {

                current.append(
                    character
                )

                escaped =
                    true

                index++

                continue
            }

            if (
                character == '\'' &&
                !inDoubleQuote
            ) {

                inSingleQuote =
                    !inSingleQuote

                current.append(
                    character
                )

                index++

                continue
            }

            if (
                character == '"' &&
                !inSingleQuote
            ) {

                inDoubleQuote =
                    !inDoubleQuote

                current.append(
                    character
                )

                index++

                continue
            }

            if (
                !inSingleQuote &&
                !inDoubleQuote
            ) {

                when (
                    character
                ) {

                    ';' -> {

                        flushSegment()
                        index++
                        continue
                    }

                    '&' -> {

                        if (
                            index + 1 < command.length &&
                            command[index + 1] == '&'
                        ) {

                            flushSegment()
                            index += 2
                            continue
                        }
                    }

                    '|' -> {

                        flushSegment()

                        index +=
                            if (
                                index + 1 < command.length &&
                                command[index + 1] == '|'
                            ) {

                                2

                            } else {

                                1
                            }

                        continue
                    }
                }
            }

            current.append(
                character
            )

            index++
        }

        flushSegment()

        return segments
    }

    private fun tokenizeShellSegment(
        segment: String
    ): List<String> {

        val tokens =
            mutableListOf<String>()

        val current =
            StringBuilder()

        var inSingleQuote =
            false

        var inDoubleQuote =
            false

        var escaped =
            false

        fun flushToken() {

            if (
                current.isNotEmpty()
            ) {

                tokens.add(
                    current.toString()
                )

                current.clear()
            }
        }

        segment.forEach { character ->

            if (
                escaped
            ) {

                current.append(
                    character
                )

                escaped =
                    false

                return@forEach
            }

            if (
                character == '\\' &&
                !inSingleQuote
            ) {

                escaped =
                    true

                return@forEach
            }

            if (
                character == '\'' &&
                !inDoubleQuote
            ) {

                inSingleQuote =
                    !inSingleQuote

                return@forEach
            }

            if (
                character == '"' &&
                !inSingleQuote
            ) {

                inDoubleQuote =
                    !inDoubleQuote

                return@forEach
            }

            if (
                character.isWhitespace() &&
                !inSingleQuote &&
                !inDoubleQuote
            ) {

                flushToken()

            } else {

                current.append(
                    character
                )
            }
        }

        if (
            escaped
        ) {

            current.append(
                '\\'
            )
        }

        flushToken()

        return tokens
    }

    private fun normalizeCommandToken(
        token: String
    ): String {

        return token
            .trim()
            .trimStart(
                '(',
                '{'
            )
            .trimEnd(
                ')',
                '}'
            )
    }

    private fun isEnvironmentAssignment(
        token: String
    ): Boolean {

        val equalsIndex =
            token.indexOf(
                '='
            )

        if (
            equalsIndex <= 0
        ) {

            return false
        }

        val name =
            token.substring(
                0,
                equalsIndex
            )

        return name.matches(
            Regex(
                "[A-Za-z_][A-Za-z0-9_]*"
            )
        )
    }

    private data class PackageInvocation(
        val executable: String,
        val arguments: List<String>
    ) {

        fun mutationAction():
                String? {

            if (
                executable != "apt" &&
                executable != "apt-get"
            ) {

                return null
            }

            return arguments
                .asSequence()
                .map { argument ->

                    argument
                        .trim()
                        .lowercase()
                }
                .firstOrNull { argument ->

                    argument in
                            aptMutationActions
                }
        }

        fun requiresAssumeYes():
                Boolean {

            return mutationAction() !=
                    null
        }

        fun hasAssumeYes():
                Boolean {

            return arguments
                .any { argument ->

                    argument == "-y" ||
                            argument == "--assume-yes" ||
                            argument.startsWith(
                                "--assume-yes="
                            )
                }
        }
    }
}