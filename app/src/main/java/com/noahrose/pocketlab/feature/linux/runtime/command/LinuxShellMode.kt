package com.noahrose.pocketlab.feature.linux.runtime.command

import com.noahrose.pocketlab.feature.linux.runtime.ProotLinuxRuntimeBackend

object LinuxShellMode {

    @Volatile
    private var active:
            Boolean =
        false

    @Volatile
    private var currentDirectory:
            String =
        "/root"

    /*
     * ------------------------------------------------
     * SHELL STATE
     * ------------------------------------------------
     */
    fun isActive():
            Boolean {

        if (
            !active
        ) {

            return false
        }

        val process =
            ProotLinuxRuntimeBackend
                .getProcess()

        if (
            process == null ||
            !process.isAlive
        ) {

            reset()

            return false
        }

        return true
    }

    fun enter():
            Boolean {

        val process =
            ProotLinuxRuntimeBackend
                .getProcess()
                ?: return false

        if (
            !process.isAlive
        ) {

            return false
        }

        active =
            true

        currentDirectory =
            "/root"

        refreshWorkingDirectory()

        return true
    }

    fun exit() {

        /*
         * Leaving shell mode does not terminate the
         * Ubuntu runtime. The persistent guest remains
         * available for linux exec / linux shell.
         */
        reset()
    }

    /*
     * ------------------------------------------------
     * PROMPT
     * ------------------------------------------------
     */
    fun getPrompt():
            String {

        val displayDirectory =
            formatDirectoryForPrompt(
                currentDirectory
            )

        return "root@atlas:$displayDirectory#"
    }

    fun getCurrentDirectory():
            String {

        return currentDirectory
    }

    /*
     * ------------------------------------------------
     * COMMAND EXECUTION
     * ------------------------------------------------
     *
     * Output callbacks fire while the guest command
     * is still running. This is what allows the
     * Compose terminal to display apt/dpkg output in
     * real time.
     */
    fun execute(
        command: String,
        onOutputLine: ((String) -> Unit)? = null,
        onErrorLine: ((String) -> Unit)? = null
    ): LinuxGuestCommandResult {

        if (
            !isActive()
        ) {

            return LinuxGuestCommandResult
                .Failure(
                    message =
                        "Ubuntu shell mode is not active."
                )
        }

        val result =
            LinuxGuestCommandExecutor
                .execute(
                    command =
                        command,

                    onOutputLine =
                        onOutputLine,

                    onErrorLine =
                        onErrorLine
                )

        when (
            result
        ) {

            is LinuxGuestCommandResult.Success -> {

                /*
                 * Stateful guest commands can alter the
                 * persistent working directory.
                 */
                refreshWorkingDirectory()
            }

            is LinuxGuestCommandResult.Failure -> {

                if (
                    ProotLinuxRuntimeBackend
                        .getProcess() == null
                ) {

                    reset()
                }
            }
        }

        return result
    }

    /*
     * ------------------------------------------------
     * WORKING DIRECTORY
     * ------------------------------------------------
     */
    private fun refreshWorkingDirectory() {

        if (
            !active
        ) {

            return
        }

        when (
            val result =
                LinuxGuestCommandExecutor
                    .execute(
                        "pwd"
                    )
        ) {

            is LinuxGuestCommandResult.Success -> {

                val directory =
                    result.output
                        .lineSequence()
                        .map { line ->

                            line.trim()
                        }
                        .firstOrNull { line ->

                            line.startsWith(
                                "/"
                            )
                        }

                if (
                    !directory
                        .isNullOrBlank()
                ) {

                    currentDirectory =
                        normalizeDirectory(
                            directory
                        )
                }
            }

            is LinuxGuestCommandResult.Failure -> {

                if (
                    ProotLinuxRuntimeBackend
                        .getProcess() == null
                ) {

                    reset()
                }
            }
        }
    }

    /*
     * ------------------------------------------------
     * PROMPT DIRECTORY FORMATTING
     * ------------------------------------------------
     */
    private fun formatDirectoryForPrompt(
        directory: String
    ): String {

        val normalized =
            normalizeDirectory(
                directory
            )

        return when {

            normalized ==
                    "/root" -> {

                "~"
            }

            normalized.startsWith(
                "/root/"
            ) -> {

                "~" +
                        normalized.removePrefix(
                            "/root"
                        )
            }

            else -> {

                normalized
            }
        }
    }

    private fun normalizeDirectory(
        directory: String
    ): String {

        val trimmed =
            directory.trim()

        if (
            trimmed.isBlank()
        ) {

            return "/root"
        }

        if (
            trimmed ==
            "/"
        ) {

            return "/"
        }

        return trimmed
            .removeSuffix(
                "/"
            )
    }

    private fun reset() {

        active =
            false

        currentDirectory =
            "/root"
    }
}