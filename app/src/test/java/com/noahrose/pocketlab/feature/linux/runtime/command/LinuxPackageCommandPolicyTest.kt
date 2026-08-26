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

    @Test
    fun aptInstallReceivesPostTransactionDpkgAudit() {

        val result =
            LinuxPackageCommandPolicy
                .prepare(
                    "apt install -y python3"
                )

        when (
            result
        ) {

            is LinuxPackageCommandPreparation.Ready -> {

                assertTrue(
                    result.command.contains(
                        "dpkg --audit"
                    )
                )

                assertTrue(
                    result.command.contains(
                        "__atlas_pkg_exit"
                    )
                )

                assertTrue(
                    result.command.contains(
                        "Atlas package health warning"
                    )
                )

                assertTrue(
                    result.command.contains(
                        "Atlas package health: CLEAN"
                    )
                )
            }

            is LinuxPackageCommandPreparation.Blocked -> {

                fail(
                    "apt install -y should be allowed and audited."
                )
            }
        }
    }

    @Test
    fun aptUpdateDoesNotReceivePackageDatabaseAudit() {

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

                assertFalse(
                    result.command.contains(
                        "__atlas_pkg_audit="
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
    fun dpkgConfigureReceivesPostTransactionAudit() {

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
                    result.command.contains(
                        "__atlas_pkg_audit="
                    )
                )
            }

            is LinuxPackageCommandPreparation.Blocked -> {

                fail(
                    "dpkg --configure -a should be allowed."
                )
            }
        }
    }

    @Test
    fun dpkgAuditDoesNotRecursivelyAddPostTransactionAudit() {

        val result =
            LinuxPackageCommandPolicy
                .prepare(
                    "dpkg --audit"
                )

        when (
            result
        ) {

            is LinuxPackageCommandPreparation.Ready -> {

                assertTrue(
                    result.hardened
                )

                assertFalse(
                    result.command.contains(
                        "__atlas_pkg_audit="
                    )
                )
            }

            is LinuxPackageCommandPreparation.Blocked -> {

                fail(
                    "dpkg --audit should not be blocked."
                )
            }
        }
    }

    @Test
    fun dpkgInstallReceivesPostTransactionAudit() {

        val result =
            LinuxPackageCommandPolicy
                .prepare(
                    "dpkg -i package.deb"
                )

        when (
            result
        ) {

            is LinuxPackageCommandPreparation.Ready -> {

                assertTrue(
                    result.command.contains(
                        "__atlas_pkg_audit="
                    )
                )
            }

            is LinuxPackageCommandPreparation.Blocked -> {

                fail(
                    "dpkg -i should not be blocked."
                )
            }
        }
    }


    @Test
    fun normalAptMutationReceivesPreTransactionHealthGate() {

        val result =
            LinuxPackageCommandPolicy
                .prepare(
                    "apt install -y python3"
                )

        when (
            result
        ) {

            is LinuxPackageCommandPreparation.Ready -> {

                assertTrue(
                    result.command.contains(
                        "__atlas_pkg_pre_audit="
                    )
                )

                assertTrue(
                    result.command.contains(
                        "Atlas package preflight: CLEAN"
                    )
                )

                assertTrue(
                    result.command.contains(
                        "Atlas package preflight: BLOCKED"
                    )
                )
            }

            is LinuxPackageCommandPreparation.Blocked -> {

                fail(
                    "apt install -y should reach the package health gate."
                )
            }
        }
    }

    @Test
    fun aptUpdateDoesNotReceivePreTransactionHealthGate() {

        val result =
            LinuxPackageCommandPolicy
                .prepare(
                    "apt update"
                )

        when (
            result
        ) {

            is LinuxPackageCommandPreparation.Ready -> {

                assertFalse(
                    result.command.contains(
                        "__atlas_pkg_pre_audit="
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
    fun dpkgConfigureBypassesPreflightSoItCanRepairDegradedState() {

        val result =
            LinuxPackageCommandPolicy
                .prepare(
                    "dpkg --configure -a"
                )

        when (
            result
        ) {

            is LinuxPackageCommandPreparation.Ready -> {

                assertFalse(
                    result.command.contains(
                        "__atlas_pkg_pre_audit="
                    )
                )

                assertTrue(
                    result.command.contains(
                        "__atlas_pkg_audit="
                    )
                )
            }

            is LinuxPackageCommandPreparation.Blocked -> {

                fail(
                    "dpkg --configure -a must remain available for recovery."
                )
            }
        }
    }

    @Test
    fun aptFixBrokenInstallBypassesPreflightButKeepsPostAudit() {

        val result =
            LinuxPackageCommandPolicy
                .prepare(
                    "apt --fix-broken install -y"
                )

        when (
            result
        ) {

            is LinuxPackageCommandPreparation.Ready -> {

                assertFalse(
                    result.command.contains(
                        "__atlas_pkg_pre_audit="
                    )
                )

                assertTrue(
                    result.command.contains(
                        "__atlas_pkg_audit="
                    )
                )
            }

            is LinuxPackageCommandPreparation.Blocked -> {

                fail(
                    "apt --fix-broken install -y must remain available for recovery."
                )
            }
        }
    }

}