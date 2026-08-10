package com.noahrose.pocketlab.feature.filesystem.operations

import com.noahrose.pocketlab.feature.filesystem.FileNode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SearchOperationsTest {

    private fun createRoot(): FileNode {
        return FileNode(
            name = "~",
            isDirectory = true
        )
    }

    @Test
    fun `find returns matching file path`() {

        val root = createRoot()

        val projects =
            FileNode(
                name = "Projects",
                isDirectory = true,
                parent = root
            )

        val file =
            FileNode(
                name = "notes.txt",
                isDirectory = false,
                parent = projects
            )

        projects.children.add(file)
        root.children.add(projects)

        val results =
            SearchOperations.find(
                root = root,
                name = "notes.txt"
            )

        assertEquals(
            listOf("~/Projects/notes.txt"),
            results
        )
    }

    @Test
    fun `find searches recursively`() {

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

        val results =
            SearchOperations.find(
                root = root,
                name = "report.txt"
            )

        assertEquals(
            listOf("~/Projects/Archive/report.txt"),
            results
        )
    }

    @Test
    fun `find is case insensitive`() {

        val root = createRoot()

        root.children.add(
            FileNode(
                name = "Atlas.TXT",
                isDirectory = false,
                parent = root
            )
        )

        val results =
            SearchOperations.find(
                root = root,
                name = "atlas.txt"
            )

        assertEquals(
            listOf("~/Atlas.TXT"),
            results
        )
    }

    @Test
    fun `find returns empty list when no match exists`() {

        val root = createRoot()

        val results =
            SearchOperations.find(
                root = root,
                name = "missing.txt"
            )

        assertTrue(results.isEmpty())
    }

    @Test
    fun `find returns empty list for blank search`() {

        val root = createRoot()

        val results =
            SearchOperations.find(
                root = root,
                name = "   "
            )

        assertTrue(results.isEmpty())
    }
}