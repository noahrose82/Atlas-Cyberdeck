package com.noahrose.pocketlab.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

private data class PartnerMessage(
    val id: Long,
    val content: String
)

/**
 * Phase 2: local conversation UI only.
 * Messages are NOT sent to an AI service or AtlasBridge yet.
 * They live in memory and disappear when the app process is restarted.
 */
@Composable
fun AtlasPartnerScreen() {
    val messages = remember { mutableStateListOf<PartnerMessage>() }
    var draft by rememberSaveable { mutableStateOf("") }
    var nextId by remember { mutableStateOf(0L) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "ATLAS PARTNER",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Your AI Partner. Anywhere.",
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.bodyMedium
        )
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text("Partner Core", fontWeight = FontWeight.SemiBold)
                Text(
                    "Local conversation test · AI not connected",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    "Messages stay in this app session. Nothing is sent to a server yet.",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
        HorizontalDivider()

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (messages.isEmpty()) {
                item {
                    Text(
                        "Type a message below to test the conversation layout. " +
                                "AI responses will be added after we connect AtlasBridge.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            items(messages, key = { it.id }) { message ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("You", fontWeight = FontWeight.SemiBold)
                        Text(message.content)
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
                modifier = Modifier.weight(1f),
                label = { Text("Message") },
                maxLines = 4
            )
            Button(
                onClick = {
                    val text = draft.trim()
                    if (text.isNotEmpty()) {
                        messages.add(PartnerMessage(id = nextId++, content = text))
                        draft = ""
                    }
                },
                enabled = draft.isNotBlank()
            ) {
                Text("Add")
            }
        }
    }
}
