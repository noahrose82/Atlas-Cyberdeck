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
import com.noahrose.pocketlab.feature.terminal.TerminalViewModel

@Composable
fun TerminalScreen(
    terminalViewModel: TerminalViewModel = viewModel()
) {

    val uiState =
        terminalViewModel.uiState

    /*
     * ------------------------------------------------
     * TERMINAL VISUAL MODE
     * ------------------------------------------------
     *
     * Atlas shell:
     *
     * white/light background
     * blue Atlas text
     *
     * Ubuntu shell:
     *
     * black background
     * Matrix-green text
     */
    val linuxShellActive =
        terminalViewModel
            .linuxShellActive

    val terminalBackground =
        if (linuxShellActive) {

            Color.Black

        } else {

            MaterialTheme
                .colorScheme
                .background
        }

    val terminalTextColor =
        if (linuxShellActive) {

            /*
             * Classic Matrix-style terminal green.
             */
            Color(
                0xFF00FF41
            )

        } else {

            MaterialTheme
                .colorScheme
                .primary
        }

    val prompt =
        terminalViewModel
            .prompt

    val listState =
        rememberLazyListState()

    /*
     * Keep the latest output visible.
     */
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

        /*
         * ------------------------------------------------
         * TERMINAL OUTPUT
         * ------------------------------------------------
         */
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
         * ------------------------------------------------
         * LIVE TERMINAL INPUT
         * ------------------------------------------------
         */
        BasicTextField(
            value =
                uiState.currentCommand,

            onValueChange = { value ->

                /*
                 * Support keyboards that submit
                 * newline characters directly.
                 */
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

                    /*
                     * Live prompt.
                     *
                     * Atlas:
                     *
                     * atlas@cyberdeck:~$
                     *
                     * Ubuntu:
                     *
                     * root@atlas:~#
                     */
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