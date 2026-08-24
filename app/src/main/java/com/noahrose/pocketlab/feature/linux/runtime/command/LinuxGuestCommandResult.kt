package com.noahrose.pocketlab.feature.linux.runtime.command

sealed interface LinuxGuestCommandResult {

    data class Success(
        val output: String,
        val errorOutput: String,
        val exitCode: Int
    ) : LinuxGuestCommandResult

    data class Failure(
        val message: String,
        val output: String = "",
        val errorOutput: String = ""
    ) : LinuxGuestCommandResult
}