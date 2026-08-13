package com.noahrose.pocketlab.feature.terminal.redirection

import com.noahrose.pocketlab.feature.filesystem.VirtualFileSystem

object RedirectionEngine {

    fun handle(
        command: String,
        commandOutput: List<String>
    ): Boolean {

        val request =
            RedirectionParser.parse(command)
                ?: return false

        return when (request.type) {

            RedirectionType.OVERWRITE -> {

                ensureFileExists(
                    request.target
                )

                val content =
                    commandOutput.joinToString("\n")

                VirtualFileSystem.writeFile(
                    name = request.target,
                    content = content
                )
            }

            RedirectionType.APPEND -> {

                ensureFileExists(
                    request.target
                )

                val existingContent =
                    VirtualFileSystem.readFile(
                        request.target
                    ) ?: ""

                val newContent =
                    commandOutput.joinToString("\n")

                val combinedContent =
                    when {

                        existingContent.isEmpty() ->
                            newContent

                        newContent.isEmpty() ->
                            existingContent

                        else ->
                            "$existingContent\n$newContent"
                    }

                VirtualFileSystem.writeFile(
                    name = request.target,
                    content = combinedContent
                )
            }

            RedirectionType.INPUT -> {

                val content =
                    VirtualFileSystem.readFile(
                        request.target
                    )

                if (content == null) {

                    false

                } else {

                    true
                }
            }
        }
    }

    private fun ensureFileExists(
        fileName: String
    ) {

        if (
            VirtualFileSystem.readFile(
                fileName
            ) == null
        ) {

            VirtualFileSystem.createFile(
                fileName
            )
        }
    }
}