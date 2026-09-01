package com.noahrose.pocketlab.feature.terminal

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.noahrose.pocketlab.feature.filesystem.VirtualFileSystem
import com.noahrose.pocketlab.feature.linux.runtime.command.LinuxInteractiveCommandGuard
import com.noahrose.pocketlab.feature.linux.runtime.command.LinuxShellMode
import com.noahrose.pocketlab.feature.linux.runtime.process.LinuxInteractiveResizeResult
import com.noahrose.pocketlab.feature.linux.runtime.process.LinuxInteractiveSessionStartResult
import com.noahrose.pocketlab.feature.terminal.completion.CommandCompletion
import com.noahrose.pocketlab.feature.terminal.history.CommandHistory
import com.noahrose.pocketlab.feature.terminal.interactive.LinuxInteractiveTerminalBridge
import com.noahrose.pocketlab.feature.terminal.interactive.LinuxInteractiveTerminalSessionController
import com.noahrose.pocketlab.feature.terminal.startup.AtlasRcManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.connectbot.terminal.TerminalEmulator

class TerminalViewModel : ViewModel() {

    companion object {

        /*
         * Compose may report several viewport sizes while
         * Android animates the soft keyboard.
         *
         * Wait briefly for the viewport to settle before
         * asking Ubuntu to change its PTY geometry.
         */
        private const val INTERACTIVE_RESIZE_DEBOUNCE_MS =
            150L
    }

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
     * INTERACTIVE TERMINAL SESSION STATE
     * ------------------------------------------------
     */
    var interactiveSessionActive by mutableStateOf(
        LinuxInteractiveTerminalSessionController
            .isActive
    )
        private set

    var interactiveControlArmed by mutableStateOf(
        LinuxInteractiveTerminalSessionController
            .controlArmed
            .value
    )
        private set

    /*
     * ------------------------------------------------
     * INTERACTIVE TERMINAL GEOMETRY
     * ------------------------------------------------
     *
     * Before an interactive application starts, these
     * hold the dimensions prepared by TerminalScreen.
     *
     * During an active PTY session they mirror the
     * application-level controller.
     *
     * This keeps:
     *
     *     TerminalScreen
     *     termlib
     *     Linux PTY
     *     nano / vim
     *
     * on one shared rows/columns contract.
     */
    var interactiveTerminalRows by mutableStateOf(
        if (
            LinuxInteractiveTerminalSessionController
                .isActive
        ) {

            LinuxInteractiveTerminalSessionController
                .rows

        } else {

            LinuxInteractiveTerminalBridge
                .DEFAULT_ROWS
        }
    )
        private set

    var interactiveTerminalColumns by mutableStateOf(
        if (
            LinuxInteractiveTerminalSessionController
                .isActive
        ) {

            LinuxInteractiveTerminalSessionController
                .columns

        } else {

            LinuxInteractiveTerminalBridge
                .DEFAULT_COLUMNS
        }
    )
        private set

    /*
     * A pending viewport change is owned only by this
     * ViewModel.
     *
     * The actual interactive process remains owned by the
     * persistent application-level controller.
     */
    private var interactiveResizeJob:
            Job? =
        null

    val interactiveTerminalEmulator:
            TerminalEmulator
        get() =
            LinuxInteractiveTerminalSessionController
                .terminalEmulator

    init {

        loadStartupConfiguration()

        /*
         * Reattach to the application-level interactive
         * terminal session after UI/ViewModel recreation.
         */
        viewModelScope
            .launch {

                LinuxInteractiveTerminalSessionController
                    .sessionActive
                    .collect { active ->

                        interactiveSessionActive =
                            active

                        if (
                            active
                        ) {

                            /*
                             * A recreated ViewModel must use
                             * the geometry already owned by
                             * the surviving PTY session.
                             */
                            interactiveTerminalRows =
                                LinuxInteractiveTerminalSessionController
                                    .rows

                            interactiveTerminalColumns =
                                LinuxInteractiveTerminalSessionController
                                    .columns

                        } else {

                            /*
                             * A resize request is no longer
                             * meaningful once the interactive
                             * process has ended.
                             */
                            interactiveResizeJob
                                ?.cancel()

                            interactiveResizeJob =
                                null

                            interactiveControlArmed =
                                false

                            commandRunning =
                                false
                        }
                    }
            }

        viewModelScope
            .launch {

                LinuxInteractiveTerminalSessionController
                    .controlArmed
                    .collect { armed ->

                        interactiveControlArmed =
                            armed
                    }
            }
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

    /*
     * ------------------------------------------------
     * INTERACTIVE TERMINAL GEOMETRY
     * ------------------------------------------------
     *
     * Before an interactive session starts, geometry is
     * simply prepared for launch.
     *
     * Once nano/vim is active, viewport changes become
     * live PTY resize requests.
     *
     * The same mechanism can respond to:
     *
     *     Android keyboard open
     *     Android keyboard closed
     *     multi-window resizing
     *     future desktop/window resizing
     *
     * TerminalScreen only reports the actual available
     * viewport. It does not need to know why the viewport
     * changed.
     */
    fun updateInteractiveTerminalGeometry(
        columns: Int,
        rows: Int
    ) {

        if (
            columns <= 0 ||
            rows <= 0
        ) {

            return
        }

        /*
         * ------------------------------------------------
         * PRE-LAUNCH GEOMETRY
         * ------------------------------------------------
         */
        if (
            !interactiveSessionActive
        ) {

            interactiveResizeJob
                ?.cancel()

            interactiveResizeJob =
                null

            if (
                interactiveTerminalColumns ==
                columns &&
                interactiveTerminalRows ==
                rows
            ) {

                return
            }

            interactiveTerminalColumns =
                columns

            interactiveTerminalRows =
                rows

            return
        }

        /*
         * ------------------------------------------------
         * LIVE PTY GEOMETRY
         * ------------------------------------------------
         */
        if (
            interactiveTerminalColumns ==
            columns &&
            interactiveTerminalRows ==
            rows
        ) {

            return
        }

        /*
         * Replace any resize request that has not yet
         * reached the Linux PTY.
         *
         * This coalesces the several intermediate viewport
         * sizes normally reported during IME animation.
         */
        interactiveResizeJob
            ?.cancel()

        interactiveResizeJob =
            viewModelScope
                .launch {

                    delay(
                        INTERACTIVE_RESIZE_DEBOUNCE_MS
                    )

                    if (
                        !interactiveSessionActive
                    ) {

                        return@launch
                    }

                    val resizeResult =
                        LinuxInteractiveTerminalSessionController
                            .resize(
                                columns =
                                    columns,

                                rows =
                                    rows
                            )

                    when (
                        resizeResult
                    ) {

                        is LinuxInteractiveResizeResult.Success -> {

                            /*
                             * Only publish the new Compose
                             * geometry after both Ubuntu and
                             * termlib accepted it.
                             */
                            interactiveTerminalColumns =
                                resizeResult.columns

                            interactiveTerminalRows =
                                resizeResult.rows
                        }

                        is LinuxInteractiveResizeResult.Failure -> {

                            /*
                             * A resize failure must never
                             * destroy the running editor or
                             * pretend a new geometry exists.
                             *
                             * Keep the last known-good rows
                             * and columns.
                             *
                             * If the process itself ended
                             * during resize, synchronize the
                             * ViewModel immediately.
                             */
                            if (
                                !LinuxInteractiveTerminalSessionController
                                    .isActive
                            ) {

                                interactiveSessionActive =
                                    false

                                interactiveControlArmed =
                                    false

                                commandRunning =
                                    false
                            }
                        }
                    }
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
     *
     * The dimensions prepared by TerminalScreen are
     * supplied through the entire PTY stack at launch.
     *
     * Once the interactive process starts, those values
     * may be updated by the live viewport resize path.
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

        commandRunning =
            true

        /*
         * Snapshot the best geometry currently known for
         * initial PTY creation.
         *
         * Once the interactive viewport is visible,
         * TerminalScreen may report a more accurate size
         * through the live-resize path.
         */
        val launchColumns =
            interactiveTerminalColumns

        val launchRows =
            interactiveTerminalRows

        viewModelScope
            .launch {

                val startResult =
                    withContext(
                        Dispatchers.IO
                    ) {

                        LinuxInteractiveTerminalSessionController
                            .start(
                                command =
                                    command,

                                columns =
                                    launchColumns,

                                rows =
                                    launchRows
                            )
                    }

                when (
                    startResult
                ) {

                    LinuxInteractiveSessionStartResult.Started -> {

                        /*
                         * Read the authoritative dimensions
                         * back from the persistent controller.
                         */
                        interactiveTerminalRows =
                            LinuxInteractiveTerminalSessionController
                                .rows

                        interactiveTerminalColumns =
                            LinuxInteractiveTerminalSessionController
                                .columns

                        interactiveSessionActive =
                            true

                        commandRunning =
                            false
                    }

                    is LinuxInteractiveSessionStartResult.Failure -> {

                        interactiveResizeJob
                            ?.cancel()

                        interactiveResizeJob =
                            null

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
     * ATLAS MOBILE TERMINAL KEYBOARD
     * ------------------------------------------------
     */

    fun toggleInteractiveControl() {

        if (
            !interactiveSessionActive
        ) {

            return
        }

        LinuxInteractiveTerminalSessionController
            .toggleControlArmed()
    }

    fun sendInteractiveEscape() {

        if (
            !interactiveSessionActive
        ) {

            return
        }

        LinuxInteractiveTerminalSessionController
            .sendEscape()
    }

    fun sendInteractiveTab() {

        if (
            !interactiveSessionActive
        ) {

            return
        }

        LinuxInteractiveTerminalSessionController
            .sendTab()
    }

    fun sendInteractiveEnter() {

        if (
            !interactiveSessionActive
        ) {

            return
        }

        LinuxInteractiveTerminalSessionController
            .sendEnter()
    }

    fun sendInteractiveBackspace() {

        if (
            !interactiveSessionActive
        ) {

            return
        }

        LinuxInteractiveTerminalSessionController
            .sendBackspace()
    }

    fun sendInteractiveArrowLeft() {

        if (
            !interactiveSessionActive
        ) {

            return
        }

        LinuxInteractiveTerminalSessionController
            .sendArrowLeft()
    }

    fun sendInteractiveArrowUp() {

        if (
            !interactiveSessionActive
        ) {

            return
        }

        LinuxInteractiveTerminalSessionController
            .sendArrowUp()
    }

    fun sendInteractiveArrowDown() {

        if (
            !interactiveSessionActive
        ) {

            return
        }

        LinuxInteractiveTerminalSessionController
            .sendArrowDown()
    }

    fun sendInteractiveArrowRight() {

        if (
            !interactiveSessionActive
        ) {

            return
        }

        LinuxInteractiveTerminalSessionController
            .sendArrowRight()
    }

    fun sendInteractiveControl(
        character: Char
    ) {

        if (
            !interactiveSessionActive
        ) {

            return
        }

        LinuxInteractiveTerminalSessionController
            .sendControl(
                character
            )
    }

    /*
     * ------------------------------------------------
     * EXPLICIT INTERACTIVE STOP
     * ------------------------------------------------
     */
    fun stopInteractiveSession() {

        if (
            !interactiveSessionActive
        ) {

            return
        }

        /*
         * Never allow a pending viewport resize to race an
         * explicit interactive-session shutdown.
         */
        interactiveResizeJob
            ?.cancel()

        interactiveResizeJob =
            null

        viewModelScope
            .launch {

                withContext(
                    Dispatchers.IO
                ) {

                    LinuxInteractiveTerminalSessionController
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

    /*
     * There is intentionally NO onCleared() override.
     *
     * UI recreation must never destroy the user's active
     * Ubuntu terminal application.
     */
}