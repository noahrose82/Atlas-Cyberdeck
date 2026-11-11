
package com.noahrose.pocketlab.feature.partner.local

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Phase 3: Offline GGUF model loading and text generation.
 *
 * Native inference runs off the Android UI thread.
 * No shell commands or device-control permissions
 * are provided through this engine.
 */
object AtlasLocalAiEngine {

    init {
        System.loadLibrary("atlas_local_ai")
    }

    private external fun nativeLoadModel(
        absolutePath: String,
        nativeLibraryDir: String
    ): Boolean

    private external fun nativeIsModelLoaded(): Boolean

    private external fun nativeUnloadModel()

    private external fun nativeGenerate(
        message: String,
        maxTokens: Int
    ): String

    /**
     * Load a GGUF model from private application storage.
     */
    suspend fun loadModel(
        context: Context,
        file: File
    ): Boolean = withContext(Dispatchers.IO) {

        val modelDirectory = File(
            context.applicationContext.filesDir,
            "models"
        ).canonicalFile

        val canonicalFile = file.canonicalFile

        require(canonicalFile.parentFile == modelDirectory) {
            "The GGUF model must be stored in " +
                    "the app's private models directory."
        }

        require(
            canonicalFile.isFile &&
                    canonicalFile.extension.equals(
                        "gguf",
                        ignoreCase = true
                    )
        ) {
            "A readable GGUF model file is required."
        }

        nativeLoadModel(
            canonicalFile.absolutePath,
            context.applicationInfo.nativeLibraryDir
        )
    }

    /**
     * Returns whether a native model is currently loaded.
     */
    suspend fun isModelLoaded(): Boolean =
        withContext(Dispatchers.IO) {
            nativeIsModelLoaded()
        }

    /**
     * Generate a response entirely on the Android device.
     *
     * The first version generates one reply at a time,
     * without persistent conversation history.
     */
    suspend fun generate(
        message: String,
        maxTokens: Int = 128
    ): String = withContext(Dispatchers.Default) {

        require(message.isNotBlank()) {
            "Message cannot be empty."
        }

        require(maxTokens in 1..256) {
            "maxTokens must be between 1 and 256."
        }

        nativeGenerate(
            message.trim(),
            maxTokens
        )
    }

    /**
     * Free the loaded model.
     */
    suspend fun unloadModel() =
        withContext(Dispatchers.IO) {
            nativeUnloadModel()
        }
}
