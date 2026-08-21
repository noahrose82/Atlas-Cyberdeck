package com.noahrose.pocketlab.feature.terminal.alias

import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CommandAliasesTest {

    @After
    fun cleanUp() {

        CommandAliases.clearUserAliases()
    }

    @Test
    fun resolve_resolvesBuiltInAlias() {

        assertEquals(
            "ls",
            CommandAliases.resolve(
                "ll"
            )
        )
    }

    @Test
    fun resolve_preservesArguments() {

        assertEquals(
            "ls Projects",
            CommandAliases.resolve(
                "ll Projects"
            )
        )
    }

    @Test
    fun setAlias_createsUserAlias() {

        assertTrue(
            CommandAliases.setAlias(
                name = "docs",
                command = "ls Documents"
            )
        )

        assertEquals(
            "ls Documents",
            CommandAliases.resolve(
                "docs"
            )
        )
    }

    @Test
    fun resolve_userAliasPreservesAdditionalArguments() {

        CommandAliases.setAlias(
            name = "search",
            command = "grep Atlas"
        )

        assertEquals(
            "grep Atlas notes.txt",
            CommandAliases.resolve(
                "search notes.txt"
            )
        )
    }

    @Test
    fun setAlias_canOverrideBuiltInAlias() {

        CommandAliases.setAlias(
            name = "ll",
            command = "ls Projects"
        )

        assertEquals(
            "ls Projects",
            CommandAliases.resolve(
                "ll"
            )
        )
    }

    @Test
    fun removeAlias_removesUserAlias() {

        CommandAliases.setAlias(
            name = "docs",
            command = "ls Documents"
        )

        assertTrue(
            CommandAliases.removeAlias(
                "docs"
            )
        )

        assertEquals(
            "docs",
            CommandAliases.resolve(
                "docs"
            )
        )
    }

    @Test
    fun removeAlias_doesNotRemoveBuiltInAlias() {

        assertFalse(
            CommandAliases.removeAlias(
                "ll"
            )
        )

        assertEquals(
            "ls",
            CommandAliases.resolve(
                "ll"
            )
        )
    }

    @Test
    fun getAllAliases_containsBuiltInAndUserAliases() {

        CommandAliases.setAlias(
            name = "docs",
            command = "ls Documents"
        )

        val aliases =
            CommandAliases.getAllAliases()

        assertEquals(
            "ls",
            aliases["ll"]
        )

        assertEquals(
            "ls Documents",
            aliases["docs"]
        )
    }
}