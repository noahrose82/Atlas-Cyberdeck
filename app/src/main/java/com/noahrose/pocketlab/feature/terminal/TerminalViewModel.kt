package com.noahrose.pocketlab.feature.terminal

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.noahrose.pocketlab.feature.filesystem.VirtualFileSystem
import com.noahrose.pocketlab.feature.linux.runtime.command.LinuxInteractiveCommandGuard
import com.noahrose.pocketlab.feature.linux.runtime.command.LinuxShellMode
import com.noahrose.pocketlab.feature.linux.runtime.process.LinuxInteractiveSessionStartResult
import com.noahrose.pocketlab.feature.terminal.completion.CommandCompletion
import com.noahrose.pocketlab.feature.terminal.history.CommandHistory
import com.noahrose.pocketlab.feature.terminal.interactive.LinuxInteractiveTerminalBridge
import com.noahrose.pocketlab.feature.terminal.startup.AtlasRcManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.connectbot.terminal.TerminalEmulator

class TerminalViewModel : ViewModel() {

    var uiState by mutableStateOf(
        TerminalUiState()
    )
        private set

    var commandRunning by mutableStateOf(
        false
    )
        private set

    /*
     * ------------------------------------------------
     * INTERACTIVE TERMINAL STATE
     * ------------------------------------------------
     */
    var interactiveSessionActive by mutableStateOf(
        false
    )
        private set

    var interactiveControlArmed by mutableStateOf(
        false
    )
        private set

    /*
     * The bridge owns the PTY transport and terminal
     * emulator.
     *
     * Ctrl state changes may originate from keyboard
     * input processing, so route the Compose state update
     * through viewModelScope.
     */
    private val interactiveBridge =
        LinuxInteractiveTerminalBridge(
            onControlArmedChanged = { armed ->

                viewModelScope
                    .launch {

                        interactiveControlArmed =
                            armed
                    }
            }
        )

    val interactiveTerminalEmulator:
            TerminalEmulator
        get() =
            interactiveBridge
                .terminalEmulator

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

        if (
            interactiveSessionActive
        ) {

            return
        }

        uiState =
            uiState.copy(
                currentCommand =
                    command
            )
    }

    fun completeCommand() {

        if (
            LinuxShellMode
                .isActive()
        ) {

            return
        }

        if (
            interactiveSessionActive
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
     */
    fun executeCommand() {

        if (
            commandRunning ||
            interactiveSessionActive
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

        /*
         * PTY applications are intercepted only from the
         * real Ubuntu shell UI path.
         */
        if (
            LinuxShellMode
                .isActive() &&
            LinuxInteractiveCommandGuard
                .requiresPty(
                    command
                )
        ) {

            executeInteractiveCommand(
                command
            )

            return
        }

        executeNormalCommand(
            command
        )
    }

    /*
     * ------------------------------------------------
     * NORMAL COMMAND PATH
     * ------------------------------------------------
     */
    private fun executeNormalCommand(
        command: String
    ) {

        val startingOutput =
            uiState
                .output
                .toList()

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

    /*
     * ------------------------------------------------
     * INTERACTIVE COMMAND PATH
     * ------------------------------------------------
     */
    private fun executeInteractiveCommand(
        command: String
    ) {

        CommandHistory
            .add(
                command
            )

        val commandLine =
            "${LinuxShellMode.getPrompt()} $command"

        uiState =
            uiState.copy(
                currentCommand =
                    "",

                output =
                    uiState.output +
                            commandLine
            )

        interactiveControlArmed =
            false

        commandRunning =
            true

        viewModelScope
            .launch {

                val startResult =
                    withContext(
                        Dispatchers.IO
                    ) {

                        interactiveBridge
                            .start(
                                command
                            )
                    }

                when (
                    startResult
                ) {

                    LinuxInteractiveSessionStartResult.Started -> {

                        interactiveSessionActive =
                            true

                        commandRunning =
                            false

                        try {

                            interactiveBridge
                                .pumpOutput()

                        } finally {

                            interactiveSessionActive =
                                false

                            interactiveControlArmed =
                                false

                            commandRunning =
                                false
                        }
                    }

                    is LinuxInteractiveSessionStartResult.Failure -> {

                        interactiveSessionActive =
                            false

                        interactiveControlArmed =
                            false

                        commandRunning =
                            false

                        uiState =
                            uiState.copy(
                                output =
                                    uiState.output +
                                            listOf(
                                                "Interactive Ubuntu session could not start.",
                                                "Why: ${startResult.message}",
                                                "Atlas did not modify your Ubuntu files.",
                                                "Return to the Atlas shell and check 'safety status' and 'linux status'.",
                                                "Error code: ATLAS-LINUX-PTY-START"
                                            )
                            )
                    }
                }
            }
    }

    /*
     * ------------------------------------------------
     * ATLAS TERMINAL KEYBOARD
     * ------------------------------------------------
     *
     * These methods are the only interface the Compose
     * terminal control bar needs.
     *
     * The UI never writes directly to the Linux process.
     */

    fun toggleInteractiveControl() {

        if (
            !interactiveSessionActive
        ) {

            return
        }

        interactiveControlArmed =
            interactiveBridge
                .toggleControlArmed()
    }

    fun sendInteractiveEscape() {

        if (
            !interactiveSessionActive
        ) {

            return
        }

        interactiveBridge
            .sendEscape()
    }

    fun sendInteractiveTab() {

        if (
            !interactiveSessionActive
        ) {

            return
        }

        interactiveBridge
            .sendTab()
    }

    fun sendInteractiveEnter() {

        if (
            !interactiveSessionActive
        ) {

            return
        }

        interactiveBridge
            .sendEnter()
    }

    fun sendInteractiveBackspace() {

        if (
            !interactiveSessionActive
        ) {

            return
        }

        interactiveBridge
            .sendBackspace()
    }

    fun sendInteractiveArrowLeft() {

        if (
            !interactiveSessionActive
        ) {

            return
        }

        interactiveBridge
            .sendArrowLeft()
    }

    fun sendInteractiveArrowUp() {

        if (
            !interactiveSessionActive
        ) {

            return
        }

        interactiveBridge
            .sendArrowUp()
    }

    fun sendInteractiveArrowDown() {

        if (
            !interactiveSessionActive
        ) {

            return
        }

        interactiveBridge
            .sendArrowDown()
    }

    fun sendInteractiveArrowRight() {

        if (
            !interactiveSessionActive
        ) {

            return
        }

        interactiveBridge
            .sendArrowRight()
    }

    /*
     * ------------------------------------------------
     * DIRECT CTRL SHORTCUT
     * ------------------------------------------------
     *
     * Useful later for dedicated buttons such as:
     *
     *     ^C
     *     ^X
     *     ^O
     *
     * without requiring the one-shot modifier.
     */
    fun sendInteractiveControl(
        character: Char
    ) {

        if (
            !interactiveSessionActive
        ) {

            return
        }

        interactiveBridge
            .sendControl(
                character
            )
    }

    /*
     * ------------------------------------------------
     * MANUAL INTERACTIVE STOP
     * ------------------------------------------------
     */
    fun stopInteractiveSession() {

        if (
            !interactiveSessionActive
        ) {

            return
        }

        viewModelScope
            .launch {

                withContext(
                    Dispatchers.IO
                ) {

                    interactiveBridge
                        .stop()
                }

                interactiveSessionActive =
                    false

                interactiveControlArmed =
                    false

                commandRunning =
                    false
            }
    }

    override fun onCleared() {

        interactiveBridge
            .stop()

        super.onCleared()
    }
}