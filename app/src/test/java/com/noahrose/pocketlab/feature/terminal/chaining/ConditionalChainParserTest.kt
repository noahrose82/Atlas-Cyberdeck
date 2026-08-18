package com.noahrose.pocketlab.feature.terminal.chaining

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class ConditionalChainParserTest {

    @Test
    fun parse_returnsNullWhenNoConditionalOperatorExists() {

        val result =
            ConditionalChainParser.parse(
                "echo hello"
            )

        assertNull(result)
    }

    @Test
    fun parse_parsesAndOperator() {

        val result =
            ConditionalChainParser.parse(
                "touch alpha.txt && echo success"
            )

        assertNotNull(result)

        assertEquals(
            2,
            result!!.size
        )

        assertEquals(
            "touch alpha.txt",
            result[0].command
        )

        assertNull(
            result[0].operatorBefore
        )

        assertEquals(
            "echo success",
            result[1].command
        )

        assertEquals(
            ConditionalOperator.AND,
            result[1].operatorBefore
        )
    }

    @Test
    fun parse_parsesOrOperator() {

        val result =
            ConditionalChainParser.parse(
                "cd Missing || echo fallback"
            )

        assertNotNull(result)

        assertEquals(
            2,
            result!!.size
        )

        assertEquals(
            ConditionalOperator.OR,
            result[1].operatorBefore
        )
    }

    @Test
    fun parse_preservesMixedOperatorOrder() {

        val result =
            ConditionalChainParser.parse(
                "cd Missing || mkdir Recovery && cd Recovery"
            )

        assertNotNull(result)

        assertEquals(
            3,
            result!!.size
        )

        assertEquals(
            "cd Missing",
            result[0].command
        )

        assertEquals(
            "mkdir Recovery",
            result[1].command
        )

        assertEquals(
            ConditionalOperator.OR,
            result[1].operatorBefore
        )

        assertEquals(
            "cd Recovery",
            result[2].command
        )

        assertEquals(
            ConditionalOperator.AND,
            result[2].operatorBefore
        )
    }

    @Test
    fun parse_rejectsIncompleteChain() {

        val result =
            ConditionalChainParser.parse(
                "echo hello &&"
            )

        assertNull(result)
    }

    @Test
    fun parse_ignoresAndOperatorInsideDoubleQuotes() {

        val result =
            ConditionalChainParser.parse(
                """echo "A && B""""
            )

        assertNull(result)
    }

    @Test
    fun parse_ignoresOrOperatorInsideSingleQuotes() {

        val result =
            ConditionalChainParser.parse(
                "echo 'A || B'"
            )

        assertNull(result)
    }

    @Test
    fun parse_handlesQuotedTextInsideRealChain() {

        val result =
            ConditionalChainParser.parse(
                """echo "Area && 51" && echo success"""
            )

        assertNotNull(result)

        assertEquals(
            2,
            result!!.size
        )

        assertEquals(
            """echo "Area && 51"""",
            result[0].command
        )

        assertEquals(
            ConditionalOperator.AND,
            result[1].operatorBefore
        )

        assertEquals(
            "echo success",
            result[1].command
        )
    }

    @Test
    fun parse_handlesMixedRealOperatorsAndQuotedOperators() {

        val result =
            ConditionalChainParser.parse(
                """echo "A || B" && cd Missing || echo fallback"""
            )

        assertNotNull(result)

        assertEquals(
            3,
            result!!.size
        )

        assertEquals(
            ConditionalOperator.AND,
            result[1].operatorBefore
        )

        assertEquals(
            ConditionalOperator.OR,
            result[2].operatorBefore
        )
    }

}