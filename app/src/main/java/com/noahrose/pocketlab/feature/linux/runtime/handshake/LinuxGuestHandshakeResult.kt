package com.noahrose.pocketlab.feature.linux.runtime.handshake

sealed interface LinuxGuestHandshakeResult {

    data class Success(
        val output: String,
        val errorOutput: String
    ) : LinuxGuestHandshakeResult

    data class Failure(
        val message: String,
        val output: String = "",
        val errorOutput: String = ""
    ) : LinuxGuestHandshakeResult
}