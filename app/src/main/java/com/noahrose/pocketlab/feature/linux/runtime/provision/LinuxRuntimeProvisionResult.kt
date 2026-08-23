package com.noahrose.pocketlab.feature.linux.runtime.provision

sealed interface LinuxRuntimeProvisionResult {

    data object Success :
        LinuxRuntimeProvisionResult

    data class Failure(
        val message: String,
        val cause: Throwable? = null
    ) : LinuxRuntimeProvisionResult
}