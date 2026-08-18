package com.noahrose.pocketlab.feature.terminal.sequential

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SequentialCommandParserTest {

    @Test
    fun parse_splitsSequentialCommands() {

        val result =
            SequentialCommandParser.parse(
                "echo one ; echo two ; echo three"
            )

        assertEquals(
            listOf(
                "echo one",
                "echo two",
                "echo three"
            ),
            result
        )
    }

    @Test
    fun parse_ignoresSemicolonInsideDoubleQuotes() {

        val result =
            SequentialCommandParser.parse(
                """echo "Area 51; Classified""""
            )

        assertNull(result)
    }

    @Test
    fun parse_ignoresSemicolonInsideSingleQuotes() {

        val result =
            SequentialCommandParser.parse(
                "echo 'Area 51; Classified'"
            )

        assertNull(result)
    }

    @Test
    fun parse_handlesQuotedSemicolonInsideSequence() {

        val result =
            SequentialCommandParser.parse(
                """echo "A; B" ; echo success"""
            )

        assertEquals(
            listOf(
                """echo "A; B"""",
                "echo success"
            ),
            result
        )
    }

    @Test
    fun parse_preservesConditionalCommands() {

        val result =
            SequentialCommandParser.parse(
                "echo one && echo two ; echo three"
            )

        assertEquals(
            listOf(
                "echo one && echo two",
                "echo three"
            ),
            result
        )
    }

    @Test
    fun parse_rejectsEmptyMiddleCommand() {

        val result =
            SequentialCommandParser.parse(
                "echo one ; ; echo two"
            )

        assertNull(result)
    }

    @Test
    fun parse_returnsNullWithoutSeparator() {

        val result =
            SequentialCommandParser.parse(
                "echo one"
            )

        assertNull(result)
    }

    @Test
    fun parse_preservesCommandAfterFailureSegment() {

        val result =
            SequentialCommandParser.parse(
                "badcommand ; echo still-running"
            )

        assertEquals(
            listOf(
                "badcommand",
                "echo still-running"
            ),
            result
        )
    }

}