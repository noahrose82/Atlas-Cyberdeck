package com.noahrose.pocketlab.feature.terminal.execution

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ExecutionStatusTest {

    @Before
    fun resetStatus() {
        ExecutionStatus.set(0)
    }

    @Test
    fun defaultSuccessCode_isZero() {

        assertEquals(
            0,
            ExecutionStatus.get()
        )

        assertTrue(
            ExecutionStatus.wasSuccessful()
        )
    }

    @Test
    fun failureCode_isStored() {

        ExecutionStatus.set(1)

        assertEquals(
            1,
            ExecutionStatus.get()
        )

        assertFalse(
            ExecutionStatus.wasSuccessful()
        )
    }

    @Test
    fun commandNotFoundCode_isStored() {

        ExecutionStatus.set(127)

        assertEquals(
            127,
            ExecutionStatus.get()
        )

        assertFalse(
            ExecutionStatus.wasSuccessful()
        )
    }

    @Test
    fun successCanBeRestoredAfterFailure() {

        ExecutionStatus.set(1)

        ExecutionStatus.set(0)

        assertEquals(
            0,
            ExecutionStatus.get()
        )

        assertTrue(
            ExecutionStatus.wasSuccessful()
        )
    }
}