package com.noahrose.pocketlab.feature.linux.runtime.process

object LinuxInteractiveProotProcessSpecFactory {

    private const val DEFAULT_COLUMNS =
        80

    private const val DEFAULT_ROWS =
        24

    /*
     * Build a dedicated PRoot process specification for
     * an interactive Ubuntu application.
     *
     * This intentionally does NOT reuse Atlas' persistent
     * guest shell process.
     *
     * The normal runtime continues to own:
     *
     *     /bin/sh
     *
     * while interactive applications run inside a second,
     * temporary PRoot process.
     *
     * Inside that process, util-linux `script` allocates
     * the real PTY required by applications such as:
     *
     *     nano
     *     vim
     *     vi
     *     top
     *     less
     */
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

        /*
         * Start from the exact same validated PRoot
         * configuration used by the persistent Ubuntu
         * runtime.
         *
         * This preserves:
         *
         *     rootfs
         *     --link2symlink
         *     .l2s
         *     /dev
         *     /proc
         *     /sys
         *     environment
         *     loader
         *     Android runtime bindings
         *
         * We only replace the final guest command.
         */
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

        /*
         * LinuxProotProcessSpecFactory currently ends the
         * PRoot argument list with:
         *
         *     /bin/sh
         *
         * That shell is appropriate for Atlas' persistent
         * command bridge but not for a dedicated interactive
         * application.
         *
         * Fail closed if that contract ever changes instead
         * of silently constructing a malformed PRoot command.
         */
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

        /*
         * `script` creates a real pseudo-terminal under
         * /dev/pts.
         *
         * The command supplied through -c executes inside
         * that PTY.
         *
         * `-q`
         *     quiet mode
         *
         * `-e`
         *     return the child application's exit status
         *
         * `-f`
         *     flush output immediately
         *
         * /dev/null
         *     prevents creation of a typescript file
         *
         * stty establishes a predictable initial terminal
         * size before the application starts.
         */
        val interactiveCommand =
            buildString {

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
                    " >/dev/null 2>&1; "
                )

                append(
                    "exec "
                )

                append(
                    cleanCommand
                )
            }

        interactiveArguments.add(
            "/usr/bin/script"
        )

        interactiveArguments.add(
            "-qefc"
        )

        /*
         * ProcessBuilder passes this as one argv value.
         *
         * No extra Android-side shell quoting is required.
         * `script` itself passes the command to the Ubuntu
         * shell attached to the PTY.
         */
        interactiveArguments.add(
            interactiveCommand
        )

        interactiveArguments.add(
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