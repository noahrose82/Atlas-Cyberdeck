package com.noahrose.pocketlab.feature.filesystem.operations

import com.noahrose.pocketlab.feature.filesystem.FileNode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TreeOperationsTest {

    private fun createRoot(): FileNode {
        return FileNode(
            name = "~",
            isDirectory = true
        )
    }

    @Test
    fun `buildTree returns root for empty directory`() {

        val root = createRoot()

        val result =
            TreeOperations.buildTree(
                directory = root
            )

        assertEquals(
            listOf("~/"),
            result
        )
    }

    @Test
    fun `buildTree includes files and directories`() {

        val root = createRoot()

        root.children.add(
            FileNode(
                name = "Documents",
                isDirectory = true,
                parent = root
            )
        )

        root.children.add(
            FileNode(
                name = "notes.txt",
                isDirectory = false,
                parent = root
            )
        )

        val result =
            TreeOperations.buildTree(
                directory = root
            )

        assertEquals("~/", result[0])
        assertTrue(
            result.any {
                it.contains("Documents/")
            }
        )
        assertTrue(
            result.any {
                it.contains("notes.txt")
            }
        )
    }

    @Test
    fun `buildTree renders nested directories recursively`() {

        val root = createRoot()

        val projects =
            FileNode(
                name = "Projects",
                isDirectory = true,
                parent = root
            )

        val archive =
            FileNode(
                name = "Archive",
                isDirectory = true,
                parent = projects
            )

        val file =
            FileNode(
                name = "report.txt",
                isDirectory = false,
                parent = archive
            )

        archive.children.add(file)
        projects.children.add(archive)
        root.children.add(projects)

        val result =
            TreeOperations.buildTree(
                directory = root
            )

        assertTrue(
            result.any {
                it.contains("Projects/")
            }
        )

        assertTrue(
            result.any {
                it.contains("Archive/")
            }
        )

        assertTrue(
            result.any {
                it.contains("report.txt")
            }
        )
    }
}