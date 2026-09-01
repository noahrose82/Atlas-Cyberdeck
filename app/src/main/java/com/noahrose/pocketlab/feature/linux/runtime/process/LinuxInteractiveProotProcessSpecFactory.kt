package com.noahrose.pocketlab.feature.linux.runtime.process

object LinuxInteractiveProotProcessSpecFactory {

    private const val DEFAULT_COLUMNS =
        80

    private const val DEFAULT_ROWS =
        24

    /*
     * ------------------------------------------------
     * INTERACTIVE PTY CONTROL FILE
     * ------------------------------------------------
     *
     * `script` allocates the real PTY inside Ubuntu.
     *
     * Android does not own that PTY descriptor directly,
     * so the interactive child publishes its slave TTY
     * path here before exec'ing nano or vim.
     *
     * Example:
     *
     *     /dev/pts/3
     *
     * Only one Atlas interactive session may exist at a
     * time, so one transient control file is sufficient.
     *
     * This is runtime metadata only.
     * It contains no user document data.
     */
    const val PTY_CONTROL_FILE =
        "/tmp/.atlas-interactive-pty"

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
         * Start from the exact validated PRoot
         * configuration used by the persistent Ubuntu
         * runtime.
         *
         * This preserves:
         *
         *     rootfs
         *     link2symlink
         *     .l2s
         *     /dev
         *     /proc
         *     /sys
         *     environment
         *     loader
         *     Android runtime bindings
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
         * Fail closed if that contract changes.
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
         * ------------------------------------------------
         * INTERACTIVE CHILD WRAPPER
         * ------------------------------------------------
         *
         * This command executes INSIDE the PTY created by
         * `script`.
         *
         * Sequence:
         *
         * 1. Remove stale PTY metadata.
         *
         * 2. Ask Ubuntu which PTY device belongs to this
         *    interactive shell.
         *
         * 3. Validate that the returned path is a numeric
         *    entry under /dev/pts.
         *
         * 4. Store the path in Atlas' transient control
         *    file.
         *
         * 5. Establish the initial rows and columns.
         *
         * 6. Replace the wrapper shell with nano, vim,
         *    or another interactive application.
         *
         * The same PTY survives the final exec.
         */
        val interactiveCommand =
            buildString {

                /*
                 * Never allow stale PTY identity from an
                 * earlier interrupted session to become
                 * authoritative.
                 */
                append(
                    "rm -f "
                )

                append(
                    PTY_CONTROL_FILE
                )

                append(
                    "; "
                )

                /*
                 * Capture the PTY allocated by `script`.
                 */
                append(
                    "ATLAS_PTY=\$(/usr/bin/tty 2>/dev/null) || exit 70; "
                )

                /*
                 * Require a device beneath /dev/pts.
                 */
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

                /*
                 * Require the PTY identifier itself to be
                 * numeric.
                 */
                append(
                    "case \"\$ATLAS_PTY_NUMBER\" in "
                )

                append(
                    "''|*[!0-9]*) exit 71 ;; "
                )

                append(
                    "esac; "
                )

                /*
                 * Restrict metadata permissions.
                 */
                append(
                    "umask 077; "
                )

                /*
                 * Publish only the validated PTY path.
                 */
                append(
                    "printf '%s\\n' \"\$ATLAS_PTY\" > "
                )

                append(
                    PTY_CONTROL_FILE
                )

                append(
                    " || exit 72; "
                )

                /*
                 * Establish initial terminal geometry.
                 */
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

                /*
                 * Replace this temporary shell while
                 * retaining the same PTY.
                 */
                append(
                    "exec "
                )

                append(
                    cleanCommand
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

        /*
         * ProcessBuilder passes this complete command as
         * one argv value to `script`.
         */
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