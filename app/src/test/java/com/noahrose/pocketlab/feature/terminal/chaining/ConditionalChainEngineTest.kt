package com.noahrose.pocketlab.feature.terminal.chaining

import com.noahrose.pocketlab.feature.terminal.execution.ExecutionStatus
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class ConditionalChainEngineTest {

    @Before
    fun resetStatus() {
        ExecutionStatus.set(0)
    }

    @Test
    fun and_executesSecondCommandWhenFirstSucceeds() {

        val executed =
            mutableListOf<String>()

        val handled =
            ConditionalChainEngine.execute(
                command = "first && second"
            ) { command ->

                executed.add(command)

                when (command) {

                    "first" ->
                        ExecutionStatus.set(0)

                    "second" ->
                        ExecutionStatus.set(0)
                }
            }

        assertEquals(
            true,
            handled
        )

        assertEquals(
            listOf(
                "first",
                "second"
            ),
            executed
        )
    }

    @Test
    fun and_skipsSecondCommandWhenFirstFails() {

        val executed =
            mutableListOf<String>()

        ConditionalChainEngine.execute(
            command = "first && second"
        ) { command ->

            executed.add(command)

            if (command == "first") {
                ExecutionStatus.set(1)
            }
        }

        assertEquals(
            listOf(
                "first"
            ),
            executed
        )
    }

    @Test
    fun or_executesSecondCommandWhenFirstFails() {

        val executed =
            mutableListOf<String>()

        ConditionalChainEngine.execute(
            command = "first || second"
        ) { command ->

            executed.add(command)

            when (command) {

                "first" ->
                    ExecutionStatus.set(1)

                "second" ->
                    ExecutionStatus.set(0)
            }
        }

        assertEquals(
            listOf(
                "first",
                "second"
            ),
            executed
        )
    }

    @Test
    fun or_skipsSecondCommandWhenFirstSucceeds() {

        val executed =
            mutableListOf<String>()

        ConditionalChainEngine.execute(
            command = "first || second"
        ) { command ->

            executed.add(command)

            if (command == "first") {
                ExecutionStatus.set(0)
            }
        }

        assertEquals(
            listOf(
                "first"
            ),
            executed
        )
    }

    @Test
    fun mixedChain_recoversFromFailureAndContinuesAfterSuccess() {

        val executed =
            mutableListOf<String>()

        ConditionalChainEngine.execute(
            command = "first || recovery && final"
        ) { command ->

            executed.add(command)

            when (command) {

                "first" ->
                    ExecutionStatus.set(1)

                "recovery" ->
                    ExecutionStatus.set(0)

                "final" ->
                    ExecutionStatus.set(0)
            }
        }

        assertEquals(
            listOf(
                "first",
                "recovery",
                "final"
            ),
            executed
        )
    }

    @Test
    fun mixedChain_skipsOrBranchAfterSuccessThenContinuesAndBranch() {

        val executed =
            mutableListOf<String>()

        ConditionalChainEngine.execute(
            command = "first || fallback && final"
        ) { command ->

            executed.add(command)

            when (command) {

                "first" ->
                    ExecutionStatus.set(0)

                "fallback" ->
                    ExecutionStatus.set(0)

                "final" ->
                    ExecutionStatus.set(0)
            }
        }

        assertEquals(
            listOf(
                "first",
                "final"
            ),
            executed
        )
    }

    @Test
    fun commandWithoutConditionalOperator_isNotHandled() {

        val executed =
            mutableListOf<String>()

        val handled =
            ConditionalChainEngine.execute(
                command = "single"
            ) { command ->

                executed.add(command)
            }

        assertEquals(
            false,
            handled
        )

        assertEquals(
            emptyList<String>(),
            executed
        )
    }
}