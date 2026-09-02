package com.noahrose.pocketlab.feature.terminal.interactive

import com.noahrose.pocketlab.feature.linux.runtime.process.LinuxInteractiveResizeResult
import com.noahrose.pocketlab.feature.linux.runtime.process.LinuxInteractiveSessionStartResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.connectbot.terminal.TerminalEmulator
import kotlin.time.Duration.Companion.milliseconds

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

    /*
     * Pumps PTY stdout/stderr into termlib.
     */
    private var outputPumpJob:
            Job? =
        null

    /*
     * Independently watches the interactive process.
     *
     * This is intentionally separate from the output pump.
     *
     * PTY streams can remain open momentarily after an
     * interactive application exits. Atlas must not require
     * stream EOF before recognizing that nano/vim ended.
     */
    private var sessionMonitorJob:
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

        /*
         * If the application-level controller is recreated
         * while the underlying process still exists, restore
         * lifecycle monitoring as well.
         */
        if (
            bridge.isActive
        ) {

            startSessionMonitor()
        }
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

                /*
                 * Lifecycle ownership must not depend on
                 * stdout/stderr reaching EOF.
                 */
                startSessionMonitor()
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

            markSessionFinished()

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

                    markSessionFinished()

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
                if (
                    bridge.isActive
                ) {

                    _sessionActive.value =
                        true

                } else {

                    markSessionFinished()
                }

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

                        /*
                         * Stream completion is still useful
                         * lifecycle evidence, but it is no
                         * longer the only lifecycle signal.
                         */
                        if (
                            bridge.isActive
                        ) {

                            _sessionActive.value =
                                true

                            _controlArmed.value =
                                bridge
                                    .isControlArmed()

                        } else {

                            markSessionFinished(
                                cancelOutputPump =
                                    false
                            )
                        }

                        outputPumpJob =
                            null
                    }
                }
    }

    /*
     * ------------------------------------------------
     * SESSION LIFECYCLE MONITOR
     * ------------------------------------------------
     *
     * Nano, Vim and other full-screen PTY applications may
     * close their process before every inherited stream has
     * reached EOF.
     *
     * Waiting exclusively for pumpOutput() can therefore
     * leave Atlas displaying an empty interactive terminal.
     *
     * The process is the authoritative lifecycle signal.
     */
    private fun startSessionMonitor() {

        sessionMonitorJob
            ?.cancel()

        sessionMonitorJob =
            controllerScope
                .launch {

                    try {

                        while (
                            true
                        ) {

                            val active =
                                bridge.isActive

                            if (
                                !active
                            ) {

                                markSessionFinished()

                                break
                            }

                            /*
                             * Keep the StateFlow authoritative
                             * even after UI recreation.
                             */
                            _sessionActive.value =
                                true

                            delay(
                                100.milliseconds
                            )
                        }

                    } finally {

                        sessionMonitorJob =
                            null
                    }
                }
    }

    /*
     * ------------------------------------------------
     * SESSION COMPLETION
     * ------------------------------------------------
     *
     * Natural command completion is different from an
     * explicit user-requested stop.
     *
     * Nano/Vim exiting should:
     *
     *     release Ctrl
     *     mark the PTY inactive
     *     return Compose to the normal Ubuntu terminal
     *
     * It must NOT stop the main Ubuntu runtime.
     */
    private fun markSessionFinished(
        cancelOutputPump: Boolean =
            true
    ) {

        bridge
            .setControlArmed(
                false
            )

        _controlArmed.value =
            false

        _sessionActive.value =
            false

        if (
            cancelOutputPump
        ) {

            outputPumpJob
                ?.cancel()
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

        if (
            !bridge.isActive
        ) {

            markSessionFinished()

            return false
        }

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

        if (
            !bridge.isActive
        ) {

            markSessionFinished()

            return
        }

        bridge
            .setControlArmed(
                armed
            )
    }

    fun toggleControlArmed():
            Boolean {

        if (
            !bridge.isActive
        ) {

            markSessionFinished()

            return false
        }

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

        return sendBytes(
            byteArrayOf(
                0x1B.toByte()
            )
        )
    }

    fun sendTab():
            Boolean {

        return sendBytes(
            byteArrayOf(
                '\t'.code.toByte()
            )
        )
    }

    fun sendEnter():
            Boolean {

        return sendBytes(
            byteArrayOf(
                '\r'.code.toByte()
            )
        )
    }

    fun sendBackspace():
            Boolean {

        return sendBytes(
            byteArrayOf(
                0x7F.toByte()
            )
        )
    }

    fun sendControl(
        character: Char
    ): Boolean {

        if (
            !bridge.isActive
        ) {

            markSessionFinished()

            return false
        }

        return bridge
            .sendControl(
                character
            )
    }

    fun sendArrowUp():
            Boolean {

        if (
            !bridge.isActive
        ) {

            markSessionFinished()

            return false
        }

        return bridge
            .sendArrowUp()
    }

    fun sendArrowDown():
            Boolean {

        if (
            !bridge.isActive
        ) {

            markSessionFinished()

            return false
        }

        return bridge
            .sendArrowDown()
    }

    fun sendArrowLeft():
            Boolean {

        if (
            !bridge.isActive
        ) {

            markSessionFinished()

            return false
        }

        return bridge
            .sendArrowLeft()
    }

    fun sendArrowRight():
            Boolean {

        if (
            !bridge.isActive
        ) {

            markSessionFinished()

            return false
        }

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

        if (
            active
        ) {

            _sessionActive.value =
                true

            _controlArmed.value =
                bridge
                    .isControlArmed()

        } else {

            markSessionFinished()
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

        val active =
            bridge.isActive

        _sessionActive.value =
            active

        _controlArmed.value =
            bridge
                .isControlArmed()

        if (
            !active
        ) {

            outputPumpJob
                ?.cancel()

            outputPumpJob =
                null

            sessionMonitorJob
                ?.cancel()

            sessionMonitorJob =
                null
        }

        return stopped
    }
}