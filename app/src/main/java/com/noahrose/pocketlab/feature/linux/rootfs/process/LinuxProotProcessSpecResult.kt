package com.noahrose.pocketlab.feature.linux.runtime.process

sealed interface LinuxProotProcessSpecResult {

    data class Ready(
        val spec: LinuxProcessSpec
    ) : LinuxProotProcessSpecResult

    data class Failure(
        val message: String
    ) : LinuxProotProcessSpecResult
}