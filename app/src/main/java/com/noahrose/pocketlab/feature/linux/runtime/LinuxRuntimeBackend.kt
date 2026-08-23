package com.noahrose.pocketlab.feature.linux.runtime

interface LinuxRuntimeBackend {

    fun start(): LinuxRuntimeBackendResult

    fun stop(): LinuxRuntimeBackendResult
}

sealed interface LinuxRuntimeBackendResult {

    data class Success(
        val session: LinuxRuntimeSession? = null
    ) : LinuxRuntimeBackendResult

    data class Failure(
        val message: String
    ) : LinuxRuntimeBackendResult
}