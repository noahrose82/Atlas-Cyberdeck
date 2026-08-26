package com.noahrose.pocketlab.feature.linux.runtime.safety

/*
 * ------------------------------------------------
 * H5B — PURE RUNTIME SAFETY STATE MACHINE
 * ------------------------------------------------
 *
 * This object owns only state-transition rules.
 *
 * It intentionally has no Android Context, filesystem,
 * PRoot, repository, persistence, or coroutine side
 * effects. That makes the safety contract directly
 * testable with normal JVM unit tests.
 */
object LinuxRuntimeSafetyStateMachine {

    fun canStartRuntime(
        snapshot: LinuxRuntimeSafetySnapshot
    ): Boolean {

        return when (
            snapshot.mode
        ) {

            LinuxRuntimeSafetyMode.NORMAL,
            LinuxRuntimeSafetyMode.RECOVERY_ARMED ->
                true

            LinuxRuntimeSafetyMode.SAFE_MODE ->
                false
        }
    }

    fun isRecoveryArmed(
        snapshot: LinuxRuntimeSafetySnapshot
    ): Boolean {

        return snapshot.mode ==
                LinuxRuntimeSafetyMode.RECOVERY_ARMED
    }

    fun trip(
        reason: LinuxRuntimeSafetyReason,
        message: String,
        timestampEpochMillis: Long
    ): LinuxRuntimeSafetySnapshot {

        return LinuxRuntimeSafetySnapshot(
            mode =
                LinuxRuntimeSafetyMode.SAFE_MODE,

            reason =
                reason,

            message =
                message
                    .trim()
                    .ifBlank {
                        "Atlas Linux runtime entered safe mode."
                    },

            trippedAtEpochMillis =
                timestampEpochMillis,

            transientCleanupSucceeded =
                null
        )
    }

    fun withCleanupResult(
        snapshot: LinuxRuntimeSafetySnapshot,
        succeeded: Boolean
    ): LinuxRuntimeSafetySnapshot {

        return snapshot.copy(
            transientCleanupSucceeded =
                succeeded
        )
    }

    fun armRecovery(
        snapshot: LinuxRuntimeSafetySnapshot
    ): LinuxRuntimeSafetySnapshot {

        if (
            !snapshot.tripped
        ) {

            return snapshot
        }

        return snapshot.copy(
            mode =
                LinuxRuntimeSafetyMode.RECOVERY_ARMED
        )
    }

    fun reset():
            LinuxRuntimeSafetySnapshot {

        return LinuxRuntimeSafetySnapshot()
    }

    fun failClosed(
        message: String,
        timestampEpochMillis: Long
    ): LinuxRuntimeSafetySnapshot {

        return LinuxRuntimeSafetySnapshot(
            mode =
                LinuxRuntimeSafetyMode.SAFE_MODE,

            reason =
                LinuxRuntimeSafetyReason.RUNTIME_INTEGRITY_FAILURE,

            message =
                message
                    .trim()
                    .ifBlank {
                        "Atlas could not read the runtime safety record."
                    },

            trippedAtEpochMillis =
                timestampEpochMillis,

            transientCleanupSucceeded =
                null
        )
    }
}
