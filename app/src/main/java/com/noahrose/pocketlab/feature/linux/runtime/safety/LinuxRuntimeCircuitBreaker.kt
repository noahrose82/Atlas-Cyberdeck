package com.noahrose.pocketlab.feature.linux.runtime.safety

import com.noahrose.pocketlab.feature.linux.repository.LinuxRepository
import com.noahrose.pocketlab.feature.linux.runtime.ProotLinuxRuntimeBackend
import com.noahrose.pocketlab.feature.linux.runtime.filesystem.LinuxRuntimePathManager
import java.io.File
import java.util.Properties
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class LinuxRuntimeSafetyMode {
    NORMAL,
    SAFE_MODE,
    RECOVERY_ARMED
}

enum class LinuxRuntimeSafetyReason {
    RUNTIME_PROCESS_LOST,
    RUNTIME_INTEGRITY_FAILURE,
    GUEST_HEALTH_FAILURE,
    PACKAGE_STATE_FAILURE,
    FILESYSTEM_FAILURE,
    MANUAL_TEST
}

data class LinuxRuntimeSafetySnapshot(
    val mode: LinuxRuntimeSafetyMode = LinuxRuntimeSafetyMode.NORMAL,
    val reason: LinuxRuntimeSafetyReason? = null,
    val message: String? = null,
    val trippedAtEpochMillis: Long? = null,
    val transientCleanupSucceeded: Boolean? = null
) {

    val tripped: Boolean
        get() =
            mode !=
                    LinuxRuntimeSafetyMode.NORMAL
}

object LinuxRuntimeCircuitBreaker {

    private const val SAFETY_DIRECTORY_NAME =
        "safety"

    private const val STATE_FILE_NAME =
        "circuit-breaker.properties"

    private const val KEY_MODE =
        "mode"

    private const val KEY_REASON =
        "reason"

    private const val KEY_MESSAGE =
        "message"

    private const val KEY_TRIPPED_AT =
        "trippedAtEpochMillis"

    private const val KEY_CLEANUP_SUCCEEDED =
        "transientCleanupSucceeded"

    /*
     * H4C reactive safety state.
     */
    private val mutableSnapshotFlow =
        MutableStateFlow(
            LinuxRuntimeSafetySnapshot()
        )

    val snapshotFlow:
            StateFlow<LinuxRuntimeSafetySnapshot> =
        mutableSnapshotFlow
            .asStateFlow()

    @Volatile
    private var cachedSnapshot:
            LinuxRuntimeSafetySnapshot? =
        null

    @Synchronized
    fun getSnapshot():
            LinuxRuntimeSafetySnapshot {

        cachedSnapshot
            ?.let { snapshot ->

                return snapshot
            }

        val loaded =
            loadFromDisk()

        publish(
            loaded
        )

        return loaded
    }

    fun isTripped():
            Boolean {

        return getSnapshot()
            .tripped
    }

    /*
     * H5B:
     * Pure transition policy now lives in
     * LinuxRuntimeSafetyStateMachine.
     */
    fun canStartRuntime():
            Boolean {

        return LinuxRuntimeSafetyStateMachine
            .canStartRuntime(
                getSnapshot()
            )
    }

    fun isRecoveryArmed():
            Boolean {

        return LinuxRuntimeSafetyStateMachine
            .isRecoveryArmed(
                getSnapshot()
            )
    }

    /*
     * Fail-closed runtime trapdoor.
     *
     * State creation is pure. Runtime shutdown,
     * persistence, and transient cleanup stay here.
     */
    @Synchronized
    fun trip(
        reason: LinuxRuntimeSafetyReason,
        message: String
    ): LinuxRuntimeSafetySnapshot {

        val initialSnapshot =
            LinuxRuntimeSafetyStateMachine
                .trip(
                    reason =
                        reason,

                    message =
                        message,

                    timestampEpochMillis =
                        System.currentTimeMillis()
                )

        /*
         * Publish/persist SAFE_MODE before touching the
         * runtime. If cleanup itself fails, Atlas is
         * already latched closed.
         */
        publish(
            initialSnapshot
        )

        persist(
            initialSnapshot
        )

        /*
         * Stop the guest before transient cleanup.
         */
        runCatching {

            ProotLinuxRuntimeBackend
                .stop()
        }

        /*
         * Keep repository state aligned with the real
         * process state. Never claim STOPPED while PRoot
         * is still alive.
         */
        if (
            !ProotLinuxRuntimeBackend
                .isProcessAlive()
        ) {

            runCatching {

                LinuxRepository
                    .stopLinux()
            }
        }

        val cleanupSucceeded =
            cleanupTransientState()

        val finalSnapshot =
            LinuxRuntimeSafetyStateMachine
                .withCleanupResult(
                    snapshot =
                        initialSnapshot,

                    succeeded =
                        cleanupSucceeded
                )

        publish(
            finalSnapshot
        )

        persist(
            finalSnapshot
        )

        return finalSnapshot
    }

    /*
     * SAFE_MODE blocks normal PRoot startup.
     * RECOVERY_ARMED allows runtime startup while the
     * guest executor restricts commands to recovery-safe
     * operations.
     */
    @Synchronized
    fun armRecovery():
            LinuxRuntimeSafetySnapshot {

        val current =
            getSnapshot()

        val armed =
            LinuxRuntimeSafetyStateMachine
                .armRecovery(
                    current
                )

        if (
            armed ==
            current
        ) {

            return current
        }

        publish(
            armed
        )

        persist(
            armed
        )

        return armed
    }

    /*
     * Called only after explicit recovery verification.
     */
    @Synchronized
    fun resetAfterVerifiedRecovery() {

        val normal =
            LinuxRuntimeSafetyStateMachine
                .reset()

        publish(
            normal
        )

        stateFile()
            ?.let { file ->

                if (
                    file.exists()
                ) {

                    runCatching {

                        file.delete()
                    }
                }
            }
    }

    /*
     * Developer escape hatch.
     *
     * This does not repair anything. It only clears the
     * safety latch, so the terminal still requires
     * --force.
     */
    @Synchronized
    fun forceReset() {

        resetAfterVerifiedRecovery()
    }

    fun statusLines():
            List<String> {

        val snapshot =
            getSnapshot()

        return buildList {

            add(
                "Atlas Runtime Safety"
            )

            add(
                "Mode    : ${snapshot.mode}"
            )

            add(
                "Tripped : ${
                    if (
                        snapshot.tripped
                    ) {
                        "YES"
                    } else {
                        "NO"
                    }
                }"
            )

            snapshot
                .reason
                ?.let { reason ->

                    add(
                        "Reason  : $reason"
                    )
                }

            snapshot
                .message
                ?.takeIf { message ->

                    message.isNotBlank()
                }
                ?.let { message ->

                    add(
                        "Message : $message"
                    )
                }

            snapshot
                .trippedAtEpochMillis
                ?.let { timestamp ->

                    add(
                        "Time    : $timestamp"
                    )
                }

            snapshot
                .transientCleanupSucceeded
                ?.let { cleaned ->

                    add(
                        "Cleanup : ${
                            if (
                                cleaned
                            ) {
                                "CLEAN"
                            } else {
                                "PARTIAL"
                            }
                        }"
                    )
                }

            when (
                snapshot.mode
            ) {

                LinuxRuntimeSafetyMode.NORMAL -> {

                    add(
                        "Runtime : ENABLED"
                    )
                }

                LinuxRuntimeSafetyMode.SAFE_MODE -> {

                    add(
                        "Runtime : BLOCKED"
                    )

                    add(
                        "Recovery: run 'safety recover'"
                    )
                }

                LinuxRuntimeSafetyMode.RECOVERY_ARMED -> {

                    add(
                        "Runtime : RECOVERY ONLY"
                    )

                    add(
                        "Recovery: start Linux, then repair/audit Ubuntu"
                    )
                }
            }
        }
    }

    /*
     * Remove only disposable runtime state.
     *
     * Deliberately untouched:
     *
     * - rootfs/.l2s
     * - /var/lib/dpkg
     * - /var/lib/apt
     * - /home
     * - /root
     * - user files
     */
    private fun cleanupTransientState():
            Boolean {

        val paths =
            LinuxRuntimePathManager
                .getPaths()
                ?: return false

        var success =
            true

        success =
            clearDirectoryContents(
                paths.temporaryDirectory
            ) &&
                    success

        val guestTmp =
            File(
                paths.rootfsDirectory,
                "tmp"
            )

        success =
            clearDirectoryContents(
                guestTmp
            ) &&
                    success

        return success
    }

    private fun clearDirectoryContents(
        directory: File
    ): Boolean {

        if (
            !directory.exists()
        ) {

            return true
        }

        if (
            !directory.isDirectory
        ) {

            return false
        }

        var success =
            true

        directory
            .listFiles()
            ?.forEach { child ->

                val deleted =
                    runCatching {

                        child.deleteRecursively()
                    }
                        .getOrDefault(
                            false
                        )

                if (
                    !deleted &&
                    child.exists()
                ) {

                    success =
                        false
                }
            }

        return success
    }

    private fun stateFile():
            File? {

        val paths =
            LinuxRuntimePathManager
                .getPaths()
                ?: return null

        val runtimeRoot =
            paths.rootfsDirectory
                .parentFile
                ?: return null

        val safetyDirectory =
            File(
                runtimeRoot,
                SAFETY_DIRECTORY_NAME
            )

        if (
            !safetyDirectory.exists() &&
            !safetyDirectory.mkdirs()
        ) {

            return null
        }

        return File(
            safetyDirectory,
            STATE_FILE_NAME
        )
    }

    private fun persist(
        snapshot: LinuxRuntimeSafetySnapshot
    ) {

        val file =
            stateFile()
                ?: return

        val properties =
            Properties()

        properties[
            KEY_MODE
        ] =
            snapshot
                .mode
                .name

        snapshot
            .reason
            ?.let { reason ->

                properties[
                    KEY_REASON
                ] =
                    reason.name
            }

        snapshot
            .message
            ?.let { message ->

                properties[
                    KEY_MESSAGE
                ] =
                    message
            }

        snapshot
            .trippedAtEpochMillis
            ?.let { timestamp ->

                properties[
                    KEY_TRIPPED_AT
                ] =
                    timestamp.toString()
            }

        snapshot
            .transientCleanupSucceeded
            ?.let { succeeded ->

                properties[
                    KEY_CLEANUP_SUCCEEDED
                ] =
                    succeeded.toString()
            }

        runCatching {

            file
                .outputStream()
                .use { output ->

                    properties.store(
                        output,
                        "Atlas Cyberdeck Linux runtime safety state"
                    )
                }
        }
    }

    private fun loadFromDisk():
            LinuxRuntimeSafetySnapshot {

        val file =
            stateFile()
                ?: return LinuxRuntimeSafetySnapshot()

        if (
            !file.exists() ||
            !file.isFile
        ) {

            return LinuxRuntimeSafetySnapshot()
        }

        return runCatching {

            val properties =
                Properties()

            file
                .inputStream()
                .use { input ->

                    properties.load(
                        input
                    )
                }

            val mode =
                properties
                    .getProperty(
                        KEY_MODE
                    )
                    ?.let { value ->

                        runCatching {

                            LinuxRuntimeSafetyMode
                                .valueOf(
                                    value
                                )
                        }
                            .getOrNull()
                    }
                    ?: LinuxRuntimeSafetyMode.SAFE_MODE

            val reason =
                properties
                    .getProperty(
                        KEY_REASON
                    )
                    ?.let { value ->

                        runCatching {

                            LinuxRuntimeSafetyReason
                                .valueOf(
                                    value
                                )
                        }
                            .getOrNull()
                    }

            val message =
                properties
                    .getProperty(
                        KEY_MESSAGE
                    )

            val trippedAt =
                properties
                    .getProperty(
                        KEY_TRIPPED_AT
                    )
                    ?.toLongOrNull()

            val cleanupSucceeded =
                properties
                    .getProperty(
                        KEY_CLEANUP_SUCCEEDED
                    )
                    ?.toBooleanStrictOrNull()

            LinuxRuntimeSafetySnapshot(
                mode =
                    mode,

                reason =
                    reason,

                message =
                    message,

                trippedAtEpochMillis =
                    trippedAt,

                transientCleanupSucceeded =
                    cleanupSucceeded
            )

        }.getOrElse {

            /*
             * A corrupted safety record fails CLOSED.
             */
            LinuxRuntimeSafetyStateMachine
                .failClosed(
                    message =
                        "Atlas could not read the runtime safety record.",

                    timestampEpochMillis =
                        System.currentTimeMillis()
                )
        }
    }

    private fun publish(
        snapshot: LinuxRuntimeSafetySnapshot
    ) {

        cachedSnapshot =
            snapshot

        mutableSnapshotFlow
            .value =
            snapshot
    }
}
