package com.noahrose.pocketlab.feature.linux.runtime.safety

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LinuxRuntimeSafetyStateMachineTest {

    @Test
    fun normalStateAllowsRuntimeStart() {

        val snapshot =
            LinuxRuntimeSafetySnapshot()

        assertTrue(
            LinuxRuntimeSafetyStateMachine
                .canStartRuntime(
                    snapshot
                )
        )
    }

    @Test
    fun safeModeBlocksRuntimeStart() {

        val snapshot =
            LinuxRuntimeSafetySnapshot(
                mode =
                    LinuxRuntimeSafetyMode.SAFE_MODE
            )

        assertFalse(
            LinuxRuntimeSafetyStateMachine
                .canStartRuntime(
                    snapshot
                )
        )
    }

    @Test
    fun recoveryArmedAllowsRuntimeStart() {

        val snapshot =
            LinuxRuntimeSafetySnapshot(
                mode =
                    LinuxRuntimeSafetyMode.RECOVERY_ARMED
            )

        assertTrue(
            LinuxRuntimeSafetyStateMachine
                .canStartRuntime(
                    snapshot
                )
        )

        assertTrue(
            LinuxRuntimeSafetyStateMachine
                .isRecoveryArmed(
                    snapshot
                )
        )
    }

    @Test
    fun tripTransitionsDirectlyToSafeMode() {

        val snapshot =
            LinuxRuntimeSafetyStateMachine
                .trip(
                    reason =
                        LinuxRuntimeSafetyReason.MANUAL_TEST,

                    message =
                        "Manual H5B test.",

                    timestampEpochMillis =
                        1234L
                )

        assertEquals(
            LinuxRuntimeSafetyMode.SAFE_MODE,
            snapshot.mode
        )

        assertEquals(
            LinuxRuntimeSafetyReason.MANUAL_TEST,
            snapshot.reason
        )

        assertEquals(
            "Manual H5B test.",
            snapshot.message
        )

        assertEquals(
            1234L,
            snapshot.trippedAtEpochMillis
        )

        assertNull(
            snapshot.transientCleanupSucceeded
        )

        assertTrue(
            snapshot.tripped
        )
    }

    @Test
    fun blankTripMessageReceivesSafeDefault() {

        val snapshot =
            LinuxRuntimeSafetyStateMachine
                .trip(
                    reason =
                        LinuxRuntimeSafetyReason.GUEST_HEALTH_FAILURE,

                    message =
                        "   ",

                    timestampEpochMillis =
                        99L
                )

        assertEquals(
            "Atlas Linux runtime entered safe mode.",
            snapshot.message
        )
    }

    @Test
    fun cleanupResultDoesNotChangeSafetyMode() {

        val tripped =
            LinuxRuntimeSafetyStateMachine
                .trip(
                    reason =
                        LinuxRuntimeSafetyReason.FILESYSTEM_FAILURE,

                    message =
                        "Filesystem failure.",

                    timestampEpochMillis =
                        500L
                )

        val completed =
            LinuxRuntimeSafetyStateMachine
                .withCleanupResult(
                    snapshot =
                        tripped,

                    succeeded =
                        false
                )

        assertEquals(
            LinuxRuntimeSafetyMode.SAFE_MODE,
            completed.mode
        )

        assertEquals(
            false,
            completed.transientCleanupSucceeded
        )
    }

    @Test
    fun armRecoveryTransitionsTrippedState() {

        val tripped =
            LinuxRuntimeSafetyStateMachine
                .trip(
                    reason =
                        LinuxRuntimeSafetyReason.PACKAGE_STATE_FAILURE,

                    message =
                        "Package state failure.",

                    timestampEpochMillis =
                        1000L
                )

        val recovery =
            LinuxRuntimeSafetyStateMachine
                .armRecovery(
                    tripped
                )

        assertEquals(
            LinuxRuntimeSafetyMode.RECOVERY_ARMED,
            recovery.mode
        )

        assertEquals(
            tripped.reason,
            recovery.reason
        )

        assertEquals(
            tripped.message,
            recovery.message
        )

        assertEquals(
            tripped.trippedAtEpochMillis,
            recovery.trippedAtEpochMillis
        )
    }

    @Test
    fun armRecoveryDoesNothingToNormalState() {

        val normal =
            LinuxRuntimeSafetySnapshot()

        val result =
            LinuxRuntimeSafetyStateMachine
                .armRecovery(
                    normal
                )

        assertEquals(
            normal,
            result
        )
    }

    @Test
    fun resetReturnsCleanNormalState() {

        val reset =
            LinuxRuntimeSafetyStateMachine
                .reset()

        assertEquals(
            LinuxRuntimeSafetyMode.NORMAL,
            reset.mode
        )

        assertFalse(
            reset.tripped
        )

        assertNull(
            reset.reason
        )

        assertNull(
            reset.message
        )

        assertNull(
            reset.trippedAtEpochMillis
        )

        assertNull(
            reset.transientCleanupSucceeded
        )
    }

    @Test
    fun failClosedProducesIntegritySafeMode() {

        val failed =
            LinuxRuntimeSafetyStateMachine
                .failClosed(
                    message =
                        "Safety record unreadable.",

                    timestampEpochMillis =
                        8080L
                )

        assertEquals(
            LinuxRuntimeSafetyMode.SAFE_MODE,
            failed.mode
        )

        assertEquals(
            LinuxRuntimeSafetyReason.RUNTIME_INTEGRITY_FAILURE,
            failed.reason
        )

        assertEquals(
            "Safety record unreadable.",
            failed.message
        )

        assertEquals(
            8080L,
            failed.trippedAtEpochMillis
        )

        assertFalse(
            LinuxRuntimeSafetyStateMachine
                .canStartRuntime(
                    failed
                )
        )
    }
}
