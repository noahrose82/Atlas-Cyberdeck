package com.noahrose.pocketlab.feature.filesystem.operations

import com.noahrose.pocketlab.feature.filesystem.FileNode

object FileOperations {

    fun createFile(
        currentDirectory: FileNode,
        name: String
    ): Boolean {

        val fileName = name.trim()

        if (fileName.isBlank()) {
            return false
        }

        val alreadyExists =
            currentDirectory.children.any {
                it.name.equals(
                    fileName,
                    ignoreCase = true
                )
            }

        if (alreadyExists) {
            return false
        }

        currentDirectory.children.add(
            FileNode(
                name = fileName,
                isDirectory = false,
                parent = currentDirectory
            )
        )

        return true
    }

    fun readFile(
        currentDirectory: FileNode,
        name: String
    ): String? {

        val fileName = name.trim()

        val file =
            currentDirectory.children.firstOrNull {
                it.name.equals(
                    fileName,
                    ignoreCase = true
                ) &&
                        !it.isDirectory
            }

        return file?.content
    }

    fun writeFile(
        currentDirectory: FileNode,
        name: String,
        content: String
    ): Boolean {

        val file =
            currentDirectory.children.firstOrNull {
                it.name.equals(
                    name.trim(),
                    ignoreCase = true
                ) &&
                        !it.isDirectory
            }

        if (file == null) {
            return false
        }

        file.content = content

        return true
    }

    fun deleteFile(
        currentDirectory: FileNode,
        name: String
    ): Boolean {

        val fileName = name.trim()

        val file =
            currentDirectory.children.firstOrNull {
                it.name.equals(
                    fileName,
                    ignoreCase = true
                ) &&
                        !it.isDirectory
            }

        if (file == null) {
            return false
        }

        currentDirectory.children.remove(file)

        return true
    }
}