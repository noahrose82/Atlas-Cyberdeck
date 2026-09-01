package com.noahrose.pocketlab.feature.terminal.interactive

import com.noahrose.pocketlab.feature.linux.runtime.process.LinuxInteractiveSessionStartResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.connectbot.terminal.TerminalEmulator

/*
 * ------------------------------------------------
 * ATLAS INTERACTIVE TERMINAL SESSION CONTROLLER
 * ------------------------------------------------
 *
 * Application-process owner for interactive Linux
 * terminal sessions.
 *
 * This object deliberately lives above:
 *
 *     TerminalScreen
 *     TerminalViewModel
 *
 * A Compose/ViewModel recreation must NOT terminate
 * nano, vim, top, less, or another active PTY program.
 *
 * Lifetime:
 *
 *     Android application process
 *              ↓
 *     Interactive controller
 *              ↓
 *     Terminal bridge
 *              ↓
 *     Dedicated PRoot
 *              ↓
 *     script / PTY
 *              ↓
 *     nano / vim
 */
object LinuxInteractiveTerminalSessionController {

    /*
     * Independent lifetime from any ViewModel.
     *
     * If TerminalViewModel disappears because Android
     * recreates UI state, this scope remains alive.
     */
    private val controllerScope =
        CoroutineScope(
            SupervisorJob() +
                    Dispatchers.IO
        )

    private var outputPumpJob:
            Job? =
        null

    private val _sessionActive =
        MutableStateFlow(
            false
        )

    val sessionActive:
            StateFlow<Boolean> =
        _sessionActive
            .asStateFlow()

    private val _controlArmed =
        MutableStateFlow(
            false
        )

    val controlArmed:
            StateFlow<Boolean> =
        _controlArmed
            .asStateFlow()

    /*
     * The bridge and its TerminalEmulator are intentionally
     * created exactly once for the Android application
     * process.
     *
     * This is the critical lifecycle change.
     *
     * Previously every TerminalViewModel created its own
     * bridge/emulator and killed the PTY from onCleared().
     */
    private val bridge =
        LinuxInteractiveTerminalBridge(
            onControlArmedChanged = { armed ->

                _controlArmed.value =
                    armed
            }
        )

    val terminalEmulator:
            TerminalEmulator
        get() =
            bridge
                .terminalEmulator

    val isActive:
            Boolean
        get() {

            val active =
                bridge
                    .isActive

            if (
                !active &&
                _sessionActive.value
            ) {

                _sessionActive.value =
                    false

                _controlArmed.value =
                    false
            }

            return active
        }

    /*
     * ------------------------------------------------
     * START
     * ------------------------------------------------
     */
    @Synchronized
    fun start(
        command: String
    ): LinuxInteractiveSessionStartResult {

        /*
         * Never allow two interactive applications to
         * compete for Atlas terminal ownership.
         */
        if (
            bridge.isActive ||
            _sessionActive.value
        ) {

            _sessionActive.value =
                bridge.isActive

            return LinuxInteractiveSessionStartResult
                .Failure(
                    message =
                        "An interactive Ubuntu session is already running."
                )
        }

        /*
         * Clear stale pump bookkeeping from a completed
         * prior session.
         */
        outputPumpJob
            ?.cancel()

        outputPumpJob =
            null

        _controlArmed.value =
            false

        val startResult =
            bridge
                .start(
                    command
                )

        if (
            startResult !is
                    LinuxInteractiveSessionStartResult.Started
        ) {

            _sessionActive.value =
                false

            return startResult
        }

        _sessionActive.value =
            true

        /*
         * ------------------------------------------------
         * PERSISTENT RAW OUTPUT PUMP
         * ------------------------------------------------
         *
         * This coroutine belongs to the application-level
         * controller rather than TerminalViewModel.
         *
         * UI recreation therefore does not cancel the PTY
         * reader.
         */
        outputPumpJob =
            controllerScope
                .launch {

                    try {

                        bridge
                            .pumpOutput()

                    } finally {

                        _sessionActive.value =
                            false

                        _controlArmed.value =
                            false

                        outputPumpJob =
                            null
                    }
                }

        return LinuxInteractiveSessionStartResult
            .Started
    }

    /*
     * ------------------------------------------------
     * INTERACTIVE INPUT
     * ------------------------------------------------
     */

    fun toggleControlArmed():
            Boolean {

        if (
            !isActive
        ) {

            return false
        }

        return bridge
            .toggleControlArmed()
    }

    fun sendEscape():
            Boolean {

        if (
            !isActive
        ) {

            return false
        }

        return bridge
            .sendEscape()
    }

    fun sendTab():
            Boolean {

        if (
            !isActive
        ) {

            return false
        }

        return bridge
            .sendTab()
    }

    fun sendEnter():
            Boolean {

        if (
            !isActive
        ) {

            return false
        }

        return bridge
            .sendEnter()
    }

    fun sendBackspace():
            Boolean {

        if (
            !isActive
        ) {

            return false
        }

        return bridge
            .sendBackspace()
    }

    fun sendArrowLeft():
            Boolean {

        if (
            !isActive
        ) {

            return false
        }

        return bridge
            .sendArrowLeft()
    }

    fun sendArrowUp():
            Boolean {

        if (
            !isActive
        ) {

            return false
        }

        return bridge
            .sendArrowUp()
    }

    fun sendArrowDown():
            Boolean {

        if (
            !isActive
        ) {

            return false
        }

        return bridge
            .sendArrowDown()
    }

    fun sendArrowRight():
            Boolean {

        if (
            !isActive
        ) {

            return false
        }

        return bridge
            .sendArrowRight()
    }

    fun sendControl(
        character: Char
    ): Boolean {

        if (
            !isActive
        ) {

            return false
        }

        return bridge
            .sendControl(
                character
            )
    }

    /*
     * ------------------------------------------------
     * EXPLICIT STOP
     * ------------------------------------------------
     *
     * UI recreation does NOT call this.
     *
     * Explicit runtime shutdown, safety transitions,
     * Linux removal, or user-requested session
     * termination may call this.
     */
    @Synchronized
    fun stop():
            Boolean {

        _controlArmed.value =
            false

        val stopped =
            bridge
                .stop()

        if (
            stopped
        ) {

            outputPumpJob
                ?.cancel()

            outputPumpJob =
                null

            _sessionActive.value =
                false
        }

        return stopped
    }
}