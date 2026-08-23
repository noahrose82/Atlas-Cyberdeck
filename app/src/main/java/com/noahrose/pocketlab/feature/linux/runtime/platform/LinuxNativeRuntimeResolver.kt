package com.noahrose.pocketlab.feature.linux.runtime.platform

import android.content.Context
import java.io.File

object LinuxNativeRuntimeResolver {

    private const val PROOT_LIBRARY_NAME =
        "libproot_atlas.so"

    private var nativeLibraryDirectory: File? =
        null

    fun initialize(
        context: Context
    ) {

        nativeLibraryDirectory =
            context
                .applicationInfo
                .nativeLibraryDir
                ?.let { path ->
                    File(path)
                }
    }

    fun getProotExecutable(): File? {

        val directory =
            nativeLibraryDirectory
                ?: return null

        return File(
            directory,
            PROOT_LIBRARY_NAME
        )
    }

    fun isProotAvailable(): Boolean {

        val executable =
            getProotExecutable()
                ?: return false

        return executable.exists() &&
                executable.isFile &&
                executable.canExecute()
    }
}