package com.noahrose.pocketlab.feature.linux.runtime.process

interface LinuxProcessLauncher {

    fun launch(
        spec: LinuxProcessSpec
    ): LinuxProcessLaunchResult
}