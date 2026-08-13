package com.noahrose.pocketlab.feature.terminal.execution

object ExecutionStatus {

    private var lastExitCode: Int = 0

    fun set(
        exitCode: Int
    ) {
        lastExitCode = exitCode
    }

    fun get(): Int {
        return lastExitCode
    }

    fun wasSuccessful(): Boolean {
        return lastExitCode == 0
    }
}