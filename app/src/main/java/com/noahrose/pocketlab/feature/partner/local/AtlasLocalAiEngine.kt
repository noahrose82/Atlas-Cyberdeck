package com.noahrose.pocketlab.feature.partner.local

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/** Phase 3: local GGUF loading only; no text generation or shell execution. */
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

    /** Loads a GGUF previously copied into app-private files/model storage. */
    suspend fun loadModel(context: Context, file: File): Boolean = withContext(Dispatchers.IO) {
        val modelDirectory = File(context.applicationContext.filesDir, "models").canonicalFile
        val canonicalFile = file.canonicalFile
        require(canonicalFile.parentFile == modelDirectory) {
            "The GGUF model must be stored in the app's private models directory."
        }
        require(canonicalFile.isFile && canonicalFile.extension.equals("gguf", ignoreCase = true)) {
            "A readable GGUF model file is required."
        }
        nativeLoadModel(canonicalFile.absolutePath, context.applicationInfo.nativeLibraryDir)
    }

    suspend fun isModelLoaded(): Boolean = withContext(Dispatchers.IO) {
        nativeIsModelLoaded()
    }

    suspend fun unloadModel() = withContext(Dispatchers.IO) {
        nativeUnloadModel()
    }
}
