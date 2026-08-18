package com.noahrose.pocketlab.feature.terminal.redirection

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class RedirectionParserTest {

    @Test
    fun parse_handlesQuotedOverwriteTarget() {

        val result =
            RedirectionParser.parse(
                """echo "Top Secret" > "classified notes.txt""""
            )

        assertEquals(
            RedirectionType.OVERWRITE,
            result?.type
        )

        assertEquals(
            """echo "Top Secret"""",
            result?.command
        )

        assertEquals(
            "classified notes.txt",
            result?.target
        )
    }

    @Test
    fun parse_handlesQuotedAppendTarget() {

        val result =
            RedirectionParser.parse(
                """echo "More Data" >> "classified notes.txt""""
            )

        assertEquals(
            RedirectionType.APPEND,
            result?.type
        )

        assertEquals(
            "classified notes.txt",
            result?.target
        )
    }

    @Test
    fun parse_handlesQuotedInputTarget() {

        val result =
            RedirectionParser.parse(
                """sort < "classified notes.txt""""
            )

        assertEquals(
            RedirectionType.INPUT,
            result?.type
        )

        assertEquals(
            "classified notes.txt",
            result?.target
        )
    }

    @Test
    fun parse_ignoresOperatorInsideQuotes() {

        val result =
            RedirectionParser.parse(
                """echo "A > B""""
            )

        assertNull(
            result
        )
    }

    @Test
    fun parse_rejectsMultipleTargets() {

        val result =
            RedirectionParser.parse(
                """echo Test > one.txt two.txt"""
            )

        assertNull(
            result
        )
    }
}