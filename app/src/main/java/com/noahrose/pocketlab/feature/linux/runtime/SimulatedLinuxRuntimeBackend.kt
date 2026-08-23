package com.noahrose.pocketlab.feature.linux.runtime

object SimulatedLinuxRuntimeBackend :
    LinuxRuntimeBackend {

    override fun start(): LinuxRuntimeBackendResult {

        val session =
            LinuxRuntimeSession(
                processId = null,
                startedAtEpochMillis =
                    System.currentTimeMillis(),
                workingDirectory = null
            )

        return LinuxRuntimeBackendResult.Success(
            session = session
        )
    }

    override fun stop(): LinuxRuntimeBackendResult {

        return LinuxRuntimeBackendResult.Success()
    }
}