package com.noahrose.pocketlab.feature.filesystem.operations

import com.noahrose.pocketlab.feature.filesystem.FileNode

object CopyMoveOperations {

    fun copyFile(
        currentDirectory: FileNode,
        sourceName: String,
        destinationName: String
    ): Boolean {

        val source =
            currentDirectory.children.firstOrNull {
                it.name.equals(
                    sourceName.trim(),
                    ignoreCase = true
                ) && !it.isDirectory
            } ?: return false

        val cleanDestination =
            destinationName
                .trim()
                .removeSuffix("/")

        val destinationDirectory =
            currentDirectory.children.firstOrNull {
                it.name.equals(
                    cleanDestination,
                    ignoreCase = true
                ) && it.isDirectory
            }

        if (destinationDirectory != null) {

            val alreadyExists =
                destinationDirectory.children.any {
                    it.name.equals(
                        source.name,
                        ignoreCase = true
                    )
                }

            if (alreadyExists) {
                return false
            }

            destinationDirectory.children.add(
                FileNode(
                    name = source.name,
                    isDirectory = false,
                    content = source.content,
                    parent = destinationDirectory
                )
            )

            return true
        }

        val alreadyExists =
            currentDirectory.children.any {
                it.name.equals(
                    cleanDestination,
                    ignoreCase = true
                )
            }

        if (alreadyExists) {
            return false
        }

        currentDirectory.children.add(
            FileNode(
                name = cleanDestination,
                isDirectory = false,
                content = source.content,
                parent = currentDirectory
            )
        )

        return true
    }

    fun moveFile(
        currentDirectory: FileNode,
        sourceName: String,
        destinationName: String
    ): Boolean {

        val source =
            currentDirectory.children.firstOrNull {
                it.name.equals(
                    sourceName.trim(),
                    ignoreCase = true
                ) && !it.isDirectory
            } ?: return false

        val cleanDestination =
            destinationName
                .trim()
                .removeSuffix("/")

        val destinationDirectory =
            currentDirectory.children.firstOrNull {
                it.name.equals(
                    cleanDestination,
                    ignoreCase = true
                ) && it.isDirectory
            }

        if (destinationDirectory != null) {

            val alreadyExists =
                destinationDirectory.children.any {
                    it.name.equals(
                        source.name,
                        ignoreCase = true
                    )
                }

            if (alreadyExists) {
                return false
            }

            currentDirectory.children.remove(source)

            source.parent =
                destinationDirectory

            destinationDirectory.children.add(source)

            return true
        }

        val alreadyExists =
            currentDirectory.children.any {
                it.name.equals(
                    cleanDestination,
                    ignoreCase = true
                )
            }

        if (alreadyExists) {
            return false
        }

        source.name =
            cleanDestination

        return true
    }
}