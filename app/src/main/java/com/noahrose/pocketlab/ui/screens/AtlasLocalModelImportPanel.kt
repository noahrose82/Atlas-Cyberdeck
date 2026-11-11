package com.noahrose.pocketlab.ui.screens

import android.net.Uri
import android.os.StatFs
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.noahrose.pocketlab.feature.partner.local.AtlasLocalAiEngine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

/**
 * Phase 3: imports one GGUF into app-private storage and verifies native loading.
 * This does not perform text generation or execute terminal commands.
 */
@Composable
fun AtlasLocalModelImportPanel() {
    val context = LocalContext.current.applicationContext
    val scope = rememberCoroutineScope()
    var busy by remember { mutableStateOf(false) }
    var loaded by remember { mutableStateOf(false) }
    var status by remember { mutableStateOf("No local model loaded") }

    LaunchedEffect(Unit) {
        loaded = try { AtlasLocalAiEngine.isModelLoaded() } catch (_: Exception) { false }
        if (loaded) status = "Local GGUF model loaded"
    }

    val picker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null && !busy) {
            busy = true
            status = "Importing model into private storage..."
            scope.launch {
                try {
                    // Never overwrite a model file while native code could be using it.
                    AtlasLocalAiEngine.unloadModel()
                    loaded = false
                    val modelFile = withContext(Dispatchers.IO) {
                        importGgufToPrivateStorage(context, uri)
                    }
                    status = "Loading GGUF into the native engine..."
                    loaded = AtlasLocalAiEngine.loadModel(context, modelFile)
                    status = if (loaded) {
                        "Local model loaded successfully (generation not enabled yet)"
                    } else {
                        "Native engine could not load this GGUF model"
                    }
                } catch (error: Exception) {
                    loaded = false
                    status = "Import/load failed: ${error.message ?: "Unknown error"}"
                } finally {
                    busy = false
                }
            }
        }
    }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("Local AI model", style = MaterialTheme.typography.titleSmall)
            Text(status, style = MaterialTheme.typography.bodySmall)
            if (busy) CircularProgressIndicator()
            Button(
                onClick = { picker.launch(arrayOf("*/*")) },
                enabled = !busy
            ) {
                Text("Import GGUF from Downloads")
            }
            if (loaded) {
                OutlinedButton(
                    enabled = !busy,
                    onClick = {
                        busy = true
                        scope.launch {
                            try {
                                AtlasLocalAiEngine.unloadModel()
                                loaded = false
                                status = "Model unloaded"
                            } catch (error: Exception) {
                                status = "Unload failed: ${error.message ?: "Unknown error"}"
                            } finally {
                                busy = false
                            }
                        }
                    }
                ) { Text("Unload model") }
            }
        }
    }
}

private fun importGgufToPrivateStorage(context: android.content.Context, uri: Uri): File {
    val directory = File(context.filesDir, "models")
    if (!directory.isDirectory && !directory.mkdirs()) {
        throw IOException("Could not create private models folder")
    }
    val destination = File(directory, "atlas-local-model.gguf")
    val temporary = File(directory, "atlas-local-model.gguf.part")
    val maxBytes = 4L * 1024 * 1024 * 1024 // Early testing: at most 4 GiB.
    val available = StatFs(directory.absolutePath).availableBytes
    val reserveBytes = 512L * 1024 * 1024
    var count = 0L

    try {
        val input = context.contentResolver.openInputStream(uri)
            ?: throw IOException("Unable to open selected file")
        input.use { source ->
            FileOutputStream(temporary).use { target ->
                val buffer = ByteArray(1024 * 1024)
                val magic = ByteArray(4)
                if (source.read(magic) != 4 || !magic.contentEquals(byteArrayOf(71, 71, 85, 70))) {
                    throw IOException("Selected file is not a GGUF model")
                }
                target.write(magic)
                count += 4
                while (true) {
                    val read = source.read(buffer)
                    if (read < 0) break
                    if (read == 0) continue
                    count += read
                    if (count > maxBytes) throw IOException("Model is larger than 4 GiB")
                    if (count + reserveBytes > available + destination.length()) {
                        throw IOException("Not enough free internal storage for model import")
                    }
                    target.write(buffer, 0, read)
                }
                target.fd.sync()
            }
        }
        if (count < 1024) throw IOException("GGUF file is unexpectedly small")
        // Replace existing copy only after the new file has been fully written.
        if (destination.exists() && !destination.delete()) {
            throw IOException("Could not replace old model")
        }
        if (!temporary.renameTo(destination)) {
            throw IOException("Could not finish model import")
        }
        return destination
    } finally {
        if (temporary.exists()) temporary.delete()
    }
}
