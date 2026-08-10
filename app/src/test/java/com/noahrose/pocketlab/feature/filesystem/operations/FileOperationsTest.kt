package com.noahrose.pocketlab.feature.filesystem.operations

import com.noahrose.pocketlab.feature.filesystem.FileNode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class FileOperationsTest {

    private fun createRoot(): FileNode {
        return FileNode(
            name = "~",
            isDirectory = true
        )
    }

    @Test
    fun `createFile creates new file`() {

        val root = createRoot()

        val created =
            FileOperations.createFile(
                currentDirectory = root,
                name = "notes.txt"
            )

        assertTrue(created)
        assertEquals(1, root.children.size)
        assertEquals(
            "notes.txt",
            root.children.first().name
        )
        assertFalse(
            root.children.first().isDirectory
        )
        assertEquals(
            root,
            root.children.first().parent
        )
    }

    @Test
    fun `createFile rejects duplicate name`() {

        val root = createRoot()

        FileOperations.createFile(
            currentDirectory = root,
            name = "notes.txt"
        )

        val createdAgain =
            FileOperations.createFile(
                currentDirectory = root,
                name = "notes.txt"
            )

        assertFalse(createdAgain)
        assertEquals(1, root.children.size)
    }

    @Test
    fun `writeFile updates file content`() {

        val root = createRoot()

        FileOperations.createFile(
            currentDirectory = root,
            name = "notes.txt"
        )

        val written =
            FileOperations.writeFile(
                currentDirectory = root,
                name = "notes.txt",
                content = "Atlas Cyberdeck"
            )

        assertTrue(written)

        val content =
            FileOperations.readFile(
                currentDirectory = root,
                name = "notes.txt"
            )

        assertEquals(
            "Atlas Cyberdeck",
            content
        )
    }

    @Test
    fun `readFile returns null when file does not exist`() {

        val root = createRoot()

        val content =
            FileOperations.readFile(
                currentDirectory = root,
                name = "missing.txt"
            )

        assertNull(content)
    }

    @Test
    fun `deleteFile removes existing file`() {

        val root = createRoot()

        FileOperations.createFile(
            currentDirectory = root,
            name = "notes.txt"
        )

        val deleted =
            FileOperations.deleteFile(
                currentDirectory = root,
                name = "notes.txt"
            )

        assertTrue(deleted)
        assertTrue(root.children.isEmpty())
    }

    @Test
    fun `deleteFile returns false when file does not exist`() {

        val root = createRoot()

        val deleted =
            FileOperations.deleteFile(
                currentDirectory = root,
                name = "missing.txt"
            )

        assertFalse(deleted)
    }

    @Test
    fun `file operations are case insensitive`() {

        val root = createRoot()

        FileOperations.createFile(
            currentDirectory = root,
            name = "Notes.TXT"
        )

        val content =
            FileOperations.readFile(
                currentDirectory = root,
                name = "notes.txt"
            )

        assertEquals("", content)
    }
}