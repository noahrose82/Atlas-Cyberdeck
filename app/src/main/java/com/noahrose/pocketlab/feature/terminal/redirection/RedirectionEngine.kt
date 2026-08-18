package com.noahrose.pocketlab.feature.terminal.redirection

import com.noahrose.pocketlab.feature.filesystem.VirtualFileSystem

object RedirectionEngine {

    fun handle(
        command: String,
        commandOutput: List<String>
    ): Boolean {

        val request =
            RedirectionParser.parse(
                command
            ) ?: return false

        val newContent =
            commandOutput.joinToString(
                separator = "\n"
            )

        return when (request.type) {

            RedirectionType.OVERWRITE -> {

                if (
                    !ensureFileExists(
                        request.target
                    )
                ) {
                    return false
                }

                VirtualFileSystem.writeFile(
                    name = request.target,
                    content = newContent
                )
            }

            RedirectionType.APPEND -> {

                if (
                    !ensureFileExists(
                        request.target
                    )
                ) {
                    return false
                }

                val existingContent =
                    VirtualFileSystem.readFile(
                        request.target
                    ) ?: return false

                val combinedContent =
                    when {

                        existingContent.isEmpty() ->
                            newContent

                        newContent.isEmpty() ->
                            existingContent

                        else ->
                            buildString {

                                append(
                                    existingContent
                                )

                                append("\n")

                                append(
                                    newContent
                                )
                            }
                    }

                VirtualFileSystem.writeFile(
                    name = request.target,
                    content = combinedContent
                )
            }

            RedirectionType.INPUT -> {

                VirtualFileSystem.readFile(
                    request.target
                ) != null
            }
        }
    }

    private fun ensureFileExists(
        fileName: String
    ): Boolean {

        if (
            VirtualFileSystem.readFile(
                fileName
            ) != null
        ) {
            return true
        }

        return VirtualFileSystem.createFile(
            fileName
        )
    }
}