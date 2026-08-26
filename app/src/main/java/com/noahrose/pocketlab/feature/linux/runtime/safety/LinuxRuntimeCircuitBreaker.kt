package com.noahrose.pocketlab.feature.linux.runtime.safety

import com.noahrose.pocketlab.feature.linux.repository.LinuxRepository
import com.noahrose.pocketlab.feature.linux.runtime.ProotLinuxRuntimeBackend
import com.noahrose.pocketlab.feature.linux.runtime.filesystem.LinuxRuntimePathManager
import java.io.File
import java.util.Properties

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

        cachedSnapshot =
            loaded

        return loaded
    }

    fun isTripped():
            Boolean {

        return getSnapshot()
            .tripped
    }

    fun canStartRuntime():
            Boolean {

        return when (
            getSnapshot()
                .mode
        ) {

            LinuxRuntimeSafetyMode.NORMAL,
            LinuxRuntimeSafetyMode.RECOVERY_ARMED ->
                true

            LinuxRuntimeSafetyMode.SAFE_MODE ->
                false
        }
    }

    fun isRecoveryArmed():
            Boolean {

        return getSnapshot()
            .mode ==
                LinuxRuntimeSafetyMode.RECOVERY_ARMED
    }

    /*
     * Fail-closed runtime trapdoor.
     *
     * This intentionally:
     *
     * - records the failure
     * - stops PRoot
     * - removes transient runtime files
     * - preserves the Ubuntu rootfs
     * - preserves user files
     * - preserves package metadata
     * - preserves the safety record
     *
     * It does NOT uninstall Atlas and does NOT erase
     * forensic evidence.
     */
    @Synchronized
    fun trip(
        reason: LinuxRuntimeSafetyReason,
        message: String
    ): LinuxRuntimeSafetySnapshot {

        val initialSnapshot =
            LinuxRuntimeSafetySnapshot(
                mode =
                    LinuxRuntimeSafetyMode.SAFE_MODE,

                reason =
                    reason,

                message =
                    message.trim()
                        .ifBlank {
                            "Atlas Linux runtime entered safe mode."
                        },

                trippedAtEpochMillis =
                    System.currentTimeMillis(),

                transientCleanupSucceeded =
                    null
            )

        cachedSnapshot =
            initialSnapshot

        persist(
            initialSnapshot
        )

        /*
         * Stop the guest before transient cleanup.
         *
         * ProotLinuxRuntimeBackend.stop() already attempts
         * a normal termination and then force-stops the
         * native runtime if it remains alive.
         */
        runCatching {

            ProotLinuxRuntimeBackend
                .stop()
        }

        /*
         * Keep Atlas' transient repository state aligned
         * with the real process state after the trapdoor
         * fires. Never claim STOPPED while PRoot is still
         * alive.
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
            initialSnapshot
                .copy(
                    transientCleanupSucceeded =
                        cleanupSucceeded
                )

        cachedSnapshot =
            finalSnapshot

        persist(
            finalSnapshot
        )

        return finalSnapshot
    }

    /*
     * Recovery is deliberate.
     *
     * SAFE_MODE blocks normal PRoot startup.
     * RECOVERY_ARMED allows the runtime to start, while
     * LinuxGuestCommandExecutor restricts the guest to
     * recovery-safe commands until package/runtime health
     * is verified.
     */
    @Synchronized
    fun armRecovery():
            LinuxRuntimeSafetySnapshot {

        val current =
            getSnapshot()

        if (
            !current.tripped
        ) {

            return current
        }

        val armed =
            current.copy(
                mode =
                    LinuxRuntimeSafetyMode.RECOVERY_ARMED
            )

        cachedSnapshot =
            armed

        persist(
            armed
        )

        return armed
    }

    /*
     * Called only after an explicit recovery command has
     * proved that package/runtime health is clean.
     */
    @Synchronized
    fun resetAfterVerifiedRecovery() {

        cachedSnapshot =
            LinuxRuntimeSafetySnapshot()

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
     * This never repairs anything. It only clears the
     * safety latch, so the terminal requires --force.
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
                    if (snapshot.tripped) {
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
                ?.takeIf {
                    it.isNotBlank()
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
                            if (cleaned) {
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
            LinuxRuntimeSafetySnapshot(
                mode =
                    LinuxRuntimeSafetyMode.SAFE_MODE,

                reason =
                    LinuxRuntimeSafetyReason.RUNTIME_INTEGRITY_FAILURE,

                message =
                    "Atlas could not read the runtime safety record.",

                trippedAtEpochMillis =
                    System.currentTimeMillis(),

                transientCleanupSucceeded =
                    null
            )
        }
    }
}
