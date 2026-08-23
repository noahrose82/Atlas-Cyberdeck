package com.noahrose.pocketlab.feature.linux.runtime.process

sealed interface LinuxProcessLaunchResult {

    data class Success(
        val process: LinuxProcessHandle
    ) : LinuxProcessLaunchResult

    data class Failure(
        val message: String,
        val cause: Throwable? = null
    ) : LinuxProcessLaunchResult
}