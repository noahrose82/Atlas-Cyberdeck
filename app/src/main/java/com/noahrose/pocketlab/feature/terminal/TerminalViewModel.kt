package com.noahrose.pocketlab.feature.terminal

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.noahrose.pocketlab.feature.filesystem.VirtualFileSystem
import com.noahrose.pocketlab.feature.linux.runtime.command.LinuxShellMode
import com.noahrose.pocketlab.feature.terminal.completion.CommandCompletion
import com.noahrose.pocketlab.feature.terminal.startup.AtlasRcManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TerminalViewModel : ViewModel() {

    var uiState by mutableStateOf(
        TerminalUiState()
    )
        private set

    var commandRunning by mutableStateOf(
        false
    )
        private set

    init {
        loadStartupConfiguration()
    }

    val linuxShellActive:
            Boolean
        get() =
            LinuxShellMode
                .isActive()

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

        if (
            executed
        ) {

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
         * Atlas completion must not rewrite commands
         * while the real Ubuntu shell owns the input.
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

    /*
     * ------------------------------------------------
     * COMMAND EXECUTION
     * ------------------------------------------------
     *
     * TerminalCommandProcessor runs on Dispatchers.IO.
     *
     * A per-command Channel transports live guest
     * output back to the main thread while apt/dpkg
     * is still running.
     */
    fun executeCommand() {

        if (
            commandRunning
        ) {

            return
        }

        val command =
            uiState
                .currentCommand
                .trim()

        if (
            command.isEmpty()
        ) {

            return
        }

        val startingOutput =
            uiState
                .output
                .toList()

        /*
         * Clear the input immediately so the terminal
         * acknowledges Enter without waiting for the
         * guest command to finish.
         */
        uiState =
            uiState.copy(
                currentCommand =
                    ""
            )

        commandRunning =
            true

        viewModelScope
            .launch {

                val liveOutput =
                    Channel<String>(
                        capacity =
                            Channel.UNLIMITED
                    )

                /*
                 * Runs on the main thread and updates
                 * Compose state one line at a time.
                 */
                val liveOutputJob =
                    launch {

                        for (
                        line in
                        liveOutput
                        ) {

                            uiState =
                                uiState.copy(
                                    output =
                                        uiState.output +
                                                line
                                )
                        }
                    }

                val processedOutput =
                    withContext(
                        Dispatchers.IO
                    ) {

                        val output =
                            startingOutput
                                .toMutableList()

                        TerminalCommandProcessor
                            .process(
                                command =
                                    command,

                                output =
                                    output,

                                onLiveOutput = { line ->

                                    liveOutput
                                        .trySend(
                                            line
                                        )
                                }
                            )

                        output
                    }

                /*
                 * Drain every queued line before the
                 * final canonical output list replaces
                 * the temporary streamed UI state.
                 */
                liveOutput
                    .close()

                liveOutputJob
                    .join()

                uiState =
                    uiState.copy(
                        output =
                            processedOutput
                    )

                commandRunning =
                    false
            }
    }
}