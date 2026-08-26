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

    /*
     * dpkg operations that can change the package
     * database or package configuration state.
     *
     * Read-only operations such as --audit, -l,
     * --status, and --print-architecture do not need
     * a post-transaction package integrity audit.
     */
    private val dpkgMutationActions =
        setOf(
            "-i",
            "--install",
            "--unpack",
            "--configure",
            "-r",
            "--remove",
            "-p",
            "--purge",
            "--triggers-only",
            "--update-avail",
            "--merge-avail",
            "--clear-avail",
            "--forget-old-unavail"
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
        val mutationInvocations =
            invocations
                .filter { invocation ->

                    invocation
                        .mutatesPackageDatabase()
                }

        val auditAfterMutation =
            mutationInvocations
                .isNotEmpty()

        /*
         * Before a normal package mutation, verify that
         * dpkg is already healthy.
         *
         * Recovery operations such as:
         *
         * dpkg --configure -a
         * apt --fix-broken install -y
         *
         * must remain available even when the package
         * database is degraded, otherwise Atlas could
         * block the very commands needed to repair it.
         */
        val preflightBeforeMutation =
            mutationInvocations
                .any { invocation ->

                    !invocation
                        .isRecoveryOperation()
                }

        val hardenedCommand =
            buildHardenedCommand(
                command =
                    trimmedCommand,

                preflightBeforeMutation =
                    preflightBeforeMutation,

                auditAfterMutation =
                    auditAfterMutation
            )

        return LinuxPackageCommandPreparation
            .Ready(
                command =
                    hardenedCommand,

                hardened =
                    true
            )
    }

    /*
     * Build a command-scoped package environment.
     *
     * Normal mutation commands receive a pre-transaction
     * dpkg audit. If Ubuntu already has an incomplete
     * package transaction, Atlas blocks the new mutation
     * before it can make package state harder to recover.
     *
     * Recovery operations bypass the preflight gate but
     * still receive the post-transaction integrity audit.
     *
     * The original package command exit code is preserved
     * after the post-transaction audit.
     */
    private fun buildHardenedCommand(
        command: String,
        preflightBeforeMutation: Boolean,
        auditAfterMutation: Boolean
    ): String {

        return buildString {

            append("( ")
            append("export DEBIAN_FRONTEND=noninteractive; ")
            append("export DEBCONF_NONINTERACTIVE_SEEN=true; ")
            append("export APT_LISTCHANGES_FRONTEND=none; ")
            append("export TZ=Etc/UTC; ")

            if (preflightBeforeMutation) {

                append("__atlas_pkg_pre_audit=\"${'$'}(dpkg --audit 2>&1)\"; ")
                append("__atlas_pkg_pre_audit_exit=${'$'}?; ")
                append("if [ ${'$'}__atlas_pkg_pre_audit_exit -ne 0 ]; then ")
                append("printf 'Atlas package preflight: FAILED (dpkg --audit exit %s).\\n' \"${'$'}__atlas_pkg_pre_audit_exit\" >&2; ")
                append("printf 'Repair with: dpkg --configure -a\\n' >&2; ")
                append("exit 125; ")
                append("fi; ")
                append("if [ -n \"${'$'}__atlas_pkg_pre_audit\" ]; then ")
                append("printf 'Atlas package preflight: BLOCKED\\n' >&2; ")
                append("printf 'Existing package state is incomplete:\\n%s\\n' \"${'$'}__atlas_pkg_pre_audit\" >&2; ")
                append("printf 'Repair with: dpkg --configure -a\\n' >&2; ")
                append("printf 'Then retry the package command.\\n' >&2; ")
                append("exit 126; ")
                append("fi; ")
                append("printf 'Atlas package preflight: CLEAN\\n' >&2; ")
            }

            append(command)

            if (auditAfterMutation) {

                append("; __atlas_pkg_exit=${'$'}?; ")
                append("__atlas_pkg_audit=\"${'$'}(dpkg --audit 2>&1)\"; ")
                append("__atlas_pkg_audit_exit=${'$'}?; ")
                append("if [ ${'$'}__atlas_pkg_audit_exit -ne 0 ]; then ")
                append("printf '\\nAtlas package health check failed (dpkg --audit exit %s).\\n' \"${'$'}__atlas_pkg_audit_exit\" >&2; ")
                append("elif [ -n \"${'$'}__atlas_pkg_audit\" ]; then ")
                append("printf '\\nAtlas package health warning:\\n%s\\n' \"${'$'}__atlas_pkg_audit\" >&2; ")
                append("printf 'Run: DEBIAN_FRONTEND=noninteractive dpkg --configure -a\\n' >&2; ")
                append("else ")
                append("printf '\\nAtlas package health: CLEAN\\n' >&2; ")
                append("fi; ")
                append("exit ${'$'}__atlas_pkg_exit")
            }

            append(" )")
        }
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

        fun mutatesPackageDatabase():
                Boolean {

            if (
                executable == "apt" ||
                executable == "apt-get"
            ) {

                return mutationAction() !=
                        null
            }

            if (
                executable != "dpkg"
            ) {

                return false
            }

            return arguments
                .any { argument ->

                    val normalized =
                        argument
                            .trim()
                            .lowercase()

                    normalized in
                            dpkgMutationActions ||
                            normalized.startsWith(
                                "--install="
                            ) ||
                            normalized.startsWith(
                                "--unpack="
                            ) ||
                            normalized.startsWith(
                                "--configure="
                            ) ||
                            normalized.startsWith(
                                "--remove="
                            ) ||
                            normalized.startsWith(
                                "--purge="
                            )
                }
        }

        /*
         * Package repair operations are deliberately
         * exempt from the pre-transaction health gate.
         * They still run in Atlas' noninteractive package
         * environment and still receive a post-transaction
         * dpkg audit.
         */
        fun isRecoveryOperation():
                Boolean {

            if (
                executable == "dpkg"
            ) {

                return arguments
                    .any { argument ->

                        val normalized =
                            argument
                                .trim()
                                .lowercase()

                        normalized ==
                                "--configure" ||
                                normalized.startsWith(
                                    "--configure="
                                ) ||
                                normalized ==
                                "--triggers-only"
                    }
            }

            if (
                executable != "apt" &&
                executable != "apt-get"
            ) {

                return false
            }

            if (
                mutationAction() !=
                "install"
            ) {

                return false
            }

            return arguments
                .any { argument ->

                    val normalized =
                        argument
                            .trim()
                            .lowercase()

                    normalized ==
                            "-f" ||
                            normalized ==
                            "--fix-broken" ||
                            normalized.startsWith(
                                "--fix-broken="
                            )
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