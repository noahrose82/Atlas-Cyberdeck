package com.noahrose.pocketlab.feature.linux.runtime.process

object LinuxInteractiveProotProcessSpecFactory {

    private const val DEFAULT_COLUMNS =
        80

    private const val DEFAULT_ROWS =
        24

    const val PTY_CONTROL_FILE =
        "/tmp/.atlas-interactive-pty"

    fun create(
        command: String,
        columns: Int = DEFAULT_COLUMNS,
        rows: Int = DEFAULT_ROWS
    ): LinuxProotProcessSpecResult {

        val cleanCommand =
            command.trim()

        if (
            cleanCommand.isBlank()
        ) {

            return LinuxProotProcessSpecResult
                .Failure(
                    message =
                        "Interactive Linux command cannot be empty."
                )
        }

        if (
            columns <= 0 ||
            rows <= 0
        ) {

            return LinuxProotProcessSpecResult
                .Failure(
                    message =
                        "Interactive terminal dimensions must be greater than zero."
                )
        }

        val baseResult =
            LinuxProotProcessSpecFactory
                .create()

        val baseSpec =
            when (
                baseResult
            ) {

                is LinuxProotProcessSpecResult.Ready -> {

                    baseResult.spec
                }

                is LinuxProotProcessSpecResult.Failure -> {

                    return baseResult
                }
            }

        if (
            baseSpec.arguments.isEmpty() ||
            baseSpec.arguments.last() !=
            "/bin/sh"
        ) {

            return LinuxProotProcessSpecResult
                .Failure(
                    message =
                        "Atlas interactive terminal could not derive " +
                                "a guest process specification from the " +
                                "current PRoot runtime configuration."
                )
        }

        val interactiveArguments =
            baseSpec.arguments
                .dropLast(
                    1
                )
                .toMutableList()

        val interactiveCommand =
            buildString {

                append(
                    "rm -f "
                )

                append(
                    PTY_CONTROL_FILE
                )

                append(
                    "; "
                )

                append(
                    "ATLAS_PTY=\$(/usr/bin/tty 2>/dev/null) || exit 70; "
                )

                append(
                    "case \"\$ATLAS_PTY\" in "
                )

                append(
                    "/dev/pts/*) "
                )

                append(
                    "ATLAS_PTY_NUMBER=\"\${ATLAS_PTY#/dev/pts/}\" "
                )

                append(
                    ";; "
                )

                append(
                    "*) exit 71 ;; "
                )

                append(
                    "esac; "
                )

                append(
                    "case \"\$ATLAS_PTY_NUMBER\" in "
                )

                append(
                    "''|*[!0-9]*) exit 71 ;; "
                )

                append(
                    "esac; "
                )

                append(
                    "umask 077; "
                )

                append(
                    "printf '%s\\n' \"\$ATLAS_PTY\" > "
                )

                append(
                    PTY_CONTROL_FILE
                )

                append(
                    " || exit 72; "
                )

                append(
                    "stty rows "
                )

                append(
                    rows
                )

                append(
                    " cols "
                )

                append(
                    columns
                )

                append(
                    " >/dev/null 2>&1 || exit 73; "
                )

                append(
                    "atlas_finish() { "
                )

                append(
                    "ATLAS_STATUS=\$?; "
                )

                append(
                    "trap - EXIT HUP INT TERM; "
                )

                append(
                    "rm -f "
                )

                append(
                    PTY_CONTROL_FILE
                )

                append(
                    "; "
                )

                append(
                    "kill -TERM \"\$PPID\" >/dev/null 2>&1 || true; "
                )

                append(
                    "exit \"\$ATLAS_STATUS\"; "
                )

                append(
                    "}; "
                )

                append(
                    "trap atlas_finish EXIT HUP INT TERM; "
                )

                append(
                    cleanCommand
                )

                append(
                    "; "
                )

                append(
                    "ATLAS_COMMAND_STATUS=\$?; "
                )

                append(
                    "exit \"\$ATLAS_COMMAND_STATUS\""
                )
            }

        interactiveArguments
            .add(
                "/usr/bin/script"
            )

        interactiveArguments
            .add(
                "-qefc"
            )

        interactiveArguments
            .add(
                interactiveCommand
            )

        interactiveArguments
            .add(
                "/dev/null"
            )

        return LinuxProotProcessSpecResult
            .Ready(
                spec =
                    LinuxProcessSpec(
                        executable =
                            baseSpec.executable,

                        arguments =
                            interactiveArguments,

                        workingDirectory =
                            baseSpec.workingDirectory,

                        environment =
                            baseSpec.environment
                    )
            )
    }
}
