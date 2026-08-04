package com.noahrose.pocketlab.feature.filesystem.operations

import com.noahrose.pocketlab.feature.filesystem.FileNode

object TreeOperations {

    fun buildTree(
        directory: FileNode
    ): List<String> {

        val output =
            mutableListOf<String>()

        output.add("${directory.name}/")

        buildTreeLines(
            directory = directory,
            prefix = "",
            output = output
        )

        return output
    }

    private fun buildTreeLines(
        directory: FileNode,
        prefix: String,
        output: MutableList<String>
    ) {

        directory.children.forEachIndexed { index, child ->

            val isLast =
                index == directory.children.lastIndex

            val branch =
                if (isLast) {
                    "└── "
                } else {
                    "├── "
                }

            val name =
                if (child.isDirectory) {
                    "${child.name}/"
                } else {
                    child.name
                }

            output.add(
                prefix + branch + name
            )

            if (child.isDirectory) {

                val childPrefix =
                    prefix +
                            if (isLast) {
                                "    "
                            } else {
                                "│   "
                            }

                buildTreeLines(
                    directory = child,
                    prefix = childPrefix,
                    output = output
                )
            }
        }
    }
}