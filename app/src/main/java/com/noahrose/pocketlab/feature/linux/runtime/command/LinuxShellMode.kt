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

    fun isActive():
            Boolean {

        /*
         * Shell mode cannot remain active if
         * the native Ubuntu runtime has exited.
         */
        if (
            active &&
            ProotLinuxRuntimeBackend
                .getProcess() == null
        ) {

            active =
                false

            currentDirectory =
                "/root"
        }

        return active
    }

    fun enter():
            Boolean {

        val process =
            ProotLinuxRuntimeBackend
                .getProcess()
                ?: return false

        if (!process.isAlive) {
            return false
        }

        active =
            true

        refreshWorkingDirectory()

        return true
    }

    fun exit() {

        /*
         * Leaving Atlas Linux shell mode must
         * NOT terminate the underlying Ubuntu
         * runtime.
         *
         * The persistent /bin/sh process remains
         * available for linux exec and future
         * shell sessions.
         */
        active =
            false

        currentDirectory =
            "/root"
    }

    fun getPrompt():
            String {

        val displayDirectory =
            when (currentDirectory) {

                "/root" ->
                    "~"

                else ->
                    currentDirectory
            }

        return "root@atlas:$displayDirectory#"
    }

    fun execute(
        command: String
    ): LinuxGuestCommandResult {

        if (!isActive()) {

            return LinuxGuestCommandResult
                .Failure(
                    message =
                        "Ubuntu shell mode is not active."
                )
        }

        val result =
            LinuxGuestCommandExecutor
                .execute(
                    command
                )

        if (
            result is
                    LinuxGuestCommandResult.Success
        ) {

            /*
             * The guest shell is persistent.
             *
             * Stateful commands such as:
             *
             * cd /etc
             *
             * therefore change the working
             * directory for subsequent commands.
             */
            refreshWorkingDirectory()

        } else if (
            ProotLinuxRuntimeBackend
                .getProcess() == null
        ) {

            active =
                false

            currentDirectory =
                "/root"
        }

        return result
    }

    private fun refreshWorkingDirectory() {

        if (!active) {
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
                    result
                        .output
                        .lineSequence()
                        .map { line ->

                            line.trim()
                        }
                        .firstOrNull { line ->

                            line.startsWith("/")
                        }

                if (
                    !directory
                        .isNullOrBlank()
                ) {

                    currentDirectory =
                        directory
                }
            }

            is LinuxGuestCommandResult.Failure -> {

                /*
                 * Keep the previous prompt path.
                 *
                 * The main command execution path
                 * will report meaningful failures.
                 */
            }
        }
    }
}