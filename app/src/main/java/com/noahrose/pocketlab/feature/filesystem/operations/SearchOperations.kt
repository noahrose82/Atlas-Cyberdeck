package com.noahrose.pocketlab.feature.filesystem.operations

import com.noahrose.pocketlab.feature.filesystem.FileNode

object SearchOperations {

    fun find(
        root: FileNode,
        name: String
    ): List<String> {

        val target =
            name.trim()

        if (target.isBlank()) {
            return emptyList()
        }

        val results =
            mutableListOf<String>()

        searchDirectory(
            directory = root,
            currentPath = "~",
            target = target,
            results = results
        )

        return results
    }

    private fun searchDirectory(
        directory: FileNode,
        currentPath: String,
        target: String,
        results: MutableList<String>
    ) {

        directory.children.forEach { child ->

            val childPath =
                if (currentPath == "~") {
                    "~/${child.name}"
                } else {
                    "$currentPath/${child.name}"
                }

            if (
                child.name.equals(
                    target,
                    ignoreCase = true
                )
            ) {
                results.add(childPath)
            }

            if (child.isDirectory) {

                searchDirectory(
                    directory = child,
                    currentPath = childPath,
                    target = target,
                    results = results
                )
            }
        }
    }
}