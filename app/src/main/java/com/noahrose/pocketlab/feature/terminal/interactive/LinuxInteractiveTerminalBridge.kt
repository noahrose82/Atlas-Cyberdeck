package com.noahrose.pocketlab.feature.terminal.interactive

import androidx.compose.ui.graphics.Color
import com.noahrose.pocketlab.feature.linux.runtime.process.LinuxInteractiveResizeResult
import com.noahrose.pocketlab.feature.linux.runtime.process.LinuxInteractiveSessionManager
import com.noahrose.pocketlab.feature.linux.runtime.process.LinuxInteractiveSessionStartResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.connectbot.terminal.TerminalEmulator
import org.connectbot.terminal.TerminalEmulatorFactory
import java.io.InputStream

class LinuxInteractiveTerminalBridge(
    private val onControlArmedChanged: ((Boolean) -> Unit)? = null
) {

    companion object {

        const val DEFAULT_ROWS =
            24

        const val DEFAULT_COLUMNS =
            80

        private const val ESC =
            0x1B
    }

    /*
     * ------------------------------------------------
     * ONE-SHOT CTRL MODIFIER
     * ------------------------------------------------
     */
    @Volatile
    private var controlArmed =
        false

    /*
     * ------------------------------------------------
     * ACTIVE TERMINAL GEOMETRY
     * ------------------------------------------------
     *
     * These values describe the geometry shared by:
     *
     *     termlib
     *     Ubuntu PTY
     *
     * Once a session is running, Atlas must keep both
     * sides synchronized.
     */
    @Volatile
    private var activeRows =
        DEFAULT_ROWS

    @Volatile
    private var activeColumns =
        DEFAULT_COLUMNS

    val rows: Int
        get() =
            activeRows

    val columns: Int
        get() =
            activeColumns

    /*
     * ------------------------------------------------
     * TERMINAL EMULATOR
     * ------------------------------------------------
     */
    val terminalEmulator:
            TerminalEmulator =
        TerminalEmulatorFactory
            .create(
                initialRows =
                    DEFAULT_ROWS,

                initialCols =
                    DEFAULT_COLUMNS,

                defaultForeground =
                    Color(
                        0xFF00FF41
                    ),

                defaultBackground =
                    Color.Black,

                onKeyboardInput = { data ->

                    val transformedData =
                        transformKeyboardInput(
                            data
                        )

                    LinuxInteractiveSessionManager
                        .write(
                            transformedData
                        )
                }
            )

    /*
     * ------------------------------------------------
     * CTRL STATE
     * ------------------------------------------------
     */
    fun setControlArmed(
        armed: Boolean
    ) {

        controlArmed =
            armed

        onControlArmedChanged
            ?.invoke(
                armed
            )
    }

    fun toggleControlArmed():
            Boolean {

        val newState =
            !controlArmed

        setControlArmed(
            newState
        )

        return newState
    }

    fun isControlArmed():
            Boolean {

        return controlArmed
    }

    /*
     * ------------------------------------------------
     * SESSION START
     * ------------------------------------------------
     *
     * The initial renderer geometry is established before
     * launching the interactive process.
     *
     * LinuxInteractiveProotProcessSpecFactory then gives
     * the newly allocated PTY the same rows and columns.
     */
    fun start(
        command: String,
        columns: Int = DEFAULT_COLUMNS,
        rows: Int = DEFAULT_ROWS
    ): LinuxInteractiveSessionStartResult {

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

        val previousRows =
            activeRows

        val previousColumns =
            activeColumns

        /*
         * Prepare termlib for the same geometry that will
         * be assigned to the Linux PTY.
         */
        try {

            terminalEmulator
                .resize(
                    rows,
                    columns
                )

        } catch (
            exception: Exception
        ) {

            return LinuxInteractiveSessionStartResult
                .Failure(
                    message =
                        "Atlas could not prepare the interactive terminal renderer.",
                    cause =
                        exception
                )
        }

        activeRows =
            rows

        activeColumns =
            columns

        val startResult =
            LinuxInteractiveSessionManager
                .start(
                    command =
                        command,

                    columns =
                        columns,

                    rows =
                        rows
                )

        /*
         * If Linux failed to start, restore the renderer
         * to the geometry it had before this attempt.
         */
        if (
            startResult is
                    LinuxInteractiveSessionStartResult.Failure
        ) {

            try {

                terminalEmulator
                    .resize(
                        previousRows,
                        previousColumns
                    )

            } catch (
                _: Exception
            ) {

                /*
                 * Start already failed.
                 *
                 * Preserve the original Linux failure as
                 * the authoritative result.
                 */
            }

            activeRows =
                previousRows

            activeColumns =
                previousColumns
        }

        return startResult
    }

    /*
     * ------------------------------------------------
     * LIVE TERMINAL RESIZE
     * ------------------------------------------------
     *
     * This is intentionally viewport-driven rather than
     * keyboard-driven.
     *
     * Any change to the actual terminal viewport may
     * request new geometry:
     *
     *     Android IME open / close
     *     portrait / landscape rotation
     *     split-screen resizing
     *     foldable posture changes
     *     DeX / desktop windows
     *
     * LinuxInteractiveSessionManager updates the real PTY
     * first. Only after Ubuntu confirms the new geometry
     * does Atlas resize termlib to match.
     */
    fun resize(
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

        /*
         * Avoid unnecessary stty operations and redraws.
         */
        if (
            columns ==
            activeColumns &&
            rows ==
            activeRows
        ) {

            return LinuxInteractiveResizeResult
                .Success(
                    columns =
                        activeColumns,

                    rows =
                        activeRows
                )
        }

        val previousRows =
            activeRows

        val previousColumns =
            activeColumns

        /*
         * Resize the actual Linux PTY first.
         */
        val linuxResult =
            LinuxInteractiveSessionManager
                .resize(
                    columns =
                        columns,

                    rows =
                        rows
                )

        if (
            linuxResult is
                    LinuxInteractiveResizeResult.Failure
        ) {

            return linuxResult
        }

        /*
         * Ubuntu accepted the new terminal geometry.
         *
         * Keep termlib synchronized with it.
         */
        return try {

            terminalEmulator
                .resize(
                    rows,
                    columns
                )

            activeRows =
                rows

            activeColumns =
                columns

            LinuxInteractiveResizeResult
                .Success(
                    columns =
                        columns,

                    rows =
                        rows
                )

        } catch (
            exception: Exception
        ) {

            /*
             * The kernel resize succeeded but the renderer
             * did not.
             *
             * Attempt to restore the PTY to the previous
             * known-good geometry so the two sides do not
             * remain intentionally mismatched.
             */
            val rollbackResult =
                LinuxInteractiveSessionManager
                    .resize(
                        columns =
                            previousColumns,

                        rows =
                            previousRows
                    )

            try {

                terminalEmulator
                    .resize(
                        previousRows,
                        previousColumns
                    )

            } catch (
                _: Exception
            ) {

                /*
                 * Preserve the resize failure below.
                 */
            }

            activeRows =
                previousRows

            activeColumns =
                previousColumns

            val rollbackMessage =
                when (
                    rollbackResult
                ) {

                    is LinuxInteractiveResizeResult.Success -> {

                        "The previous PTY geometry was restored."
                    }

                    is LinuxInteractiveResizeResult.Failure -> {

                        "Atlas could not confirm PTY rollback: " +
                                rollbackResult.message
                    }
                }

            LinuxInteractiveResizeResult
                .Failure(
                    message =
                        "Ubuntu accepted the new terminal size, but " +
                                "the Atlas renderer could not apply it. " +
                                rollbackMessage
                )
        }
    }

    /*
     * ------------------------------------------------
     * RAW TERMINAL INPUT
     * ------------------------------------------------
     */
    fun sendBytes(
        bytes: ByteArray
    ): Boolean {

        return LinuxInteractiveSessionManager
            .write(
                bytes
            )
    }

    fun sendEscape():
            Boolean {

        return sendBytes(
            byteArrayOf(
                ESC.toByte()
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

    /*
     * ------------------------------------------------
     * DIRECT CTRL CHARACTER
     * ------------------------------------------------
     */
    fun sendControl(
        character: Char
    ): Boolean {

        val upper =
            character
                .uppercaseChar()

        if (
            upper !in
            'A'..'Z'
        ) {

            return false
        }

        val controlByte =
            upper.code and
                    0x1F

        return sendBytes(
            byteArrayOf(
                controlByte.toByte()
            )
        )
    }

    /*
     * ------------------------------------------------
     * ANSI CURSOR KEYS
     * ------------------------------------------------
     */
    fun sendArrowUp():
            Boolean {

        return sendAnsiSequence(
            "[A"
        )
    }

    fun sendArrowDown():
            Boolean {

        return sendAnsiSequence(
            "[B"
        )
    }

    fun sendArrowRight():
            Boolean {

        return sendAnsiSequence(
            "[C"
        )
    }

    fun sendArrowLeft():
            Boolean {

        return sendAnsiSequence(
            "[D"
        )
    }

    /*
     * ------------------------------------------------
     * PTY OUTPUT
     * ------------------------------------------------
     */
    suspend fun pumpOutput() {

        val stdout =
            LinuxInteractiveSessionManager
                .getInputStream()

        val stderr =
            LinuxInteractiveSessionManager
                .getErrorStream()

        coroutineScope {

            if (
                stdout != null
            ) {

                launch(
                    Dispatchers.IO
                ) {

                    pumpStream(
                        stdout
                    )
                }
            }

            if (
                stderr != null
            ) {

                launch(
                    Dispatchers.IO
                ) {

                    pumpStream(
                        stderr
                    )
                }
            }
        }

        LinuxInteractiveSessionManager
            .refresh()

        /*
         * Never leave Ctrl armed after an interactive
         * application exits.
         */
        setControlArmed(
            false
        )
    }

    val isActive:
            Boolean
        get() =
            LinuxInteractiveSessionManager
                .isActive

    fun stop():
            Boolean {

        setControlArmed(
            false
        )

        return LinuxInteractiveSessionManager
            .stop()
    }

    /*
     * ------------------------------------------------
     * ANDROID KEYBOARD → CTRL TRANSFORMATION
     * ------------------------------------------------
     */
    private fun transformKeyboardInput(
        data: ByteArray
    ): ByteArray {

        if (
            !controlArmed ||
            data.size != 1
        ) {

            return data
        }

        val value =
            data[0]
                .toInt() and
                    0xFF

        val character =
            value.toChar()

        val upper =
            when (
                character
            ) {

                in 'a'..'z' ->
                    character
                        .uppercaseChar()

                in 'A'..'Z' ->
                    character

                else ->
                    return data
            }

        val controlByte =
            upper.code and
                    0x1F

        /*
         * CTRL is one-shot.
         */
        setControlArmed(
            false
        )

        return byteArrayOf(
            controlByte.toByte()
        )
    }

    /*
     * ------------------------------------------------
     * ANSI HELPER
     * ------------------------------------------------
     */
    private fun sendAnsiSequence(
        sequence: String
    ): Boolean {

        val sequenceBytes =
            sequence
                .encodeToByteArray()

        val bytes =
            ByteArray(
                sequenceBytes.size +
                        1
            )

        bytes[0] =
            ESC.toByte()

        sequenceBytes
            .copyInto(
                destination =
                    bytes,

                destinationOffset =
                    1
            )

        return sendBytes(
            bytes
        )
    }

    /*
     * ------------------------------------------------
     * RAW STREAM → LIBVTERM
     * ------------------------------------------------
     */
    private fun pumpStream(
        inputStream: InputStream
    ) {

        val buffer =
            ByteArray(
                8192
            )

        while (
            true
        ) {

            val count =
                try {

                    inputStream
                        .read(
                            buffer
                        )

                } catch (
                    _: Exception
                ) {

                    break
                }

            if (
                count < 0
            ) {

                break
            }

            if (
                count == 0
            ) {

                continue
            }

            terminalEmulator
                .writeInput(
                    buffer,
                    0,
                    count
                )
        }
    }
}