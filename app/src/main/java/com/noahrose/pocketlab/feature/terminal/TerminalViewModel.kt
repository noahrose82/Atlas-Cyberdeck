package com.noahrose.pocketlab.feature.terminal

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.noahrose.pocketlab.feature.terminal.completion.CommandCompletion
import com.noahrose.pocketlab.feature.terminal.startup.AtlasRcManager

class TerminalViewModel : ViewModel() {

    var uiState by mutableStateOf(
        TerminalUiState()
    )
        private set

    init {
        loadStartupConfiguration()
    }

    private fun loadStartupConfiguration() {

        val output =
            uiState.output.toMutableList()

        val executed =
            AtlasRcManager.execute(
                output = output
            )

        if (executed) {

            uiState =
                uiState.copy(
                    output = output
                )
        }
    }

    fun updateCommand(
        command: String
    ) {

        uiState =
            uiState.copy(
                currentCommand = command
            )
    }

    fun completeCommand() {

        val completion =
            CommandCompletion.complete(
                uiState.currentCommand
            )

        if (completion != null) {

            uiState =
                uiState.copy(
                    currentCommand = completion
                )
        }
    }

    fun executeCommand() {

        val command =
            uiState.currentCommand.trim()

        if (command.isEmpty()) {
            return
        }

        val output =
            uiState.output.toMutableList()

        TerminalCommandProcessor.process(
            command = command,
            output = output
        )

        uiState =
            uiState.copy(
                output = output,
                currentCommand = ""
            )
    }
}