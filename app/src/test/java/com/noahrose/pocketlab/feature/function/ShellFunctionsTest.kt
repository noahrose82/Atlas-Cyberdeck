package com.noahrose.pocketlab.feature.terminal.function

import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ShellFunctionsTest {

    @After
    fun cleanUp() {

        ShellFunctions.clear()
    }

    @Test
    fun define_createsFunction() {

        val result =
            ShellFunctions.define(
                name = "status",
                commands = listOf(
                    "echo Atlas",
                    "pwd"
                )
            )

        assertTrue(result)

        assertTrue(
            ShellFunctions.exists(
                "status"
            )
        )
    }

    @Test
    fun get_returnsFunctionCommands() {

        ShellFunctions.define(
            name = "status",
            commands = listOf(
                "echo Atlas",
                "echo \$MODE",
                "pwd"
            )
        )

        assertEquals(
            listOf(
                "echo Atlas",
                "echo \$MODE",
                "pwd"
            ),
            ShellFunctions.get(
                "status"
            )
        )
    }

    @Test
    fun define_rejectsBlankName() {

        assertFalse(
            ShellFunctions.define(
                name = "",
                commands = listOf(
                    "echo Atlas"
                )
            )
        )
    }

    @Test
    fun define_rejectsEmptyBody() {

        assertFalse(
            ShellFunctions.define(
                name = "status",
                commands = emptyList()
            )
        )
    }

    @Test
    fun define_rejectsNameStartingWithNumber() {

        assertFalse(
            ShellFunctions.define(
                name = "51status",
                commands = listOf(
                    "echo Atlas"
                )
            )
        )
    }

    @Test
    fun define_rejectsInvalidCharacters() {

        assertFalse(
            ShellFunctions.define(
                name = "atlas-status",
                commands = listOf(
                    "echo Atlas"
                )
            )
        )
    }

    @Test
    fun define_allowsUnderscoreName() {

        assertTrue(
            ShellFunctions.define(
                name = "atlas_status",
                commands = listOf(
                    "echo Atlas"
                )
            )
        )
    }

    @Test
    fun define_replacesExistingFunction() {

        ShellFunctions.define(
            name = "status",
            commands = listOf(
                "echo old"
            )
        )

        ShellFunctions.define(
            name = "status",
            commands = listOf(
                "echo new"
            )
        )

        assertEquals(
            listOf(
                "echo new"
            ),
            ShellFunctions.get(
                "status"
            )
        )
    }

    @Test
    fun remove_deletesFunction() {

        ShellFunctions.define(
            name = "status",
            commands = listOf(
                "echo Atlas"
            )
        )

        assertTrue(
            ShellFunctions.remove(
                "status"
            )
        )

        assertFalse(
            ShellFunctions.exists(
                "status"
            )
        )

        assertNull(
            ShellFunctions.get(
                "status"
            )
        )
    }

    @Test
    fun getAll_returnsDefinedFunctions() {

        ShellFunctions.define(
            name = "status",
            commands = listOf(
                "echo Atlas"
            )
        )

        ShellFunctions.define(
            name = "mode",
            commands = listOf(
                "echo \$MODE"
            )
        )

        val functions =
            ShellFunctions.getAll()

        assertEquals(
            2,
            functions.size
        )

        assertEquals(
            listOf("echo Atlas"),
            functions["status"]
        )

        assertEquals(
            listOf("echo \$MODE"),
            functions["mode"]
        )
    }
}