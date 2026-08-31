package com.noahrose.pocketlab.feature.filesystem

import com.noahrose.pocketlab.feature.filesystem.operations.CopyMoveOperations
import com.noahrose.pocketlab.feature.filesystem.operations.DirectoryOperations
import com.noahrose.pocketlab.feature.filesystem.operations.FileOperations
import com.noahrose.pocketlab.feature.filesystem.operations.SearchOperations
import com.noahrose.pocketlab.feature.filesystem.operations.TreeOperations
import com.noahrose.pocketlab.feature.filesystem.persistence.PersistenceManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object VirtualFileSystem {

    private val root =
        PersistenceManager.load()
            ?: FileNode(
                name = "~"
            )

    private val _currentDirectory =
        MutableStateFlow(root)

    private val _currentPath =
        MutableStateFlow("~")

    val currentPath: StateFlow<String> =
        _currentPath.asStateFlow()

    private val _currentEntries =
        MutableStateFlow<List<FileNode>>(
            emptyList()
        )

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

            saveFilesystem()
        }

        refreshCurrentEntries()
    }

    private fun saveFilesystem() {

        PersistenceManager.save(root)
    }

    fun createDirectory(
        name: String
    ): Boolean {

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

    fun changeDirectory(
        name: String
    ): Boolean {

        val targetDirectory =
            DirectoryOperations.changeDirectory(
                currentDirectory = _currentDirectory.value,
                root = root,
                name = name
            ) ?: return false

        moveToDirectory(
            targetDirectory
        )

        return true
    }

    private fun moveToDirectory(
        directory: FileNode
    ) {

        _currentDirectory.value =
            directory

        _currentPath.value =
            buildPath(
                directory
            )

        refreshCurrentEntries()
    }

    private fun refreshCurrentEntries() {

        _currentEntries.value =
            _currentDirectory.value
                .children
                .toList()
    }

    private fun buildPath(
        directory: FileNode
    ): String {

        if (directory === root) {
            return "~"
        }

        val segments =
            mutableListOf<String>()

        var currentNode: FileNode? =
            directory

        while (
            currentNode != null &&
            currentNode !== root
        ) {

            segments.add(
                currentNode.name
            )

            currentNode =
                currentNode.parent
        }

        return "~/" +
                segments
                    .asReversed()
                    .joinToString("/")
    }

    fun getDirectoryPaths(): List<String> {

        val paths =
            mutableListOf<String>()

        collectDirectoryPaths(
            directory = root,
            paths = paths
        )

        return paths
            .sortedWith(
                compareBy<String> {
                    if (it == "~") {
                        0
                    } else {
                        1
                    }
                }.thenBy {
                    it.lowercase()
                }
            )
    }

    private fun collectDirectoryPaths(
        directory: FileNode,
        paths: MutableList<String>
    ) {

        paths.add(
            buildPath(
                directory
            )
        )

        directory.children
            .filter {
                it.isDirectory
            }
            .forEach { child ->

                collectDirectoryPaths(
                    directory = child,
                    paths = paths
                )
            }
    }

    fun createFile(
        name: String
    ): Boolean {

        val created =
            FileOperations.createFile(
                currentDirectory = _currentDirectory.value,
                name = name
            )

        if (created) {
            refreshCurrentEntries()
            saveFilesystem()
        }

        return created
    }

    fun readFile(
        name: String
    ): String? {

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

    fun deleteFile(
        name: String
    ): Boolean {

        val deleted =
            FileOperations.deleteFile(
                currentDirectory = _currentDirectory.value,
                name = name
            )

        if (deleted) {
            refreshCurrentEntries()
            saveFilesystem()
        }

        return deleted
    }

    fun deleteDirectory(
        name: String
    ): Boolean {

        val deleted =
            DirectoryOperations.deleteDirectory(
                currentDirectory = _currentDirectory.value,
                name = name
            )

        if (deleted) {
            refreshCurrentEntries()
            saveFilesystem()
        }

        return deleted
    }

    fun renameDirectory(
        sourceName: String,
        destinationName: String
    ): Boolean {

        val renamed =
            DirectoryOperations.renameDirectory(
                currentDirectory = _currentDirectory.value,
                sourceName = sourceName,
                destinationName = destinationName
            )

        if (renamed) {
            refreshCurrentEntries()
            saveFilesystem()
        }

        return renamed
    }

    fun moveDirectoryToDirectory(
        sourceName: String,
        destinationPath: String
    ): Boolean {

        val destinationDirectory =
            resolveDirectory(
                destinationPath
            ) ?: return false

        val moved =
            DirectoryOperations.moveDirectory(
                currentDirectory = _currentDirectory.value,
                sourceName = sourceName,
                destinationDirectory = destinationDirectory
            )

        if (moved) {
            refreshCurrentEntries()
            saveFilesystem()
        }

        return moved
    }

    fun buildTree(): List<String> {

        return TreeOperations.buildTree(
            directory = _currentDirectory.value
        )
    }

    fun find(
        name: String
    ): List<String> {

        return SearchOperations.find(
            root = root,
            name = name
        )
    }

    /*
     * Existing Atlas terminal copy.
     */
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
            saveFilesystem()
        }

        return copied
    }

    /*
     * Existing Atlas terminal move / rename.
     */
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
            saveFilesystem()
        }

        return moved
    }

    fun copyFileToDirectory(
        sourceName: String,
        destinationPath: String
    ): Boolean {

        val source =
            findFileInCurrentDirectory(
                sourceName
            ) ?: return false

        val destinationDirectory =
            resolveDirectory(
                destinationPath
            ) ?: return false

        val alreadyExists =
            destinationDirectory.children.any { child ->

                child.name.equals(
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

        refreshCurrentEntries()
        saveFilesystem()

        return true
    }

    fun moveFileToDirectory(
        sourceName: String,
        destinationPath: String
    ): Boolean {

        val sourceDirectory =
            _currentDirectory.value

        val source =
            findFileInCurrentDirectory(
                sourceName
            ) ?: return false

        val destinationDirectory =
            resolveDirectory(
                destinationPath
            ) ?: return false

        if (
            destinationDirectory ===
            sourceDirectory
        ) {
            return false
        }

        val alreadyExists =
            destinationDirectory.children.any { child ->

                child.name.equals(
                    source.name,
                    ignoreCase = true
                )
            }

        if (alreadyExists) {
            return false
        }

        val removed =
            sourceDirectory.children.remove(
                source
            )

        if (!removed) {
            return false
        }

        source.parent =
            destinationDirectory

        destinationDirectory.children.add(
            source
        )

        refreshCurrentEntries()
        saveFilesystem()

        return true
    }

    /*
     * Lets the GUI explain duplicate-name failures.
     */
    fun entryExistsInDirectory(
        destinationPath: String,
        entryName: String
    ): Boolean {

        val destinationDirectory =
            resolveDirectory(
                destinationPath
            ) ?: return false

        val cleanName =
            entryName.trim()

        if (cleanName.isBlank()) {
            return false
        }

        return destinationDirectory.children.any { child ->

            child.name.equals(
                cleanName,
                ignoreCase = true
            )
        }
    }

    private fun findFileInCurrentDirectory(
        name: String
    ): FileNode? {

        val cleanName =
            name.trim()

        if (cleanName.isBlank()) {
            return null
        }

        return _currentDirectory.value
            .children
            .firstOrNull { child ->

                !child.isDirectory &&
                        child.name.equals(
                            cleanName,
                            ignoreCase = true
                        )
            }
    }

    private fun resolveDirectory(
        path: String
    ): FileNode? {

        val normalizedPath =
            path
                .trim()
                .replace(
                    '\\',
                    '/'
                )
                .replace(
                    Regex("/+"),
                    "/"
                )
                .removeSuffix("/")

        if (
            normalizedPath.isBlank() ||
            normalizedPath == "~"
        ) {
            return root
        }

        if (
            !normalizedPath.startsWith("~/")
        ) {
            return null
        }

        val segments =
            normalizedPath
                .removePrefix("~/")
                .split("/")
                .filter {
                    it.isNotBlank()
                }

        var directory =
            root

        segments.forEach { segment ->

            val nextDirectory =
                directory.children
                    .firstOrNull { child ->

                        child.isDirectory &&
                                child.name.equals(
                                    segment,
                                    ignoreCase = true
                                )
                    }
                    ?: return null

            directory =
                nextDirectory
        }

        return directory
    }
}