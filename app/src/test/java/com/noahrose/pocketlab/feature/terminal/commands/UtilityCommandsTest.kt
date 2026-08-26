package com.noahrose.pocketlab.feature.terminal.commands

import java.io.File
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class UtilityCommandsTest {

    /*
     * ------------------------------------------------
     * H5A — LINUX COMMAND CONTRACT
     * ------------------------------------------------
     *
     * These are source-contract tests on purpose.
     *
     * UtilityCommands touches Android/runtime-backed
     * singletons (LinuxRuntimePathManager, the circuit
     * breaker, and PRoot state). Plain JVM unit tests do
     * not have an Android Context, so invoking those
     * singletons directly causes IllegalStateException.
     *
     * H5A's job is narrower: prevent the terminal command
     * surface from silently losing "linux shell" again.
     */
    @Test
    fun linuxCommandContractStillContainsShell() {

        val source =
            utilityCommandsSource()

        assertTrue(
            source.contains(
                "\"shell\" ->"
            )
        )

        assertTrue(
            source.contains(
                "LinuxShellMode"
            )
        )

        assertTrue(
            source.contains(
                ".enter()"
            )
        )

        assertTrue(
            source.contains(
                "Usage: linux [status|start|stop|shell]"
            )
        )

        assertFalse(
            source.contains(
                "Usage: linux [status|start|stop]\""
            )
        )
    }

    @Test
    fun linuxShellContractRetainsSafeModeBlock() {

        val source =
            utilityCommandsSource()

        assertTrue(
            source.contains(
                "LinuxRuntimeSafetyMode.SAFE_MODE"
            )
        )

        assertTrue(
            source.contains(
                "linux: Ubuntu shell blocked by Atlas Safe Mode."
            )
        )

        assertTrue(
            source.contains(
                "Run 'safety recover' to begin controlled recovery."
            )
        )
    }

    @Test
    fun linuxShellContractRetainsNormalEntryMessage() {

        val source =
            utilityCommandsSource()

        assertTrue(
            source.contains(
                "Ubuntu shell mode enabled."
            )
        )

        assertTrue(
            source.contains(
                "Type 'exit' to return to Atlas."
            )
        )
    }

    @Test
    fun linuxShellContractRetainsRecoveryEntryMessage() {

        val source =
            utilityCommandsSource()

        assertTrue(
            source.contains(
                "LinuxRuntimeSafetyMode.RECOVERY_ARMED"
            )
        )

        assertTrue(
            source.contains(
                "Ubuntu recovery shell mode enabled."
            )
        )

        assertTrue(
            source.contains(
                "Only approved recovery and diagnostic commands are allowed."
            )
        )
    }

    @Test
    fun linuxStartContractRetainsSafeModeResult() {

        val source =
            utilityCommandsSource()

        assertTrue(
            source.contains(
                "LinuxRuntimeControlResult.SAFE_MODE_BLOCKED"
            )
        )

        assertTrue(
            source.contains(
                "linux: runtime startup blocked by Atlas Safe Mode."
            )
        )
    }

    /*
     * Gradle normally executes this test with either the
     * project root or the :app module as user.dir.
     *
     * Walk upward so the test remains stable in Android
     * Studio, PowerShell, and CI.
     */
    private fun utilityCommandsSource():
            String {

        val relativePath =
            "src/main/java/" +
                    "com/noahrose/pocketlab/" +
                    "feature/terminal/commands/" +
                    "UtilityCommands.kt"

        var directory:
                File? =
            File(
                System.getProperty(
                    "user.dir"
                )
            )
                .absoluteFile

        repeat(
            8
        ) {

            val current =
                directory
                    ?: return@repeat

            val moduleCandidate =
                File(
                    current,
                    relativePath
                )

            if (
                moduleCandidate.exists() &&
                moduleCandidate.isFile
            ) {

                return moduleCandidate
                    .readText()
            }

            val projectCandidate =
                File(
                    current,
                    "app/$relativePath"
                )

            if (
                projectCandidate.exists() &&
                projectCandidate.isFile
            ) {

                return projectCandidate
                    .readText()
            }

            directory =
                current.parentFile
        }

        throw AssertionError(
            "Unable to locate UtilityCommands.kt from " +
                    System.getProperty(
                        "user.dir"
                    )
        )
    }
}
