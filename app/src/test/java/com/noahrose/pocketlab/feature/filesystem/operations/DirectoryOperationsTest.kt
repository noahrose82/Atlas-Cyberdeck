package com.noahrose.pocketlab.feature.filesystem.operations

import com.noahrose.pocketlab.feature.filesystem.FileNode
import org.junit.Assert.*
import org.junit.Test

class DirectoryOperationsTest {

    private fun createRoot(): FileNode {
        return FileNode(
            name = "~",
            isDirectory = true
        )
    }

    @Test
    fun `createDirectory creates new directory`() {

        val root = createRoot()

        val created =
            DirectoryOperations.createDirectory(
                currentDirectory = root,
                name = "Projects"
            )

        assertTrue(created)
        assertEquals(1, root.children.size)
        assertTrue(root.children.first().isDirectory)
        assertEquals("Projects", root.children.first().name)
    }

    @Test
    fun `createDirectory rejects duplicate directory`() {

        val root = createRoot()

        DirectoryOperations.createDirectory(
            currentDirectory = root,
            name = "Projects"
        )

        val created =
            DirectoryOperations.createDirectory(
                currentDirectory = root,
                name = "Projects"
            )

        assertFalse(created)
        assertEquals(1, root.children.size)
    }

    @Test
    fun `deleteDirectory removes empty directory`() {

        val root = createRoot()

        DirectoryOperations.createDirectory(
            currentDirectory = root,
            name = "Projects"
        )

        val deleted =
            DirectoryOperations.deleteDirectory(
                currentDirectory = root,
                name = "Projects"
            )

        assertTrue(deleted)
        assertTrue(root.children.isEmpty())
    }

    @Test
    fun `deleteDirectory returns false for missing directory`() {

        val root = createRoot()

        val deleted =
            DirectoryOperations.deleteDirectory(
                currentDirectory = root,
                name = "Missing"
            )

        assertFalse(deleted)
    }

    @Test
    fun `deleteDirectory refuses non empty directory`() {

        val root = createRoot()

        DirectoryOperations.createDirectory(
            currentDirectory = root,
            name = "Projects"
        )

        val directory =
            root.children.first()

        directory.children.add(
            FileNode(
                name = "notes.txt",
                isDirectory = false,
                parent = directory
            )
        )

        val deleted =
            DirectoryOperations.deleteDirectory(
                currentDirectory = root,
                name = "Projects"
            )

        assertFalse(deleted)
        assertEquals(1, root.children.size)
    }
}