package com.noahrose.pocketlab.feature.terminal.function

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ShellFunctionParserTest {

    @Test
    fun parse_parsesFunctionDefinition() {

        val result =
            ShellFunctionParser.parse(
                """function status { echo "Atlas Cyberdeck"; echo ${'$'}MODE; pwd }"""
            )


        assertEquals(
            "status",
            result?.name
        )

        assertEquals(
            listOf(
                """echo "Atlas Cyberdeck"""",
                "echo \$MODE",
                "pwd"
            ),
            result?.commands
        )
    }

    @Test
    fun parse_preservesQuotedSemicolon() {

        val result =
            ShellFunctionParser.parse(
                """function test { echo "A; B"; pwd }"""
            )

        assertEquals(
            listOf(
                """echo "A; B"""",
                "pwd"
            ),
            result?.commands
        )
    }

    @Test
    fun parse_rejectsInvalidName() {

        assertNull(
            ShellFunctionParser.parse(
                "function 51status { echo Atlas }"
            )
        )
    }

    @Test
    fun parse_rejectsMissingOpeningBrace() {

        assertNull(
            ShellFunctionParser.parse(
                "function status echo Atlas }"
            )
        )
    }

    @Test
    fun parse_rejectsMissingClosingBrace() {

        assertNull(
            ShellFunctionParser.parse(
                "function status { echo Atlas"
            )
        )
    }

    @Test
    fun parse_rejectsEmptyBody() {

        assertNull(
            ShellFunctionParser.parse(
                "function status { }"
            )
        )
    }
}