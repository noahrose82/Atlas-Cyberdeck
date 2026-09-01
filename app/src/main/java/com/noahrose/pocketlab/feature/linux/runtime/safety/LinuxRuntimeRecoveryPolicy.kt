package com.noahrose.pocketlab.feature.linux.runtime.safety

object LinuxRuntimeRecoveryPolicy {

    /*
     * Debian package names permitted by read-only
     * recovery diagnostics.
     */
    private val packageNamePattern =
        Regex(
            "^[a-z0-9][a-z0-9+.-]*(?::[a-z0-9][a-z0-9-]*)?$"
        )

    /*
     * Safe read-only commands available while
     * RECOVERY_ARMED is active.
     */
    private val exactReadOnlyCommands =
        setOf(
            "pwd",
            "whoami",
            "id",

            "uname",
            "uname -a",
            "uname -m",
            "uname -n",
            "uname -r",
            "uname -s",

            "cat /etc/os-release"
        )

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

            normalized in
                    exactReadOnlyCommands ->
                true

            /*
             * Package database audit is diagnostic only.
             *
             * It cannot clear the recovery latch.
             */
            normalized ==
                    "dpkg --audit" ->
                true

            /*
             * Safe package metadata inspection.
             */
            isReadOnlyPackageDiagnostic(
                command
            ) ->
                true

            /*
             * Explicit package repair operations.
             */
            isRepairOperation(
                command
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
     * Read-only package diagnostics permitted during
     * controlled recovery.
     *
     * Examples:
     *
     * dpkg -s libc6
     * dpkg --status perl-base
     * dpkg -L vim-runtime
     * dpkg --listfiles nano
     * dpkg -V perl-base
     * dpkg --verify libc6
     */
    private fun isReadOnlyPackageDiagnostic(
        command: String
    ): Boolean {

        val normalizedWhitespace =
            normalizeWhitespace(
                command
            )

        val parts =
            normalizedWhitespace
                .split(" ")

        if (
            parts.size != 3
        ) {
            return false
        }

        val executable =
            parts[0]

        val option =
            parts[1]

        val packageName =
            parts[2]

        if (
            !executable.equals(
                "dpkg",
                ignoreCase = true
            )
        ) {
            return false
        }

        /*
         * dpkg short options are case-sensitive.
         */
        val allowedOption =
            option ==
                    "-s" ||

                    option ==
                    "--status" ||

                    option ==
                    "-L" ||

                    option ==
                    "--listfiles" ||

                    option ==
                    "-V" ||

                    option ==
                    "--verify"

        if (
            !allowedOption
        ) {
            return false
        }

        /*
         * Require one valid Debian-style package name.
         *
         * This prevents paths, redirection, pipes,
         * substitutions, and additional shell syntax
         * from passing through the recovery policy.
         */
        return packageName ==
                packageName.lowercase() &&
                packageNamePattern.matches(
                    packageName
                )
    }

    /*
     * Detect package-integrity failures produced by
     * Atlas package transaction auditing.
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
     * 1. an approved repair operation ran,
     * 2. that operation exited successfully, and
     * 3. Atlas' automatic package audit reported CLEAN.
     *
     * Read-only diagnostics and dpkg --audit alone
     * can never clear recovery mode.
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

        return normalizeWhitespace(
            command
        ).lowercase()
    }

    private fun normalizeWhitespace(
        command: String
    ): String {

        return command
            .trim()
            .replace(
                Regex("\\s+"),
                " "
            )
    }
}