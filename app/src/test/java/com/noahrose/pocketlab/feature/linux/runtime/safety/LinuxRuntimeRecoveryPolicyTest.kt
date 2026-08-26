package com.noahrose.pocketlab.feature.linux.runtime.safety

import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LinuxRuntimeRecoveryPolicyTest {

    @Test
    fun recoveryModeAllowsDpkgRepair() {

        assertTrue(
            LinuxRuntimeRecoveryPolicy
                .isAllowedInRecovery(
                    "dpkg --configure -a"
                )
        )
    }

    @Test
    fun recoveryModeAllowsFixBroken() {

        assertTrue(
            LinuxRuntimeRecoveryPolicy
                .isAllowedInRecovery(
                    "apt --fix-broken install -y"
                )
        )
    }

    @Test
    fun recoveryModeAllowsAuditForDiagnostics() {

        assertTrue(
            LinuxRuntimeRecoveryPolicy
                .isAllowedInRecovery(
                    "dpkg --audit"
                )
        )
    }

    @Test
    fun recoveryModeBlocksNormalPackageInstall() {

        assertFalse(
            LinuxRuntimeRecoveryPolicy
                .isAllowedInRecovery(
                    "apt install -y python3"
                )
        )
    }

    @Test
    fun recoveryModeBlocksArbitraryShellMutation() {

        assertFalse(
            LinuxRuntimeRecoveryPolicy
                .isAllowedInRecovery(
                    "rm -rf /tmp/example"
                )
        )
    }

    @Test
    fun packageHealthWarningTripsPolicy() {

        assertNotNull(
            LinuxRuntimeRecoveryPolicy
                .detectPackageIntegrityFailure(
                    "Atlas package health warning:\npackage is incomplete"
                )
        )
    }

    @Test
    fun cleanPackageHealthDoesNotTripPolicy() {

        assertNull(
            LinuxRuntimeRecoveryPolicy
                .detectPackageIntegrityFailure(
                    "Atlas package health: CLEAN"
                )
        )
    }

    @Test
    fun cleanDpkgAuditAloneDoesNotVerifyRecovery() {

        assertFalse(
            LinuxRuntimeRecoveryPolicy
                .recoveryVerified(
                    command =
                        "dpkg --audit",

                    output =
                        "",

                    errorOutput =
                        "",

                    exitCode =
                        0
                )
        )
    }

    @Test
    fun successfulDpkgConfigureWithCleanAuditVerifiesRecovery() {

        assertTrue(
            LinuxRuntimeRecoveryPolicy
                .recoveryVerified(
                    command =
                        "dpkg --configure -a",

                    output =
                        "",

                    errorOutput =
                        "Atlas package health: CLEAN",

                    exitCode =
                        0
                )
        )
    }

    @Test
    fun failedDpkgConfigureNeverVerifiesRecovery() {

        assertFalse(
            LinuxRuntimeRecoveryPolicy
                .recoveryVerified(
                    command =
                        "dpkg --configure -a",

                    output =
                        "",

                    errorOutput =
                        "Atlas package health: CLEAN",

                    exitCode =
                        1
                )
        )
    }

    @Test
    fun auditIsNotARepairOperation() {

        assertFalse(
            LinuxRuntimeRecoveryPolicy
                .isRepairOperation(
                    "dpkg --audit"
                )
        )
    }
}
