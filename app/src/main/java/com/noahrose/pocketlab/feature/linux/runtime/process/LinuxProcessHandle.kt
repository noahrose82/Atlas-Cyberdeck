package com.noahrose.pocketlab.feature.linux.runtime.process

class LinuxProcessHandle(
    private val process: Process
) {

    /*
     * Android's java.lang.Process API does not
     * provide a portable PID accessor here.
     *
     * A future native/rootless backend can expose
     * the actual process ID when available.
     */
    val processId: Long? =
        null

    val isAlive: Boolean
        get() =
            process.isAlive

    val inputStream
        get() =
            process.inputStream

    val errorStream
        get() =
            process.errorStream

    val outputStream
        get() =
            process.outputStream

    fun stop() {

        if (!process.isAlive) {
            return
        }

        process.destroy()
    }

    fun forceStop() {

        if (!process.isAlive) {
            return
        }

        process.destroyForcibly()
    }
}