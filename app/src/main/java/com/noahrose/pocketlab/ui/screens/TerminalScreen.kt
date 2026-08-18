package com.noahrose.pocketlab.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.text.TextStyle
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                MaterialTheme.colorScheme.background
            )
            .padding(16.dp),
        verticalArrangement =
            Arrangement.Top
    ) {

        uiState.output.forEach { line ->

            Text(
                text = line,
                color =
                    MaterialTheme.colorScheme.primary
            )
        }

        Column(
            modifier =
                Modifier.fillMaxWidth()
        ) {

            BasicTextField(
                value =
                    uiState.currentCommand,

                onValueChange = { value ->

                    if (
                        value.contains("\n")
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

                modifier = Modifier
                    .fillMaxWidth()
                    .onPreviewKeyEvent { keyEvent ->

                        if (
                            keyEvent.type ==
                            KeyEventType.KeyDown &&
                            keyEvent.key ==
                            Key.Tab
                        ) {

                            terminalViewModel
                                .completeCommand()

                            true

                        } else {

                            false
                        }
                    },

                keyboardOptions =
                    KeyboardOptions(
                        capitalization =
                            KeyboardCapitalization.None,
                        autoCorrectEnabled =
                            false,
                        keyboardType =
                            KeyboardType.Ascii
                    ),

                textStyle =
                    TextStyle(
                        color =
                            MaterialTheme.colorScheme.primary
                    ),

                decorationBox = { innerTextField ->

                    Column {

                        Text(
                            text =
                                "atlas@cyberdeck:~$ ",
                            color =
                                MaterialTheme.colorScheme.primary
                        )

                        innerTextField()
                    }
                }
            )
        }
    }
}