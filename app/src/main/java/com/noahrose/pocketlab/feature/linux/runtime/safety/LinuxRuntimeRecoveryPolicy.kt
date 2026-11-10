package com.noahrose.pocketlab.feature.linux.runtime.safety

/** Strict allowlist for the PRoot guest while Atlas is in controlled recovery. */
object LinuxRuntimeRecoveryPolicy {
    private val packageNamePattern =
        Regex("^[a-z0-9][a-z0-9+.-]*(?::[a-z0-9][a-z0-9-]*)?$")

    private val exactReadOnlyCommands = setOf(
        "pwd", "whoami", "id", "uname", "uname -a", "uname -m",
        "uname -n", "uname -r", "uname -s", "cat /etc/os-release"
    )

    private val exactRepairCommands = setOf(
        "dpkg --configure -a",
        "dpkg --configure --pending",
        "dpkg --configure ca-certificates",
        "apt --fix-broken install -y",
        "apt --fix-broken install --assume-yes",
        "apt-get -f install -y",
        "apt-get --fix-broken install -y",
        "apt-get --fix-broken install --assume-yes"
    )

    fun isAllowedInRecovery(command: String): Boolean {
        val normalized = normalize(command)
        if (normalized.isBlank()) return false
        return normalized in exactReadOnlyCommands ||
                normalized == "dpkg --audit" ||
                isReadOnlyPackageDiagnostic(command) ||
                isRepairOperation(command)
    }

    fun isRepairOperation(command: String): Boolean =
        normalize(command) in exactRepairCommands

    fun isAuditOnly(command: String): Boolean =
        normalize(command) == "dpkg --audit"

    private fun isReadOnlyPackageDiagnostic(command: String): Boolean {
        val parts = normalizeWhitespace(command).split(" ")
        if (parts.size != 3 || parts[0] != "dpkg") return false
        if (parts[1] !in setOf("-s", "--status", "-L", "--listfiles", "-V", "--verify")) {
            return false
        }
        val packageName = parts[2]
        return packageName == packageName.lowercase() &&
                packageNamePattern.matches(packageName)
    }

    fun detectPackageIntegrityFailure(errorOutput: String): String? {
        val text = errorOutput.trim()
        return when {
            text.contains("Atlas package health check failed", ignoreCase = true) ->
                "dpkg audit failed after a package transaction."
            text.contains("Atlas package health warning:", ignoreCase = true) ->
                "dpkg reported incomplete package state after a package transaction."
            else -> null
        }
    }

    /** Never clear RECOVERY_ARMED on an audit alone or a failed repair. */
    fun recoveryVerified(
        command: String,
        output: String,
        errorOutput: String,
        exitCode: Int
    ): Boolean {
        if (exitCode != 0 || !isRepairOperation(command)) return false
        if (detectPackageIntegrityFailure(errorOutput) != null) return false
        return errorOutput.lineSequence().any {
            it.trim().equals("Atlas package health: CLEAN", ignoreCase = true)
        }
    }

    private fun normalize(command: String): String =
        normalizeWhitespace(command).lowercase()

    private fun normalizeWhitespace(command: String): String =
        command.trim().replace(Regex("\\s+"), " ")
}
