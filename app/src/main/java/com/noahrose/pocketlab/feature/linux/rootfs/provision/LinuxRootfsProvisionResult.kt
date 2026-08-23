package com.noahrose.pocketlab.feature.linux.rootfs.provision

sealed interface LinuxRootfsProvisionResult {

    data object Success :
        LinuxRootfsProvisionResult

    data class Failure(
        val message: String
    ) : LinuxRootfsProvisionResult
}