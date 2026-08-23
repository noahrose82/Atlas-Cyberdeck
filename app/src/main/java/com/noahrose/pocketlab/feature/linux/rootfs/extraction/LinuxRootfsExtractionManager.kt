package com.noahrose.pocketlab.feature.linux.rootfs.extraction

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

object LinuxRootfsExtractionManager {

    private const val TAG =
        "AtlasRootfs"

    private val _state =
        MutableStateFlow<LinuxRootfsExtractionState>(
            LinuxRootfsExtractionState.Idle
        )

    val state: StateFlow<LinuxRootfsExtractionState> =
        _state.asStateFlow()

    suspend fun extract():
            LinuxRootfsExtractionResult =
        withContext(
            Dispatchers.IO
        ) {

            if (
                _state.value is
                        LinuxRootfsExtractionState.Extracting
            ) {

                val result =
                    LinuxRootfsExtractionResult
                        .Failure(
                            message =
                                "Rootfs extraction is already in progress."
                        )

                Log.e(
                    TAG,
                    result.message
                )

                return@withContext result
            }

            _state.value =
                LinuxRootfsExtractionState
                    .Extracting

            Log.i(
                TAG,
                "Beginning Ubuntu rootfs extraction."
            )

            when (
                val result =
                    LinuxRootfsExtractor
                        .extract()
            ) {

                is LinuxRootfsExtractionResult.Success -> {

                    Log.i(
                        TAG,
                        "Rootfs extraction completed. " +
                                "entries=${result.entriesExtracted}, " +
                                "bytes=${result.bytesExtracted}"
                    )

                    _state.value =
                        LinuxRootfsExtractionState
                            .Ready(
                                entriesExtracted =
                                    result.entriesExtracted,

                                bytesExtracted =
                                    result.bytesExtracted
                            )

                    result
                }

                is LinuxRootfsExtractionResult.Failure -> {

                    Log.e(
                        TAG,
                        "Rootfs extraction failed: ${result.message}",
                        result.cause
                    )

                    _state.value =
                        LinuxRootfsExtractionState
                            .Failed(
                                message =
                                    result.message
                            )

                    result
                }
            }
        }

    fun reset() {

        Log.i(
            TAG,
            "Rootfs extraction state reset."
        )

        _state.value =
            LinuxRootfsExtractionState
                .Idle
    }
}