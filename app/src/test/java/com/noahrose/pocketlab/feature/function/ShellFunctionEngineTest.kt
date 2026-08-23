package com.noahrose.pocketlab.feature.terminal.function

import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ShellFunctionEngineTest {

    @After
    fun cleanUp() {

        ShellFunctions.clear()
    }

    @Test
    fun execute_returnsFalseForUnknownFunction() {

        val output =
            mutableListOf<String>()

        assertFalse(
            ShellFunctionEngine.execute(
                name = "missing",
                arguments = emptyList(),
                output = output
            )
        )
    }

    @Test
    fun execute_runsDefinedFunction() {

        ShellFunctions.define(
            name = "status",
            commands = listOf(
                "echo Atlas"
            )
        )

        val output =
            mutableListOf<String>()

        assertTrue(
            ShellFunctionEngine.execute(
                name = "status",
                arguments = emptyList(),
                output = output,
                showPrompt = false
            )
        )

        assertTrue(
            output.contains(
                "Atlas"
            )
        )
    }

    @Test
    fun execute_expandsFirstArgument() {

        ShellFunctions.define(
            name = "greet",
            commands = listOf(
                "echo Hello \$1"
            )
        )

        val output =
            mutableListOf<String>()

        assertTrue(
            ShellFunctionEngine.execute(
                name = "greet",
                arguments = listOf(
                    "Noah"
                ),
                output = output,
                showPrompt = false
            )
        )

        assertTrue(
            output.contains(
                "Hello Noah"
            )
        )
    }

    @Test
    fun execute_expandsMultipleArguments() {

        ShellFunctions.define(
            name = "greet",
            commands = listOf(
                "echo Hello \$1",
                "echo Welcome to \$2"
            )
        )

        val output =
            mutableListOf<String>()

        assertTrue(
            ShellFunctionEngine.execute(
                name = "greet",
                arguments = listOf(
                    "Noah",
                    "Cyberdeck"
                ),
                output = output,
                showPrompt = false
            )
        )

        assertTrue(
            output.contains(
                "Hello Noah"
            )
        )

        assertTrue(
            output.contains(
                "Welcome to Cyberdeck"
            )
        )
    }

    @Test
    fun execute_expandsAllArguments() {

        ShellFunctions.define(
            name = "showargs",
            commands = listOf(
                "echo Args: \$@"
            )
        )

        val output =
            mutableListOf<String>()

        ShellFunctionEngine.execute(
            name = "showargs",
            arguments = listOf(
                "one",
                "two",
                "three"
            ),
            output = output,
            showPrompt = false
        )

        assertTrue(
            output.contains(
                "Args: one two three"
            )
        )
    }

    @Test
    fun execute_expandsArgumentCount() {

        ShellFunctions.define(
            name = "showargs",
            commands = listOf(
                "echo Count: \$#"
            )
        )

        val output =
            mutableListOf<String>()

        ShellFunctionEngine.execute(
            name = "showargs",
            arguments = listOf(
                "one",
                "two",
                "three"
            ),
            output = output,
            showPrompt = false
        )

        assertTrue(
            output.contains(
                "Count: 3"
            )
        )
    }

}