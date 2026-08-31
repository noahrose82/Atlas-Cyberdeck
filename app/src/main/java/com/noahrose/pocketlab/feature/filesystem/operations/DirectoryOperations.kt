package com.noahrose.pocketlab.feature.filesystem.operations

import com.noahrose.pocketlab.feature.filesystem.FileNode

object DirectoryOperations {

    /*
     * ------------------------------------------------
     * CREATE DIRECTORY
     * ------------------------------------------------
     */
    fun createDirectory(
        currentDirectory: FileNode,
        name: String
    ): Boolean {

        val directoryName =
            name.trim()

        if (
            directoryName.isBlank()
        ) {
            return false
        }

        val alreadyExists =
            currentDirectory
                .children
                .any { child ->

                    child.name.equals(
                        directoryName,
                        ignoreCase = true
                    )
                }

        if (
            alreadyExists
        ) {
            return false
        }

        currentDirectory
            .children
            .add(
                FileNode(
                    name =
                        directoryName,

                    isDirectory =
                        true,

                    parent =
                        currentDirectory
                )
            )

        return true
    }

    /*
     * ------------------------------------------------
     * DELETE DIRECTORY
     * ------------------------------------------------
     *
     * Atlas currently permits safe directory deletion
     * only when the directory is empty.
     */
    fun deleteDirectory(
        currentDirectory: FileNode,
        name: String
    ): Boolean {

        val directoryName =
            name.trim()

        val directory =
            currentDirectory
                .children
                .firstOrNull { child ->

                    child.isDirectory &&
                            child.name.equals(
                                directoryName,
                                ignoreCase = true
                            )
                }
                ?: return false

        if (
            directory.children.isNotEmpty()
        ) {
            return false
        }

        return currentDirectory
            .children
            .remove(
                directory
            )
    }

    /*
     * ------------------------------------------------
     * CHANGE DIRECTORY
     * ------------------------------------------------
     */
    fun changeDirectory(
        currentDirectory: FileNode,
        root: FileNode,
        name: String
    ): FileNode? {

        val destination =
            name.trim()

        if (
            destination == "~"
        ) {
            return root
        }

        if (
            destination == ".."
        ) {
            return currentDirectory.parent
                ?: currentDirectory
        }

        return currentDirectory
            .children
            .firstOrNull { child ->

                child.isDirectory &&
                        child.name.equals(
                            destination,
                            ignoreCase = true
                        )
            }
    }

    /*
     * ------------------------------------------------
     * RENAME DIRECTORY
     * ------------------------------------------------
     *
     * Renaming occurs only inside the current parent
     * directory.
     */
    fun renameDirectory(
        currentDirectory: FileNode,
        sourceName: String,
        destinationName: String
    ): Boolean {

        val cleanSourceName =
            sourceName.trim()

        val cleanDestinationName =
            destinationName.trim()

        if (
            cleanSourceName.isBlank() ||
            cleanDestinationName.isBlank()
        ) {
            return false
        }

        val directory =
            currentDirectory
                .children
                .firstOrNull { child ->

                    child.isDirectory &&
                            child.name.equals(
                                cleanSourceName,
                                ignoreCase = true
                            )
                }
                ?: return false

        /*
         * No actual rename requested.
         */
        if (
            directory.name ==
            cleanDestinationName
        ) {
            return false
        }

        /*
         * Prevent duplicate names inside the same parent.
         */
        val alreadyExists =
            currentDirectory
                .children
                .any { child ->

                    child !== directory &&
                            child.name.equals(
                                cleanDestinationName,
                                ignoreCase = true
                            )
                }

        if (
            alreadyExists
        ) {
            return false
        }

        directory.name =
            cleanDestinationName

        return true
    }

    /*
     * ------------------------------------------------
     * MOVE DIRECTORY
     * ------------------------------------------------
     *
     * Moves a child directory from currentDirectory into
     * destinationDirectory.
     *
     * Safety rules:
     *
     * - cannot move into its current parent
     * - cannot move into itself
     * - cannot move into one of its descendants
     * - cannot overwrite an existing child
     */
    fun moveDirectory(
        currentDirectory: FileNode,
        sourceName: String,
        destinationDirectory: FileNode
    ): Boolean {

        val cleanSourceName =
            sourceName.trim()

        if (
            cleanSourceName.isBlank()
        ) {
            return false
        }

        if (
            !destinationDirectory.isDirectory
        ) {
            return false
        }

        val sourceDirectory =
            currentDirectory
                .children
                .firstOrNull { child ->

                    child.isDirectory &&
                            child.name.equals(
                                cleanSourceName,
                                ignoreCase = true
                            )
                }
                ?: return false

        /*
         * Moving to the current parent changes nothing.
         */
        if (
            destinationDirectory ===
            currentDirectory
        ) {
            return false
        }

        /*
         * A directory cannot contain itself.
         */
        if (
            destinationDirectory ===
            sourceDirectory
        ) {
            return false
        }

        /*
         * This is the important cycle protection.
         *
         * Example:
         *
         * ~/Projects
         *     └── Atlas
         *
         * Projects cannot be moved inside Atlas because
         * Atlas already lives inside Projects.
         */
        if (
            isDescendantOf(
                candidate =
                    destinationDirectory,

                ancestor =
                    sourceDirectory
            )
        ) {
            return false
        }

        /*
         * Prevent overwriting an item already present in
         * the destination.
         */
        val alreadyExists =
            destinationDirectory
                .children
                .any { child ->

                    child.name.equals(
                        sourceDirectory.name,
                        ignoreCase = true
                    )
                }

        if (
            alreadyExists
        ) {
            return false
        }

        val removed =
            currentDirectory
                .children
                .remove(
                    sourceDirectory
                )

        if (
            !removed
        ) {
            return false
        }

        sourceDirectory.parent =
            destinationDirectory

        destinationDirectory
            .children
            .add(
                sourceDirectory
            )

        return true
    }

    /*
     * ------------------------------------------------
     * DESCENDANT CHECK
     * ------------------------------------------------
     *
     * Walk upward from candidate until root.
     *
     * If ancestor is encountered, candidate sits somewhere
     * underneath ancestor and cannot be used as the move
     * destination.
     */
    private fun isDescendantOf(
        candidate: FileNode,
        ancestor: FileNode
    ): Boolean {

        var currentNode: FileNode? =
            candidate.parent

        while (
            currentNode != null
        ) {

            if (
                currentNode ===
                ancestor
            ) {
                return true
            }

            currentNode =
                currentNode.parent
        }

        return false
    }
}