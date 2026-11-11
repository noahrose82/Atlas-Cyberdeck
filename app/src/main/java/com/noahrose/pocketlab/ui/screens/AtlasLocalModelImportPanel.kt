
package com.noahrose.pocketlab.ui.screens

import android.content.Context
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
 * Phase 3: Local GGUF import, native model loading,
 * and controlled offline text-generation testing.
 *
 * AI generation does not execute terminal commands.
 */
@Composable
fun AtlasLocalModelImportPanel() {

    val context = LocalContext.current.applicationContext
    val scope = rememberCoroutineScope()

    var busy by remember {
        mutableStateOf(false)
    }

    var loaded by remember {
        mutableStateOf(false)
    }

    var status by remember {
        mutableStateOf("No local model loaded")
    }

    var generatedResponse by remember {
        mutableStateOf("")
    }

    LaunchedEffect(Unit) {
        loaded = try {
            AtlasLocalAiEngine.isModelLoaded()
        } catch (_: Exception) {
            false
        }

        if (loaded) {
            status = "Local GGUF model loaded"
        }
    }

    val picker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->

        if (uri != null && !busy) {

            busy = true
            generatedResponse = ""
            status = "Importing model into private storage..."

            scope.launch {
                try {
                    // Unload native model before replacing its file.
                    AtlasLocalAiEngine.unloadModel()
                    loaded = false

                    val modelFile = withContext(Dispatchers.IO) {
                        importGgufToPrivateStorage(
                            context,
                            uri
                        )
                    }

                    status = "Loading GGUF into native engine..."

                    loaded = AtlasLocalAiEngine.loadModel(
                        context,
                        modelFile
                    )

                    status = if (loaded) {
                        "Local model loaded. Ready for offline test."
                    } else {
                        "Native engine could not load this GGUF model"
                    }

                } catch (error: Exception) {
                    loaded = false

                    status =
                        "Import/load failed: " +
                                (error.message ?: "Unknown error")

                } finally {
                    busy = false
                }
            }
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            Text(
                text = "Local AI model",
                style = MaterialTheme.typography.titleSmall
            )

            Text(
                text = status,
                style = MaterialTheme.typography.bodySmall
            )

            if (busy) {
                CircularProgressIndicator()
            }

            Button(
                onClick = {
                    picker.launch(arrayOf("*/*"))
                },
                enabled = !busy
            ) {
                Text("Import GGUF from Downloads")
            }

            if (loaded) {

                Button(
                    enabled = !busy,
                    onClick = {
                        busy = true
                        generatedResponse = ""
                        status = "Generating offline response..."

                        scope.launch {
                            try {
                                generatedResponse =
                                    AtlasLocalAiEngine.generate(
                                        message =
                                            "Hello Atlas. " +
                                                    "Introduce yourself in one sentence.",
                                        maxTokens = 96
                                    )

                                status =
                                    "Offline generation completed"

                            } catch (error: Exception) {
                                status =
                                    "Offline test failed: " +
                                            (error.message
                                                ?: error.javaClass.simpleName)

                            } finally {
                                busy = false
                            }
                        }
                    }
                ) {
                    Text("Test Offline AI")
                }

                if (generatedResponse.isNotBlank()) {

                    Text(
                        text = "Atlas response",
                        style =
                            MaterialTheme.typography.titleSmall
                    )

                    Text(
                        text = generatedResponse,
                        style =
                            MaterialTheme.typography.bodyMedium
                    )
                }

                OutlinedButton(
                    enabled = !busy,
                    onClick = {
                        busy = true

                        scope.launch {
                            try {
                                AtlasLocalAiEngine.unloadModel()

                                loaded = false
                                generatedResponse = ""
                                status = "Model unloaded"

                            } catch (error: Exception) {
                                status =
                                    "Unload failed: " +
                                            (error.message ?: "Unknown error")

                            } finally {
                                busy = false
                            }
                        }
                    }
                ) {
                    Text("Unload model")
                }
            }
        }
    }
}

/**
 * Copies a GGUF into private application storage.
 *
 * A temporary file is used so an incomplete copy
 * is not mistaken for a valid model.
 */
private fun importGgufToPrivateStorage(
    context: Context,
    uri: Uri
): File {

    val directory = File(context.filesDir, "models")

    if (!directory.isDirectory && !directory.mkdirs()) {
        throw IOException(
            "Could not create private models folder"
        )
    }

    val destination = File(
        directory,
        "atlas-local-model.gguf"
    )

    val temporary = File(
        directory,
        "atlas-local-model.gguf.part"
    )

    val maxBytes = 4L * 1024 * 1024 * 1024
    val reserveBytes = 512L * 1024 * 1024

    val available = StatFs(
        directory.absolutePath
    ).availableBytes

    var count = 0L

    try {
        val input = context.contentResolver
            .openInputStream(uri)
            ?: throw IOException(
                "Unable to open selected file"
            )

        input.use { source ->

            FileOutputStream(temporary).use { target ->

                val buffer = ByteArray(1024 * 1024)
                val magic = ByteArray(4)

                if (
                    source.read(magic) != 4 ||
                    !magic.contentEquals(
                        byteArrayOf(71, 71, 85, 70)
                    )
                ) {
                    throw IOException(
                        "Selected file is not a GGUF model"
                    )
                }

                target.write(magic)
                count += 4

                while (true) {

                    val read = source.read(buffer)

                    if (read < 0) break
                    if (read == 0) continue

                    count += read

                    if (count > maxBytes) {
                        throw IOException(
                            "Model is larger than 4 GiB"
                        )
                    }

                    if (
                        count + reserveBytes >
                        available + destination.length()
                    ) {
                        throw IOException(
                            "Not enough free internal storage " +
                                    "for model import"
                        )
                    }

                    target.write(
                        buffer,
                        0,
                        read
                    )
                }

                target.fd.sync()
            }
        }

        if (count < 1024) {
            throw IOException(
                "GGUF file is unexpectedly small"
            )
        }

        // Replace the old copy only after the
        // new model has been fully written.
        if (
            destination.exists() &&
            !destination.delete()
        ) {
            throw IOException(
                "Could not replace old model"
            )
        }

        if (!temporary.renameTo(destination)) {
            throw IOException(
                "Could not finish model import"
            )
        }

        return destination

    } finally {
        if (temporary.exists()) {
            temporary.delete()
        }
    }
}
