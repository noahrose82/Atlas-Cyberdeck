package com.noahrose.pocketlab.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.noahrose.pocketlab.feature.linux.runtime.safety.LinuxRuntimeCircuitBreaker
import com.noahrose.pocketlab.feature.linux.runtime.safety.LinuxRuntimeSafetyMode
import com.noahrose.pocketlab.feature.terminal.TerminalViewModel

private val UbuntuTerminalGreen =
    Color(
        0xFF00FF41
    )

private val SafeModeYellow =
    Color(
        0xFFFFD600
    )

private val RecoveryModeAmber =
    Color(
        0xFFFFA000
    )

@Composable
fun TerminalScreen(
    terminalViewModel: TerminalViewModel = viewModel()
) {

    val uiState =
        terminalViewModel.uiState

    val linuxShellActive =
        terminalViewModel
            .linuxShellActive

    /*
     * ------------------------------------------------
     * H4B — VISUAL SAFETY IDENTITY
     * ------------------------------------------------
     *
     * Safety state has visual priority over shell state.
     *
     * NORMAL + Atlas shell:
     *     existing Material theme
     *
     * NORMAL + Ubuntu shell:
     *     black / Matrix green
     *
     * SAFE_MODE:
     *     black / yellow
     *
     * RECOVERY_ARMED:
     *     black / amber
     *
     * This observes the same circuit-breaker StateFlow
     * that controls runtime access, so SAFE/RECOVERY
     * visual changes happen immediately and do not depend
     * on some unrelated terminal output recomposition.
     */
    val safetySnapshot by
    LinuxRuntimeCircuitBreaker
        .snapshotFlow
        .collectAsState()

    val safetyMode =
        safetySnapshot
            .mode

    val terminalBackground =
        when (
            safetyMode
        ) {

            LinuxRuntimeSafetyMode.SAFE_MODE,
            LinuxRuntimeSafetyMode.RECOVERY_ARMED -> {

                Color.Black
            }

            LinuxRuntimeSafetyMode.NORMAL -> {

                if (
                    linuxShellActive
                ) {

                    Color.Black

                } else {

                    MaterialTheme
                        .colorScheme
                        .background
                }
            }
        }

    val terminalTextColor =
        when (
            safetyMode
        ) {

            LinuxRuntimeSafetyMode.SAFE_MODE -> {

                SafeModeYellow
            }

            LinuxRuntimeSafetyMode.RECOVERY_ARMED -> {

                RecoveryModeAmber
            }

            LinuxRuntimeSafetyMode.NORMAL -> {

                if (
                    linuxShellActive
                ) {

                    UbuntuTerminalGreen

                } else {

                    MaterialTheme
                        .colorScheme
                        .primary
                }
            }
        }

    val prompt =
        terminalViewModel
            .prompt

    val listState =
        rememberLazyListState()

    LaunchedEffect(
        uiState.output.size
    ) {

        if (
            uiState
                .output
                .isNotEmpty()
        ) {

            listState
                .animateScrollToItem(
                    uiState.output.lastIndex
                )
        }
    }

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .background(
                    terminalBackground
                )
                .imePadding()
                .padding(
                    16.dp
                )
    ) {

        LazyColumn(
            modifier =
                Modifier
                    .weight(
                        1f
                    )
                    .fillMaxWidth(),

            state =
                listState,

            verticalArrangement =
                Arrangement.spacedBy(
                    2.dp
                )
        ) {

            items(
                items =
                    uiState.output
            ) { line ->

                Text(
                    text =
                        line,

                    color =
                        terminalTextColor
                )
            }
        }

        /*
         * A silent Linux command should never make
         * Atlas appear frozen.
         *
         * This indicator disappears automatically
         * when commandRunning becomes false.
         */
        if (
            terminalViewModel
                .commandRunning
        ) {

            Text(
                text =
                    "Running...",

                color =
                    terminalTextColor,

                modifier =
                    Modifier
                        .padding(
                            top =
                                6.dp
                        )
            )
        }

        BasicTextField(
            value =
                uiState.currentCommand,

            onValueChange = { value ->

                if (
                    value.contains(
                        "\n"
                    )
                ) {

                    terminalViewModel
                        .executeCommand()

                } else {

                    terminalViewModel
                        .updateCommand(
                            value
                        )
                }
            },

            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(
                        top =
                            8.dp
                    )
                    .onPreviewKeyEvent { keyEvent ->

                        if (
                            keyEvent.type ==
                            KeyEventType.KeyDown
                        ) {

                            when (
                                keyEvent.key
                            ) {

                                Key.Tab -> {

                                    terminalViewModel
                                        .completeCommand()

                                    true
                                }

                                Key.Enter -> {

                                    terminalViewModel
                                        .executeCommand()

                                    true
                                }

                                else ->
                                    false
                            }

                        } else {

                            false
                        }
                    },

            singleLine =
                true,

            keyboardOptions =
                KeyboardOptions(
                    capitalization =
                        KeyboardCapitalization.None,

                    autoCorrectEnabled =
                        false,

                    keyboardType =
                        KeyboardType.Ascii,

                    imeAction =
                        ImeAction.Done
                ),

            keyboardActions =
                KeyboardActions(
                    onDone = {

                        terminalViewModel
                            .executeCommand()
                    }
                ),

            textStyle =
                TextStyle(
                    color =
                        terminalTextColor
                ),

            cursorBrush =
                SolidColor(
                    terminalTextColor
                ),

            decorationBox = { innerTextField ->

                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                ) {

                    Text(
                        text =
                            "$prompt ",

                        color =
                            terminalTextColor
                    )

                    Box(
                        modifier =
                            Modifier
                                .weight(
                                    1f
                                )
                    ) {

                        innerTextField()
                    }
                }
            }
        )
    }
}
