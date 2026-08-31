package com.noahrose.pocketlab.feature.filesystem.operations

import com.noahrose.pocketlab.feature.filesystem.FileNode

object SearchOperations {

    fun find(
        root: FileNode,
        name: String
    ): List<String> {

        val target =
            name.trim()

        /*
         * Atlas search requires at least
         * two characters.
         *
         * This keeps searches useful without
         * returning nearly the entire filesystem
         * for a single-letter query.
         */
        if (target.length < 2) {
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
            .sortedWith(
                String.CASE_INSENSITIVE_ORDER
            )
    }

    private fun searchDirectory(
        directory: FileNode,
        currentPath: String,
        target: String,
        results: MutableList<String>
    ) {

        directory.children
            .forEach { child ->

                val childPath =
                    if (currentPath == "~") {
                        "~/${child.name}"
                    } else {
                        "$currentPath/${child.name}"
                    }

                /*
                 * Partial-name search.
                 *
                 * Examples:
                 *
                 * "te" -> test.txt
                 * "pro" -> Projects
                 * "txt" -> notes.txt
                 *
                 * Matching is case-insensitive.
                 */
                if (
                    child.name.contains(
                        other = target,
                        ignoreCase = true
                    )
                ) {

                    results.add(
                        childPath
                    )
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