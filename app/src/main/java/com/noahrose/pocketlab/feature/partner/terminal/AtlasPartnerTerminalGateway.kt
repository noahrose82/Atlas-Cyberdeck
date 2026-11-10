package com.noahrose.pocketlab.feature.partner.terminal

import com.noahrose.pocketlab.feature.linux.runtime.command.LinuxGuestCommandExecutor
import com.noahrose.pocketlab.feature.linux.runtime.command.LinuxGuestCommandResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Phase 2: supervised, read-only commands for Atlas Partner.
 *
 * Never pass an AI-generated command string to the shell. The approved command
 * is selected from this fixed list. The calling UI must display the command and
 * receive an explicit user confirmation before calling executeApproved().
 *
 * The existing LinuxGuestCommandExecutor continues to enforce runtime safety.
 */
enum class AtlasReadOnlyCommand(val command: String, val description: String) {
    PRINT_WORKING_DIRECTORY("pwd", "Show the current Ubuntu directory"),
    CURRENT_USER("whoami", "Show the current Ubuntu user"),
    SYSTEM_INFO("uname -a", "Show the Ubuntu kernel information"),
    UBUNTU_VERSION("cat /etc/os-release", "Show the Ubuntu release details")
}

data class AtlasTerminalExecutionResult(
    val command: String,
    val succeeded: Boolean,
    val output: String,
    val error: String,
    val exitCode: Int?,
    val message: String
)

object AtlasPartnerTerminalGateway {
    /** Call only after the user approves the exact displayed command. */
    suspend fun executeApproved(
        approvedCommand: AtlasReadOnlyCommand
    ): AtlasTerminalExecutionResult = withContext(Dispatchers.IO) {
        when (val result = LinuxGuestCommandExecutor.execute(approvedCommand.command)) {
            is LinuxGuestCommandResult.Success -> AtlasTerminalExecutionResult(
                command = approvedCommand.command,
                succeeded = result.exitCode == 0,
                output = result.output,
                error = result.errorOutput,
                exitCode = result.exitCode,
                message = if (result.exitCode == 0) "Command completed." else "Command exited with code ${result.exitCode}."
            )

            is LinuxGuestCommandResult.Failure -> AtlasTerminalExecutionResult(
                command = approvedCommand.command,
                succeeded = false,
                output = result.output,
                error = result.errorOutput,
                exitCode = null,
                message = result.message
            )
        }
    }
}
