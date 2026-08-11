package com.noahrose.pocketlab.feature.terminal.commands

import com.noahrose.pocketlab.feature.filesystem.VirtualFileSystem
import com.noahrose.pocketlab.feature.terminal.handler.CommandHandler
object FileCommands : CommandHandler {

    override fun handle(
        commandName: String,
        parts: List<String>,
        output: MutableList<String>
    ): Boolean {

        when (commandName) {

            "touch" -> {

                if (
                    parts.size < 2 ||
                    parts[1].isBlank()
                ) {
                    output.add(
                        "Usage: touch <filename>"
                    )

                    return true
                }

                val fileName =
                    parts[1].trim()

                val created =
                    VirtualFileSystem.createFile(
                        fileName
                    )

                if (created) {
                    output.add(
                        "File created: $fileName"
                    )
                } else {
                    output.add(
                        "touch: '$fileName' already exists"
                    )
                }

                return true
            }

            "cat" -> {

                if (
                    parts.size < 2 ||
                    parts[1].isBlank()
                ) {
                    output.add(
                        "Usage: cat <filename>"
                    )

                    return true
                }

                val fileNames =
                    parts[1]
                        .trim()
                        .split(
                            Regex("\\s+")
                        )

                fileNames.forEachIndexed { index, fileName ->

                    val content =
                        VirtualFileSystem.readFile(
                            fileName
                        )

                    if (content == null) {

                        output.add(
                            "cat: $fileName: No such file"
                        )

                    } else {

                        if (fileNames.size > 1) {
                            output.add(
                                "----- $fileName -----"
                            )
                        }

                        if (content.isEmpty()) {
                            output.add("<empty>")
                        } else {
                            output.add(content)
                        }

                        if (
                            fileNames.size > 1 &&
                            index != fileNames.lastIndex
                        ) {
                            output.add("")
                        }
                    }
                }

                return true
            }

            "echo" -> {

                val input =
                    if (parts.size < 2) {
                        ""
                    } else {
                        parts[1].trim()
                    }

                when {

                    input.isBlank() -> {
                        output.add("")
                    }

                    input.contains(">>") -> {

                        val pieces =
                            input.split(
                                ">>",
                                limit = 2
                            )

                        val text =
                            pieces[0].trim()

                        val fileName =
                            pieces[1].trim()

                        if (fileName.isBlank()) {

                            output.add(
                                "Usage: echo <text> >> <filename>"
                            )

                        } else {

                            val existingContent =
                                VirtualFileSystem.readFile(
                                    fileName
                                )

                            if (existingContent == null) {

                                output.add(
                                    "echo: $fileName: No such file"
                                )

                            } else {

                                val updatedContent =
                                    if (existingContent.isEmpty()) {
                                        text
                                    } else {
                                        "$existingContent\n$text"
                                    }

                                val written =
                                    VirtualFileSystem.writeFile(
                                        name = fileName,
                                        content = updatedContent
                                    )

                                if (written) {
                                    output.add(
                                        "Appended to $fileName"
                                    )
                                } else {
                                    output.add(
                                        "echo: $fileName: No such file"
                                    )
                                }
                            }
                        }
                    }

                    input.contains(">") -> {

                        val pieces =
                            input.split(
                                ">",
                                limit = 2
                            )

                        val text =
                            pieces[0].trim()

                        val fileName =
                            pieces[1].trim()

                        if (fileName.isBlank()) {

                            output.add(
                                "Usage: echo <text> > <filename>"
                            )

                        } else {

                            val written =
                                VirtualFileSystem.writeFile(
                                    name = fileName,
                                    content = text
                                )

                            if (written) {
                                output.add(
                                    "Wrote to $fileName"
                                )
                            } else {
                                output.add(
                                    "echo: $fileName: No such file"
                                )
                            }
                        }
                    }

                    else -> {
                        output.add(input)
                    }
                }

                return true
            }

            "rm" -> {

                if (
                    parts.size < 2 ||
                    parts[1].isBlank()
                ) {
                    output.add(
                        "Usage: rm <filename>"
                    )

                    return true
                }

                val fileNames =
                    parts[1]
                        .trim()
                        .split(
                            Regex("\\s+")
                        )

                var deletedCount = 0

                fileNames.forEach { fileName ->

                    val deleted =
                        VirtualFileSystem.deleteFile(
                            fileName
                        )

                    if (deleted) {
                        output.add(
                            "Deleted: $fileName"
                        )

                        deletedCount++
                    } else {
                        output.add(
                            "rm: $fileName: No such file"
                        )
                    }
                }

                if (deletedCount == 0) {
                    output.add(
                        "No files were deleted."
                    )
                }

                return true
            }

            "cp" -> {

                if (
                    parts.size < 2 ||
                    parts[1].isBlank()
                ) {
                    output.add(
                        "Usage: cp <source...> <destination>"
                    )

                    return true
                }

                val arguments =
                    parts[1]
                        .trim()
                        .split(
                            Regex("\\s+")
                        )

                if (arguments.size < 2) {

                    output.add(
                        "Usage: cp <source...> <destination>"
                    )

                    return true
                }

                val destination =
                    arguments.last()

                val sources =
                    arguments.dropLast(1)

                var copiedCount = 0

                sources.forEach { source ->

                    val copied =
                        VirtualFileSystem.copyFile(
                            sourceName = source,
                            destinationName = destination
                        )

                    if (copied) {
                        copiedCount++

                        output.add(
                            "Copied '$source' to '$destination'"
                        )
                    } else {
                        output.add(
                            "cp: failed to copy '$source'"
                        )
                    }
                }

                if (copiedCount == 0) {
                    output.add(
                        "cp: no files copied"
                    )
                }

                return true
            }

            "mv" -> {

                if (
                    parts.size < 2 ||
                    parts[1].isBlank()
                ) {
                    output.add(
                        "Usage: mv <source...> <destination>"
                    )

                    return true
                }

                val arguments =
                    parts[1]
                        .trim()
                        .split(
                            Regex("\\s+")
                        )

                if (arguments.size < 2) {

                    output.add(
                        "Usage: mv <source...> <destination>"
                    )

                    return true
                }

                val destination =
                    arguments.last()

                val sources =
                    arguments.dropLast(1)

                var movedCount = 0

                sources.forEach { source ->

                    val moved =
                        VirtualFileSystem.moveFile(
                            sourceName = source,
                            destinationName = destination
                        )

                    if (moved) {
                        movedCount++

                        output.add(
                            "Moved '$source' to '$destination'"
                        )
                    } else {
                        output.add(
                            "mv: failed to move '$source'"
                        )
                    }
                }

                if (movedCount == 0) {
                    output.add(
                        "mv: no files moved"
                    )
                }

                return true
            }

            "grep" -> {

                if (
                    parts.size < 2 ||
                    parts[1].isBlank()
                ) {
                    output.add(
                        "Usage: grep <text> <filename>"
                    )

                    return true
                }

                val arguments =
                    parts[1]
                        .trim()
                        .split(
                            Regex("\\s+"),
                            limit = 2
                        )

                if (arguments.size < 2) {

                    output.add(
                        "Usage: grep <text> <filename>"
                    )

                    return true
                }

                val searchText =
                    arguments[0]

                val fileName =
                    arguments[1]

                val content =
                    VirtualFileSystem.readFile(
                        fileName
                    )

                if (content == null) {

                    output.add(
                        "grep: $fileName: No such file"
                    )

                } else {

                    val matches =
                        content
                            .lines()
                            .filter {
                                it.contains(searchText)
                            }

                    if (matches.isEmpty()) {
                        output.add(
                            "No matches found."
                        )
                    } else {
                        matches.forEach { line ->
                            output.add(line)
                        }
                    }
                }

                return true
            }

            "head" -> {

                if (
                    parts.size < 2 ||
                    parts[1].isBlank()
                ) {
                    output.add(
                        "Usage: head <filename>"
                    )

                    return true
                }

                val fileName =
                    parts[1].trim()

                val content =
                    VirtualFileSystem.readFile(
                        fileName
                    )

                if (content == null) {

                    output.add(
                        "head: $fileName: No such file"
                    )

                } else {

                    content
                        .lines()
                        .take(3)
                        .forEach { line ->
                            output.add(line)
                        }
                }

                return true
            }

            "tail" -> {

                if (
                    parts.size < 2 ||
                    parts[1].isBlank()
                ) {
                    output.add(
                        "Usage: tail <filename>"
                    )

                    return true
                }

                val fileName =
                    parts[1].trim()

                val content =
                    VirtualFileSystem.readFile(
                        fileName
                    )

                if (content == null) {

                    output.add(
                        "tail: $fileName: No such file"
                    )

                } else {

                    content
                        .lines()
                        .takeLast(3)
                        .forEach { line ->
                            output.add(line)
                        }
                }

                return true
            }
        }

        return false
    }
}