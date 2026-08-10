package com.noahrose.pocketlab.feature.filesystem.operations

import com.noahrose.pocketlab.feature.filesystem.FileNode
import org.junit.Assert.*
import org.junit.Test

class CopyMoveOperationsTest {

    private fun createRoot(): FileNode {
        return FileNode(
            name = "~",
            isDirectory = true
        )
    }

    @Test
    fun `copyFile creates duplicate file`() {

        val root = createRoot()

        FileOperations.createFile(
            currentDirectory = root,
            name = "notes.txt"
        )

        FileOperations.writeFile(
            currentDirectory = root,
            name = "notes.txt",
            content = "Atlas"
        )

        val copied =
            CopyMoveOperations.copyFile(
                currentDirectory = root,
                sourceName = "notes.txt",
                destinationName = "copy.txt"
            )

        assertTrue(copied)
        assertEquals(2, root.children.size)

        val copy =
            root.children.first {
                it.name == "copy.txt"
            }

        assertEquals("Atlas", copy.content)
    }

    @Test
    fun `moveFile renames file`() {

        val root = createRoot()

        FileOperations.createFile(
            currentDirectory = root,
            name = "notes.txt"
        )

        val moved =
            CopyMoveOperations.moveFile(
                currentDirectory = root,
                sourceName = "notes.txt",
                destinationName = "atlas.txt"
            )

        assertTrue(moved)

        assertEquals(
            "atlas.txt",
            root.children.first().name
        )
    }

    @Test
    fun `copyFile fails when source missing`() {

        val root = createRoot()

        val copied =
            CopyMoveOperations.copyFile(
                currentDirectory = root,
                sourceName = "missing.txt",
                destinationName = "copy.txt"
            )

        assertFalse(copied)
    }

    @Test
    fun `moveFile fails when source missing`() {

        val root = createRoot()

        val moved =
            CopyMoveOperations.moveFile(
                currentDirectory = root,
                sourceName = "missing.txt",
                destinationName = "atlas.txt"
            )

        assertFalse(moved)
    }

    @Test
    fun `copyFile rejects duplicate destination`() {

        val root = createRoot()

        FileOperations.createFile(
            currentDirectory = root,
            name = "notes.txt"
        )

        FileOperations.createFile(
            currentDirectory = root,
            name = "copy.txt"
        )

        val copied =
            CopyMoveOperations.copyFile(
                currentDirectory = root,
                sourceName = "notes.txt",
                destinationName = "copy.txt"
            )

        assertFalse(copied)
    }
}