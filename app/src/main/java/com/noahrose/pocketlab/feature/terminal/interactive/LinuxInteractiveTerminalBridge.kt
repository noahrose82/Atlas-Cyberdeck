package com.noahrose.pocketlab.feature.terminal.interactive

import androidx.compose.ui.graphics.Color
import com.noahrose.pocketlab.feature.linux.runtime.process.LinuxInteractiveSessionManager
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
     *
     * Android soft keyboards normally do not expose
     * Ctrl.
     *
     * Atlas provides its own Ctrl key. When armed,
     * the next ASCII letter received from the normal
     * Android keyboard becomes a control character.
     *
     * Examples:
     *
     *     CTRL + C = 0x03
     *     CTRL + O = 0x0F
     *     CTRL + X = 0x18
     *
     * Ctrl automatically releases after the letter.
     */
    @Volatile
    private var controlArmed =
        false

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
     */
    fun start(
        command: String
    ) =
        LinuxInteractiveSessionManager
            .start(
                command =
                    command,

                columns =
                    DEFAULT_COLUMNS,

                rows =
                    DEFAULT_ROWS
            )

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
         *
         * Once we consume a letter, Atlas releases
         * the modifier automatically.
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