package com.noahrose.pocketlab.feature.filesystem.transfer

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns

object AtlasFileTransfer {

    data class ImportedTextFile(
        val name: String,
        val content: String
    )

    fun importTextFile(
        context: Context,
        uri: Uri
    ): ImportedTextFile? {

        val fileName =
            resolveFileName(
                context = context,
                uri = uri
            ) ?: return null

        val content =
            try {

                context
                    .contentResolver
                    .openInputStream(
                        uri
                    )
                    ?.bufferedReader()
                    ?.use {
                        it.readText()
                    }

            } catch (_: Exception) {

                null
            }
                ?: return null

        return ImportedTextFile(
            name = fileName,
            content = content
        )
    }

    fun exportTextFile(
        context: Context,
        uri: Uri,
        content: String
    ): Boolean {

        return try {

            val outputStream =
                context
                    .contentResolver
                    .openOutputStream(
                        uri,
                        "wt"
                    )
                    ?: return false

            outputStream
                .bufferedWriter()
                .use { writer ->

                    writer.write(
                        content
                    )
                }

            true

        } catch (_: Exception) {

            false
        }
    }

    private fun resolveFileName(
        context: Context,
        uri: Uri
    ): String? {

        var fileName: String? =
            null

        try {

            context
                .contentResolver
                .query(
                    uri,
                    arrayOf(
                        OpenableColumns.DISPLAY_NAME
                    ),
                    null,
                    null,
                    null
                )
                ?.use { cursor ->

                    val nameIndex =
                        cursor.getColumnIndex(
                            OpenableColumns.DISPLAY_NAME
                        )

                    if (
                        nameIndex >= 0 &&
                        cursor.moveToFirst()
                    ) {

                        fileName =
                            cursor.getString(
                                nameIndex
                            )
                    }
                }

        } catch (_: Exception) {

            fileName =
                null
        }

        if (
            fileName
                .isNullOrBlank()
        ) {

            fileName =
                uri
                    .lastPathSegment
                    ?.substringAfterLast("/")
        }

        return fileName
            ?.trim()
            ?.takeIf {
                it.isNotBlank()
            }
    }
}
