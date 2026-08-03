package com.noahrose.pocketlab.feature.filesystem.operations

import com.noahrose.pocketlab.feature.filesystem.FileNode

object DirectoryOperations {

    fun createDirectory(
        currentDirectory: FileNode,
        name: String
    ): Boolean {

        val directoryName = name.trim()

        if (directoryName.isBlank()) {
            return false
        }

        val alreadyExists =
            currentDirectory.children.any {
                it.name.equals(
                    directoryName,
                    ignoreCase = true
                )
            }

        if (alreadyExists) {
            return false
        }

        currentDirectory.children.add(
            FileNode(
                name = directoryName,
                isDirectory = true,
                parent = currentDirectory
            )
        )

        return true
    }

    fun deleteDirectory(
        currentDirectory: FileNode,
        name: String
    ): Boolean {

        val directoryName = name.trim()

        val directory =
            currentDirectory.children.firstOrNull {

                it.name.equals(
                    directoryName,
                    ignoreCase = true
                ) &&
                        it.isDirectory
            }

        if (directory == null) {
            return false
        }

        if (directory.children.isNotEmpty()) {
            return false
        }

        currentDirectory.children.remove(directory)

        return true
    }

    fun changeDirectory(
        currentDirectory: FileNode,
        root: FileNode,
        name: String
    ): FileNode? {

        val destination =
            name.trim()

        if (destination == "~") {
            return root
        }

        if (destination == "..") {
            return currentDirectory.parent ?: currentDirectory
        }

        return currentDirectory.children.firstOrNull {
            it.name.equals(
                destination,
                ignoreCase = true
            ) &&
                    it.isDirectory
        }
    }
}