package com.noahrose.pocketlab.feature.filesystem

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import com.noahrose.pocketlab.feature.filesystem.operations.FileOperations
import com.noahrose.pocketlab.feature.filesystem.operations.DirectoryOperations
object VirtualFileSystem {

    private val root = FileNode(
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

        refreshCurrentEntries()
    }

    fun createDirectory(name: String): Boolean {

        val created =
            DirectoryOperations.createDirectory(
                currentDirectory = _currentDirectory.value,
                name = name
            )

        if (created) {
            refreshCurrentEntries()
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

        return FileOperations.writeFile(
            currentDirectory = _currentDirectory.value,
            name = name,
            content = content
        )
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

        val output = mutableListOf<String>()

        output.add("${_currentDirectory.value.name}/")

        buildTreeLines(
            directory = _currentDirectory.value,
            prefix = "",
            output = output
        )

        return output
    }

    fun find(name: String): List<String> {

        val results = mutableListOf<String>()

        searchDirectory(
            directory = root,
            currentPath = "~",
            target = name.trim(),
            results = results
        )

        return results
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

        val currentDirectory =
            _currentDirectory.value

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

        refreshCurrentEntries()

        return true
    }

    fun moveFile(
        sourceName: String,
        destinationName: String
    ): Boolean {

        val currentDirectory =
            _currentDirectory.value

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

            source.parent = destinationDirectory

            destinationDirectory.children.add(source)

            refreshCurrentEntries()

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

        refreshCurrentEntries()

        return true
    }
}