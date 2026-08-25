package com.noahrose.pocketlab.feature.linux.runtime.command

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class LinuxPackageCommandPolicyTest {

    @Test
    fun normalLinuxCommandRemainsUntouched() {

        val original =
            "python3 --version"

        val result =
            LinuxPackageCommandPolicy
                .prepare(
                    original
                )

        when (
            result
        ) {

            is LinuxPackageCommandPreparation.Ready -> {

                assertFalse(
                    result.hardened
                )

                assertTrue(
                    result.command == original
                )
            }

            is LinuxPackageCommandPreparation.Blocked -> {

                fail(
                    "Normal Linux command was blocked."
                )
            }
        }
    }

    @Test
    fun aptUpdateReceivesNonInteractiveEnvironment() {

        val result =
            LinuxPackageCommandPolicy
                .prepare(
                    "apt update"
                )

        when (
            result
        ) {

            is LinuxPackageCommandPreparation.Ready -> {

                assertTrue(
                    result.hardened
                )

                assertTrue(
                    result.command.contains(
                        "DEBIAN_FRONTEND=noninteractive"
                    )
                )

                assertTrue(
                    result.command.contains(
                        "TZ=Etc/UTC"
                    )
                )
            }

            is LinuxPackageCommandPreparation.Blocked -> {

                fail(
                    "apt update should not be blocked."
                )
            }
        }
    }

    @Test
    fun aptInstallWithAssumeYesIsAllowed() {

        val result =
            LinuxPackageCommandPolicy
                .prepare(
                    "apt install -y python3"
                )

        assertTrue(
            result is
                    LinuxPackageCommandPreparation.Ready
        )
    }

    @Test
    fun aptInstallWithoutAssumeYesIsBlocked() {

        val result =
            LinuxPackageCommandPolicy
                .prepare(
                    "apt install python3"
                )

        assertTrue(
            result is
                    LinuxPackageCommandPreparation.Blocked
        )
    }

    @Test
    fun aptGetLongAssumeYesIsAllowed() {

        val result =
            LinuxPackageCommandPolicy
                .prepare(
                    "apt-get --assume-yes remove python3"
                )

        assertTrue(
            result is
                    LinuxPackageCommandPreparation.Ready
        )
    }

    @Test
    fun dpkgConfigureIsHardenedWithoutAssumeYesRequirement() {

        val result =
            LinuxPackageCommandPolicy
                .prepare(
                    "dpkg --configure -a"
                )

        when (
            result
        ) {

            is LinuxPackageCommandPreparation.Ready -> {

                assertTrue(
                    result.hardened
                )
            }

            is LinuxPackageCommandPreparation.Blocked -> {

                fail(
                    "dpkg --configure -a should not be blocked."
                )
            }
        }
    }

    @Test
    fun quotedPackageTextIsNotTreatedAsPackageCommand() {

        val result =
            LinuxPackageCommandPolicy
                .prepare(
                    "echo 'apt install python3'"
                )

        when (
            result
        ) {

            is LinuxPackageCommandPreparation.Ready -> {

                assertFalse(
                    result.hardened
                )
            }

            is LinuxPackageCommandPreparation.Blocked -> {

                fail(
                    "Quoted text was incorrectly classified as apt."
                )
            }
        }
    }

    @Test
    fun chainedPackageCommandIsDetected() {

        val result =
            LinuxPackageCommandPolicy
                .prepare(
                    "echo ready && apt install -y curl"
                )

        when (
            result
        ) {

            is LinuxPackageCommandPreparation.Ready -> {

                assertTrue(
                    result.hardened
                )
            }

            is LinuxPackageCommandPreparation.Blocked -> {

                fail(
                    "Chained apt command with -y should be allowed."
                )
            }
        }
    }

    @Test
    fun chainedInteractiveInstallIsBlocked() {

        val result =
            LinuxPackageCommandPolicy
                .prepare(
                    "apt update && apt install curl"
                )

        assertTrue(
            result is
                    LinuxPackageCommandPreparation.Blocked
        )
    }
}