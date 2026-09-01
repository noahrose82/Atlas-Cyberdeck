package com.noahrose.pocketlab.feature.terminal.interactive

import com.noahrose.pocketlab.feature.linux.runtime.process.LinuxInteractiveResizeResult
import com.noahrose.pocketlab.feature.linux.runtime.process.LinuxInteractiveSessionStartResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.connectbot.terminal.TerminalEmulator

object LinuxInteractiveTerminalSessionController {

    /*
     * ------------------------------------------------
     * PERSISTENT CONTROLLER SCOPE
     * ------------------------------------------------
     *
     * This scope belongs to the Atlas application process,
     * not to TerminalViewModel.
     *
     * Therefore an active nano/vim session can survive:
     *
     *     Terminal -> Dashboard -> Terminal
     *     Activity recreation
     *     ViewModel recreation
     */
    private val controllerScope =
        CoroutineScope(
            SupervisorJob() +
                    Dispatchers.IO
        )

    /*
     * Serialize live resize operations.
     *
     * LinuxGuestCommandExecutor is itself synchronized,
     * but this mutex also protects the renderer side from
     * overlapping geometry transitions.
     */
    private val resizeMutex =
        Mutex()

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
     * ------------------------------------------------
     * PERSISTENT TERMINAL BRIDGE
     * ------------------------------------------------
     */
    private val bridge =
        LinuxInteractiveTerminalBridge(
            onControlArmedChanged = { armed ->

                _controlArmed.value =
                    armed
            }
        )

    private var outputPumpJob:
            Job? =
        null

    init {

        /*
         * Synchronize StateFlows with any session already
         * owned by the underlying process manager.
         */
        _sessionActive.value =
            bridge.isActive

        _controlArmed.value =
            bridge.isControlArmed()
    }

    /*
     * ------------------------------------------------
     * TERMINAL STATE
     * ------------------------------------------------
     */
    val terminalEmulator:
            TerminalEmulator
        get() =
            bridge.terminalEmulator

    val rows: Int
        get() =
            bridge.rows

    val columns: Int
        get() =
            bridge.columns

    val isActive: Boolean
        get() =
            bridge.isActive

    /*
     * ------------------------------------------------
     * SESSION START
     * ------------------------------------------------
     */
    fun start(
        command: String,
        columns: Int =
            LinuxInteractiveTerminalBridge.DEFAULT_COLUMNS,
        rows: Int =
            LinuxInteractiveTerminalBridge.DEFAULT_ROWS
    ): LinuxInteractiveSessionStartResult {

        if (
            bridge.isActive
        ) {

            _sessionActive.value =
                true

            return LinuxInteractiveSessionStartResult
                .Failure(
                    message =
                        "An interactive Ubuntu session is already running."
                )
        }

        if (
            columns <= 0 ||
            rows <= 0
        ) {

            return LinuxInteractiveSessionStartResult
                .Failure(
                    message =
                        "Interactive terminal dimensions must be greater than zero."
                )
        }

        val startResult =
            bridge
                .start(
                    command =
                        command,

                    columns =
                        columns,

                    rows =
                        rows
                )

        when (
            startResult
        ) {

            is LinuxInteractiveSessionStartResult.Started -> {

                _sessionActive.value =
                    true

                _controlArmed.value =
                    bridge
                        .isControlArmed()

                startOutputPump()
            }

            is LinuxInteractiveSessionStartResult.Failure -> {

                _sessionActive.value =
                    bridge.isActive

                _controlArmed.value =
                    bridge
                        .isControlArmed()
            }
        }

        return startResult
    }

    /*
     * ------------------------------------------------
     * LIVE VIEWPORT RESIZE
     * ------------------------------------------------
     *
     * This operation is intentionally suspendable.
     *
     * Updating the Linux PTY uses the persistent Ubuntu
     * guest command executor and must never block Compose's
     * UI thread.
     *
     * The caller may request this whenever the actual
     * terminal viewport changes.
     */
    suspend fun resize(
        columns: Int,
        rows: Int
    ): LinuxInteractiveResizeResult {

        if (
            columns <= 0 ||
            rows <= 0
        ) {

            return LinuxInteractiveResizeResult
                .Failure(
                    message =
                        "Interactive terminal dimensions must be greater than zero."
                )
        }

        if (
            !bridge.isActive
        ) {

            _sessionActive.value =
                false

            return LinuxInteractiveResizeResult
                .Failure(
                    message =
                        "No interactive Ubuntu session is active."
                )
        }

        return resizeMutex
            .withLock {

                /*
                 * Re-check after waiting for another resize
                 * operation to finish.
                 */
                if (
                    !bridge.isActive
                ) {

                    _sessionActive.value =
                        false

                    return@withLock LinuxInteractiveResizeResult
                        .Failure(
                            message =
                                "The interactive Ubuntu session ended before resize."
                        )
                }

                val result =
                    withContext(
                        Dispatchers.IO
                    ) {

                        bridge
                            .resize(
                                columns =
                                    columns,

                                rows =
                                    rows
                            )
                    }

                /*
                 * Keep controller state synchronized even
                 * if the interactive process exits during
                 * the resize attempt.
                 */
                _sessionActive.value =
                    bridge.isActive

                result
            }
    }

    /*
     * ------------------------------------------------
     * OUTPUT PUMP
     * ------------------------------------------------
     */
    private fun startOutputPump() {

        /*
         * Only one output pump may own the interactive
         * streams at a time.
         */
        outputPumpJob
            ?.cancel()

        outputPumpJob =
            controllerScope
                .launch {

                    try {

                        bridge
                            .pumpOutput()

                    } finally {

                        _sessionActive.value =
                            bridge.isActive

                        _controlArmed.value =
                            bridge
                                .isControlArmed()

                        outputPumpJob =
                            null
                    }
                }
    }

    /*
     * ------------------------------------------------
     * RAW INPUT
     * ------------------------------------------------
     */
    fun sendBytes(
        bytes: ByteArray
    ): Boolean {

        return bridge
            .sendBytes(
                bytes
            )
    }

    fun sendText(
        text: String
    ): Boolean {

        return sendBytes(
            text.encodeToByteArray()
        )
    }

    /*
     * ------------------------------------------------
     * CTRL
     * ------------------------------------------------
     */
    fun setControlArmed(
        armed: Boolean
    ) {

        bridge
            .setControlArmed(
                armed
            )
    }

    fun toggleControlArmed():
            Boolean {

        return bridge
            .toggleControlArmed()
    }

    /*
     * ------------------------------------------------
     * TERMINAL KEYS
     * ------------------------------------------------
     */
    fun sendEscape():
            Boolean {

        return bridge
            .sendEscape()
    }

    fun sendTab():
            Boolean {

        return bridge
            .sendTab()
    }

    fun sendEnter():
            Boolean {

        return bridge
            .sendEnter()
    }

    fun sendBackspace():
            Boolean {

        return bridge
            .sendBackspace()
    }

    fun sendControl(
        character: Char
    ): Boolean {

        return bridge
            .sendControl(
                character
            )
    }

    fun sendArrowUp():
            Boolean {

        return bridge
            .sendArrowUp()
    }

    fun sendArrowDown():
            Boolean {

        return bridge
            .sendArrowDown()
    }

    fun sendArrowLeft():
            Boolean {

        return bridge
            .sendArrowLeft()
    }

    fun sendArrowRight():
            Boolean {

        return bridge
            .sendArrowRight()
    }

    /*
     * ------------------------------------------------
     * STATE REFRESH
     * ------------------------------------------------
     */
    fun refresh():
            Boolean {

        val active =
            bridge.isActive

        _sessionActive.value =
            active

        if (
            !active
        ) {

            bridge
                .setControlArmed(
                    false
                )
        }

        return active
    }

    /*
     * ------------------------------------------------
     * SESSION STOP
     * ------------------------------------------------
     */
    fun stop():
            Boolean {

        bridge
            .setControlArmed(
                false
            )

        val stopped =
            bridge
                .stop()

        _sessionActive.value =
            bridge.isActive

        _controlArmed.value =
            bridge
                .isControlArmed()

        if (
            stopped
        ) {

            outputPumpJob
                ?.cancel()

            outputPumpJob =
                null
        }

        return stopped
    }
}