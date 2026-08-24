package com.noahrose.pocketlab.feature.terminal

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.noahrose.pocketlab.feature.filesystem.VirtualFileSystem
import com.noahrose.pocketlab.feature.linux.runtime.command.LinuxShellMode
import com.noahrose.pocketlab.feature.terminal.completion.CommandCompletion
import com.noahrose.pocketlab.feature.terminal.startup.AtlasRcManager

class TerminalViewModel : ViewModel() {

    var uiState by mutableStateOf(
        TerminalUiState()
    )
        private set

    init {
        loadStartupConfiguration()
    }

    /*
     * ------------------------------------------------
     * CURRENT TERMINAL MODE
     * ------------------------------------------------
     *
     * LinuxShellMode remains the single source
     * of truth for whether the terminal is
     * currently connected to the Ubuntu shell.
     */
    val linuxShellActive:
            Boolean
        get() =
            LinuxShellMode
                .isActive()

    /*
     * ------------------------------------------------
     * LIVE PROMPT
     * ------------------------------------------------
     *
     * Atlas:
     *
     * atlas@cyberdeck:~$
     *
     * Ubuntu:
     *
     * root@atlas:~#
     *
     * Ubuntu working-directory changes are
     * reflected automatically by LinuxShellMode.
     */
    val prompt:
            String
        get() {

            return if (
                LinuxShellMode
                    .isActive()
            ) {

                LinuxShellMode
                    .getPrompt()

            } else {

                val currentPath =
                    VirtualFileSystem
                        .currentPath
                        .value

                "atlas@cyberdeck:$currentPath$"
            }
        }

    private fun loadStartupConfiguration() {

        val output =
            uiState
                .output
                .toMutableList()

        val executed =
            AtlasRcManager
                .execute(
                    output =
                        output
                )

        if (executed) {

            uiState =
                uiState.copy(
                    output =
                        output
                )
        }
    }

    fun updateCommand(
        command: String
    ) {

        uiState =
            uiState.copy(
                currentCommand =
                    command
            )
    }

    fun completeCommand() {

        /*
         * Atlas completion understands Atlas
         * commands and the Atlas virtual
         * filesystem.
         *
         * Do not apply Atlas completion rules
         * while commands are being sent to the
         * real Ubuntu guest.
         */
        if (
            LinuxShellMode
                .isActive()
        ) {

            return
        }

        val completion =
            CommandCompletion
                .complete(
                    uiState.currentCommand
                )

        if (
            completion != null
        ) {

            uiState =
                uiState.copy(
                    currentCommand =
                        completion
                )
        }
    }

    fun executeCommand() {

        val command =
            uiState
                .currentCommand
                .trim()

        if (
            command.isEmpty()
        ) {

            return
        }

        val output =
            uiState
                .output
                .toMutableList()

        /*
         * TerminalCommandProcessor determines
         * whether the command belongs to the
         * Atlas shell or Ubuntu shell.
         */
        TerminalCommandProcessor
            .process(
                command =
                    command,

                output =
                    output
            )

        /*
         * Updating Compose state forces the
         * terminal to redraw immediately after:
         *
         * linux shell
         * cd
         * exit
         *
         * The live prompt and terminal colors
         * therefore follow the current shell.
         */
        uiState =
            uiState.copy(
                output =
                    output,

                currentCommand =
                    ""
            )
    }
}