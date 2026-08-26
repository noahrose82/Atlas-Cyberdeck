package com.noahrose.pocketlab.feature.linux.runtime.safety

object LinuxRuntimeRecoveryPolicy {

    fun isAllowedInRecovery(
        command: String
    ): Boolean {

        val normalized =
            normalize(
                command
            )

        if (
            normalized.isBlank()
        ) {

            return false
        }

        return when {

            normalized ==
                    "pwd" ->
                true

            normalized ==
                    "whoami" ->
                true

            normalized ==
                    "id" ->
                true

            normalized ==
                    "uname" ||
                    normalized.startsWith(
                        "uname "
                    ) ->
                true

            normalized ==
                    "cat /etc/os-release" ->
                true

            /*
             * Audit is allowed for diagnostics, but it
             * MUST NOT clear recovery mode by itself.
             *
             * A clean dpkg database does not prove that
             * the preceding repair operation succeeded.
             */
            normalized ==
                    "dpkg --audit" ->
                true

            isRepairOperation(
                normalized
            ) ->
                true

            else ->
                false
        }
    }

    fun isRepairOperation(
        command: String
    ): Boolean {

        val normalized =
            normalize(
                command
            )

        return normalized ==
                "dpkg --configure -a" ||
                normalized ==
                "dpkg --configure --pending" ||
                normalized ==
                "apt --fix-broken install -y" ||
                normalized ==
                "apt --fix-broken install --assume-yes" ||
                normalized ==
                "apt-get -f install -y" ||
                normalized ==
                "apt-get --fix-broken install -y" ||
                normalized ==
                "apt-get --fix-broken install --assume-yes"
    }

    fun isAuditOnly(
        command: String
    ): Boolean {

        return normalize(
            command
        ) ==
                "dpkg --audit"
    }

    /*
     * Detect a post-transaction package integrity failure
     * emitted by LinuxPackageCommandPolicy.
     *
     * A package command can fail while dpkg --audit is
     * still clean. That is NOT an integrity failure; the
     * original non-zero command exit code remains the
     * authoritative transaction result.
     */
    fun detectPackageIntegrityFailure(
        errorOutput: String
    ): String? {

        val text =
            errorOutput.trim()

        if (
            text.contains(
                "Atlas package health check failed",
                ignoreCase = true
            )
        ) {

            return "dpkg audit failed after a package transaction."
        }

        if (
            text.contains(
                "Atlas package health warning:",
                ignoreCase = true
            )
        ) {

            return "dpkg reported incomplete package state after a package transaction."
        }

        return null
    }

    /*
     * Recovery may clear ONLY when:
     *
     * 1. the command was an actual repair operation,
     * 2. that repair command exited successfully, and
     * 3. Atlas' automatic post-transaction dpkg audit
     *    reported CLEAN.
     *
     * `dpkg --audit` by itself is diagnostic only and
     * can never clear the recovery latch.
     */
    fun recoveryVerified(
        command: String,
        output: String,
        errorOutput: String,
        exitCode: Int
    ): Boolean {

        if (
            exitCode != 0
        ) {

            return false
        }

        if (
            !isRepairOperation(
                command
            )
        ) {

            return false
        }

        return errorOutput
            .contains(
                "Atlas package health: CLEAN",
                ignoreCase = true
            )
    }

    private fun normalize(
        command: String
    ): String {

        return command
            .trim()
            .replace(
                Regex("\\s+"),
                " "
            )
            .lowercase()
    }
}
