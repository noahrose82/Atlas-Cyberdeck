package com.noahrose.pocketlab.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.noahrose.pocketlab.feature.bridge.AtlasBridgeClient
import com.noahrose.pocketlab.feature.partner.terminal.AtlasPartnerTerminalGateway
import com.noahrose.pocketlab.feature.partner.terminal.AtlasReadOnlyCommand
import io.ktor.client.plugins.ResponseException
import kotlinx.coroutines.launch

private data class PartnerEntry(
    val id: Long,
    val author: String,
    val text: String
)

/**
 * Phase 2: supervised local USB development connection to AtlasBridge.
 * Requires: adb reverse tcp:7331 tcp:7331
 * No passwords or session identifiers are stored on disk.
 * Do not use HTTP authentication over an untrusted Wi-Fi network.
 */
@Composable
fun AtlasPartnerScreen() {
    val bridgeClient = remember { AtlasBridgeClient("http://127.0.0.1:7331") }
    val scope = rememberCoroutineScope()
    val messages = remember { mutableStateListOf<PartnerEntry>() }
    val listState = rememberLazyListState()

    var username by remember { mutableStateOf("atlas") }
    var password by remember { mutableStateOf("") }
    var sessionId by remember { mutableStateOf<String?>(null) }
    var draft by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("Not connected") }
    var busy by remember { mutableStateOf(false) }
    var nextMessageId by remember { mutableStateOf(0L) }
    var pendingCommand by remember { mutableStateOf<AtlasReadOnlyCommand?>(null) }

    DisposableEffect(bridgeClient) {
        onDispose { bridgeClient.close() }
    }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.lastIndex)
        }
    }

    fun addMessage(author: String, text: String) {
        messages.add(PartnerEntry(nextMessageId++, author, text))
    }

    fun sendMessage() {
        val currentSession = sessionId ?: return
        val outgoing = draft.trim()
        if (outgoing.isEmpty() || busy) return

        draft = ""
        addMessage("You", outgoing)
        busy = true
        scope.launch {
            try {
                val result = bridgeClient.partnerChat(currentSession, outgoing)
                status = result.status
                addMessage("AtlasBridge", result.message)
            } catch (error: Exception) {
                status = if (error is ResponseException &&
                    error.response.status.value == 401
                ) {
                    sessionId = null
                    "Session expired — log in again"
                } else {
                    "Request failed"
                }
                addMessage(
                    "System",
                    "Could not send message. Check that AtlasBridge is running and USB forwarding is active."
                )
            } finally {
                busy = false
            }
        }
    }

    if (pendingCommand != null) {
        val command = pendingCommand!!
        AlertDialog(
            onDismissRequest = { if (!busy) pendingCommand = null },
            title = { Text("Approve Ubuntu command?") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(command.description)
                    Text(
                        command.command,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text("This runs locally in the Ubuntu PRoot runtime on your phone.")
                }
            },
            confirmButton = {
                TextButton(
                    enabled = !busy,
                    onClick = {
                        pendingCommand = null
                        busy = true
                        addMessage("You approved", command.command)
                        scope.launch {
                            try {
                                val result = AtlasPartnerTerminalGateway.executeApproved(command)
                                val details = buildString {
                                    append(result.message)
                                    if (result.exitCode != null) append(" Exit: ${result.exitCode}.")
                                    if (result.output.isNotBlank()) append("\n\n" + result.output.take(3500))
                                    if (result.error.isNotBlank()) append("\n\nError: " + result.error.take(1000))
                                }
                                addMessage("Ubuntu", details)
                            } catch (_: Exception) {
                                addMessage("System", "Ubuntu command failed to execute.")
                            } finally {
                                busy = false
                            }
                        }
                    }
                ) { Text("Run command") }
            },
            dismissButton = {
                TextButton(
                    enabled = !busy,
                    onClick = { pendingCommand = null }
                ) { Text("Cancel") }
            }
        )
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = "ATLAS PARTNER",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Your AI Partner. Anywhere.",
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.bodySmall
        )

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text("Partner Core", fontWeight = FontWeight.Bold)
                Text("Bridge: $status", style = MaterialTheme.typography.bodySmall)
                if (sessionId == null) {
                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it },
                        label = { Text("Bridge username") },
                        singleLine = true,
                        enabled = !busy,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Bridge password") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        enabled = !busy,
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done)
                    )
                    Button(
                        onClick = {
                            if (busy || username.isBlank() || password.isEmpty()) return@Button
                            busy = true
                            status = "Signing in..."
                            scope.launch {
                                try {
                                    val result = bridgeClient.login(username.trim(), password)
                                    sessionId = result.sessionId
                                    status = "Authenticated as ${result.username}"
                                } catch (error: Exception) {
                                    status = "Login failed — check credentials and Bridge connection"
                                } finally {
                                    password = ""
                                    busy = false
                                }
                            }
                        },
                        enabled = !busy && username.isNotBlank() && password.isNotEmpty()
                    ) { Text("Connect to AtlasBridge") }
                } else {
                    OutlinedButton(
                        enabled = !busy,
                        onClick = {
                            val oldSession = sessionId ?: return@OutlinedButton
                            sessionId = null
                            status = "Disconnected"
                            scope.launch {
                                try {
                                    bridgeClient.logout(oldSession)
                                } catch (_: Exception) {
                                    // Local session has already been cleared.
                                }
                            }
                        }
                    ) { Text("Log out") }
                }
                Text(
                    "USB development connection. AI chat is active when Ollama is available; terminal commands require approval.",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        HorizontalDivider()

        Text("Ubuntu tools · approval required", style = MaterialTheme.typography.titleSmall)
        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AtlasReadOnlyCommand.entries.forEach { command ->
                OutlinedButton(
                    onClick = { pendingCommand = command },
                    enabled = !busy && sessionId != null
                ) {
                    Text(command.command)
                }
            }
        }

        LazyColumn(
            state = listState,
            modifier = Modifier.weight(1f).fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(messages, key = { it.id }) { entry ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(entry.author, fontWeight = FontWeight.Bold)
                        Text(entry.text)
                    }
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = draft,
                onValueChange = { draft = it },
                label = { Text("Message") },
                singleLine = true,
                enabled = sessionId != null && !busy,
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = { sendMessage() })
            )
            Button(
                onClick = { sendMessage() },
                enabled = sessionId != null && draft.isNotBlank() && !busy
            ) {
                if (busy) {
                    CircularProgressIndicator(strokeWidth = 2.dp)
                } else {
                    Text("Send")
                }
            }
        }
    }
}
