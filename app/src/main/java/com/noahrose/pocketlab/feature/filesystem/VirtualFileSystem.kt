package com.noahrose.pocketlab.feature.filesystem

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import com.noahrose.pocketlab.feature.filesystem.operations.FileOperations
import com.noahrose.pocketlab.feature.filesystem.operations.DirectoryOperations
import com.noahrose.pocketlab.feature.filesystem.operations.CopyMoveOperations
import com.noahrose.pocketlab.feature.filesystem.operations.SearchOperations
import com.noahrose.pocketlab.feature.filesystem.operations.TreeOperations
import com.noahrose.pocketlab.feature.filesystem.persistence.PersistenceManager
object VirtualFileSystem {

    private val root =
        PersistenceManager.load()
            ?: FileNode(
                name = "~"
            )

    private val _currentDirectory =
        MutableStateFlow(root)

    val currentDirectory: StateFlow<FileNode> =
        _currentDirectory.asStateFlow()

    private val _currentPath =
        MutableStateFlow("~")

    val currentPath: StateFlow<String> =
        _currentPath.asStateFlow()

    private val _currentEntries =
        MutableStateFlow<List<FileNode>>(emptyList())

    val currentEntries: StateFlow<List<FileNode>> =
        _currentEntries.asStateFlow()

    init {

        if (root.children.isEmpty()) {

            root.children.addAll(
                listOf(
                    FileNode(
                        name = "Documents",
                        parent = root
                    ),
                    FileNode(
                        name = "Downloads",
                        parent = root
                    ),
                    FileNode(
                        name = "Projects",
                        parent = root
                    )
                )
            )
        }

        refreshCurrentEntries()
    }
    private fun saveFilesystem() {
        PersistenceManager.save(root)
    }
    fun createDirectory(name: String): Boolean {

        val created =
            DirectoryOperations.createDirectory(
                currentDirectory = _currentDirectory.value,
                name = name
            )

        if (created) {
            refreshCurrentEntries()
            saveFilesystem()
        }

        return created
    }

    fun changeDirectory(name: String): Boolean {

        val targetDirectory =
            DirectoryOperations.changeDirectory(
                currentDirectory = _currentDirectory.value,
                root = root,
                name = name
            ) ?: return false

        moveToDirectory(targetDirectory)

        return true
    }

    private fun moveToDirectory(directory: FileNode) {

        _currentDirectory.value = directory
        _currentPath.value = buildPath(directory)

        refreshCurrentEntries()
    }

    private fun refreshCurrentEntries() {

        _currentEntries.value =
            _currentDirectory.value.children.toList()
    }

    private fun buildPath(directory: FileNode): String {

        if (directory === root) {
            return "~"
        }

        val segments = mutableListOf<String>()

        var currentNode: FileNode? = directory

        while (
            currentNode != null &&
            currentNode !== root
        ) {
            segments.add(currentNode.name)
            currentNode = currentNode.parent
        }

        return "~/" +
                segments
                    .asReversed()
                    .joinToString("/")
    }

    fun createFile(name: String): Boolean {

        val created =
            FileOperations.createFile(
                currentDirectory = _currentDirectory.value,
                name = name
            )

        if (created) {
            refreshCurrentEntries()
        }

        return created
    }

    fun readFile(name: String): String? {

        return FileOperations.readFile(
            currentDirectory = _currentDirectory.value,
            name = name
        )
    }

    fun writeFile(
        name: String,
        content: String
    ): Boolean {

        val written =
            FileOperations.writeFile(
                currentDirectory = _currentDirectory.value,
                name = name,
                content = content
            )

        if (written) {
            saveFilesystem()
        }

        return written
    }

    fun deleteFile(name: String): Boolean {

        val deleted =
            FileOperations.deleteFile(
                currentDirectory = _currentDirectory.value,
                name = name
            )

        if (deleted) {
            refreshCurrentEntries()
        }

        return deleted
    }

    fun deleteDirectory(name: String): Boolean {

        val deleted =
            DirectoryOperations.deleteDirectory(
                currentDirectory = _currentDirectory.value,
                name = name
            )

        if (deleted) {
            refreshCurrentEntries()
        }

        return deleted
    }

    fun buildTree(): List<String> {

        return TreeOperations.buildTree(
            directory = _currentDirectory.value
        )
    }

    fun find(name: String): List<String> {

        return SearchOperations.find(
            root = root,
            name = name
        )
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

    fun copyFile(
        sourceName: String,
        destinationName: String
    ): Boolean {

        val copied =
            CopyMoveOperations.copyFile(
                currentDirectory = _currentDirectory.value,
                sourceName = sourceName,
                destinationName = destinationName
            )

        if (copied) {
            refreshCurrentEntries()
        }

        return copied
    }

    fun moveFile(
        sourceName: String,
        destinationName: String
    ): Boolean {

        val moved =
            CopyMoveOperations.moveFile(
                currentDirectory = _currentDirectory.value,
                sourceName = sourceName,
                destinationName = destinationName
            )

        if (moved) {
            refreshCurrentEntries()
        }

        return moved
    }
}