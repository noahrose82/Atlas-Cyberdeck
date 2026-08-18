package com.noahrose.pocketlab.feature.terminal.parsing

import org.junit.Assert.assertEquals
import org.junit.Test

class CommandTokenizerTest {

    @Test
    fun tokenize_splitsNormalCommand() {

        val result =
            CommandTokenizer.tokenize(
                "touch file.txt"
            )

        assertEquals(
            listOf(
                "touch",
                "file.txt"
            ),
            result
        )
    }

    @Test
    fun tokenize_preservesDoubleQuotedArgument() {

        val result =
            CommandTokenizer.tokenize(
                """touch "classified files.txt""""
            )

        assertEquals(
            listOf(
                "touch",
                "classified files.txt"
            ),
            result
        )
    }

    @Test
    fun tokenize_preservesSingleQuotedArgument() {

        val result =
            CommandTokenizer.tokenize(
                "echo 'Area 51'"
            )

        assertEquals(
            listOf(
                "echo",
                "Area 51"
            ),
            result
        )
    }

    @Test
    fun tokenize_preservesQuotedDirectoryName() {

        val result =
            CommandTokenizer.tokenize(
                """mkdir "Area 51""""
            )

        assertEquals(
            listOf(
                "mkdir",
                "Area 51"
            ),
            result
        )
    }

    @Test
    fun tokenize_handlesMultipleDoubleQuotedArguments() {

        val result =
            CommandTokenizer.tokenize(
                """cp "Area 51.txt" "Classified Files.txt""""
            )

        assertEquals(
            listOf(
                "cp",
                "Area 51.txt",
                "Classified Files.txt"
            ),
            result
        )
    }

    @Test
    fun tokenize_handlesMultipleSingleQuotedArguments() {

        val result =
            CommandTokenizer.tokenize(
                "cp 'secret document.txt' 'backup document.txt'"
            )

        assertEquals(
            listOf(
                "cp",
                "secret document.txt",
                "backup document.txt"
            ),
            result
        )
    }

    @Test
    fun tokenize_preservesMoveArgumentsWithSpaces() {

        val result =
            CommandTokenizer.tokenize(
                """mv "backup documents.txt" "top secret documents.txt""""
            )

        assertEquals(
            listOf(
                "mv",
                "backup documents.txt",
                "top secret documents.txt"
            ),
            result
        )
    }

    @Test
    fun tokenize_handlesMixedQuotedAndNormalArguments() {

        val result =
            CommandTokenizer.tokenize(
                """grep "Top Secret" report.txt"""
            )

        assertEquals(
            listOf(
                "grep",
                "Top Secret",
                "report.txt"
            ),
            result
        )
    }

    @Test
    fun tokenize_preservesDoubleQuotesInsideSingleQuotes() {

        val result =
            CommandTokenizer.tokenize(
                """echo 'Project "Area 51"'"""
            )

        assertEquals(
            listOf(
                "echo",
                """Project "Area 51""""
            ),
            result
        )
    }

    @Test
    fun tokenize_preservesSingleQuotesInsideDoubleQuotes() {

        val result =
            CommandTokenizer.tokenize(
                """echo "Project 'Area 51'""""
            )

        assertEquals(
            listOf(
                "echo",
                "Project 'Area 51'"
            ),
            result
        )
    }

    @Test
    fun tokenize_handlesEscapedQuotes() {

        val result =
            CommandTokenizer.tokenize(
                """echo "He said \"classified\"""""
            )

        assertEquals(
            listOf(
                "echo",
                """He said "classified""""
            ),
            result
        )
    }

    @Test
    fun tokenize_preservesEscapedBackslash() {

        val result =
            CommandTokenizer.tokenize(
                """echo Area\\51"""
            )

        assertEquals(
            listOf(
                "echo",
                "Area\\51"
            ),
            result
        )
    }

    @Test
    fun tokenize_handlesExtraWhitespace() {

        val result =
            CommandTokenizer.tokenize(
                """   echo    "Hello Area 51"   """
            )

        assertEquals(
            listOf(
                "echo",
                "Hello Area 51"
            ),
            result
        )
    }

    @Test
    fun tokenize_handlesEmptyInput() {

        val result =
            CommandTokenizer.tokenize(
                ""
            )

        assertEquals(
            emptyList<String>(),
            result
        )
    }

    @Test
    fun tokenizeOrNull_rejectsUnmatchedDoubleQuote() {

        val result =
            CommandTokenizer.tokenizeOrNull(
                """echo "Area 51"""
            )

        assertEquals(
            null,
            result
        )
    }

    @Test
    fun tokenizeOrNull_rejectsUnmatchedSingleQuote() {

        val result =
            CommandTokenizer.tokenizeOrNull(
                "echo 'Area 51"
            )

        assertEquals(
            null,
            result
        )
    }

    @Test
    fun tokenizeOrNull_acceptsBalancedDoubleQuotes() {

        val result =
            CommandTokenizer.tokenizeOrNull(
                """echo "Area 51""""
            )

        assertEquals(
            listOf(
                "echo",
                "Area 51"
            ),
            result
        )
    }

    @Test
    fun tokenizeOrNull_acceptsBalancedSingleQuotes() {

        val result =
            CommandTokenizer.tokenizeOrNull(
                "echo 'Area 51'"
            )

        assertEquals(
            listOf(
                "echo",
                "Area 51"
            ),
            result
        )
    }
}