package com.noahrose.pocketlab.feature.linux.runtime.activity

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class LinuxRuntimeActivityLevel {

    INFO,

    SUCCESS,

    WARNING,

    ERROR
}

data class LinuxRuntimeActivityEntry(

    val timestampEpochMillis: Long,

    val message: String,

    val level: LinuxRuntimeActivityLevel
)

object LinuxRuntimeActivityReporter {

    private const val MAX_ENTRIES =
        100

    private val mutableEntries =
        MutableStateFlow<List<LinuxRuntimeActivityEntry>>(
            emptyList()
        )

    val entries:
            StateFlow<List<LinuxRuntimeActivityEntry>> =
        mutableEntries
            .asStateFlow()

    @Synchronized
    fun info(
        message: String
    ) {

        report(
            message =
                message,

            level =
                LinuxRuntimeActivityLevel.INFO
        )
    }

    @Synchronized
    fun success(
        message: String
    ) {

        report(
            message =
                message,

            level =
                LinuxRuntimeActivityLevel.SUCCESS
        )
    }

    @Synchronized
    fun warning(
        message: String
    ) {

        report(
            message =
                message,

            level =
                LinuxRuntimeActivityLevel.WARNING
        )
    }

    @Synchronized
    fun error(
        message: String
    ) {

        report(
            message =
                message,

            level =
                LinuxRuntimeActivityLevel.ERROR
        )
    }

    @Synchronized
    fun clear() {

        mutableEntries.value =
            emptyList()
    }

    private fun report(
        message: String,
        level: LinuxRuntimeActivityLevel
    ) {

        val trimmedMessage =
            message.trim()

        if (
            trimmedMessage.isBlank()
        ) {
            return
        }

        val entry =
            LinuxRuntimeActivityEntry(
                timestampEpochMillis =
                    System.currentTimeMillis(),

                message =
                    trimmedMessage,

                level =
                    level
            )

        mutableEntries.value =
            (
                    mutableEntries.value +
                            entry
                    )
                .takeLast(
                    MAX_ENTRIES
                )
    }
}