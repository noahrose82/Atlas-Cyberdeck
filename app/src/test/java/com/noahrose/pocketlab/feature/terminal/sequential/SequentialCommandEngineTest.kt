package com.noahrose.pocketlab.feature.terminal.sequential

import org.junit.Assert.assertEquals
import org.junit.Test

class SequentialCommandEngineTest {

    @Test
    fun execute_passesCompleteCommandsToExecutor() {

        val executed =
            mutableListOf<String>()

        val handled =
            SequentialCommandEngine.execute(
                command =
                    "badcommand ; echo still-running"
            ) { command ->

                executed.add(command)
            }

        assertEquals(
            true,
            handled
        )

        assertEquals(
            listOf(
                "badcommand",
                "echo still-running"
            ),
            executed
        )
    }
}