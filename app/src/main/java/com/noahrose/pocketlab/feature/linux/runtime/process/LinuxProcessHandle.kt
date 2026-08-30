package com.noahrose.pocketlab.feature.linux.runtime.process

import java.io.InputStream
import java.io.OutputStream
import java.util.concurrent.TimeUnit

class LinuxProcessHandle(
    private val process: Process
) {

    companion object {

        /*
         * Give graceful termination a short opportunity
         * to complete before Atlas escalates.
         */
        private const val NORMAL_STOP_TIMEOUT_MS =
            1_000L

        /*
         * Forced termination may also require a short
         * period before Android reports the process dead.
         */
        private const val FORCE_STOP_TIMEOUT_MS =
            2_000L
    }

    /*
     * Android's java.lang.Process implementation used by
     * the current Atlas runtime does not expose a portable
     * PID through this abstraction.
     */
    val processId: Long? =
        null

    val isAlive: Boolean
        get() =
            process.isAlive

    val inputStream: InputStream
        get() =
            process.inputStream

    val errorStream: InputStream
        get() =
            process.errorStream

    val outputStream: OutputStream
        get() =
            process.outputStream

    /*
     * Request graceful process termination.
     *
     * Process.destroy() does not guarantee that the
     * process has exited when the method returns.
     */
    fun stop() {

        if (
            !process.isAlive
        ) {
            return
        }

        process.destroy()

        waitForExit(
            timeoutMillis =
                NORMAL_STOP_TIMEOUT_MS
        )
    }

    /*
     * Force process termination and allow Android a short
     * period to update the process state.
     */
    fun forceStop() {

        if (
            !process.isAlive
        ) {
            return
        }

        process.destroyForcibly()

        waitForExit(
            timeoutMillis =
                FORCE_STOP_TIMEOUT_MS
        )
    }

    /*
     * Wait for termination without ever blocking
     * indefinitely.
     */
    private fun waitForExit(
        timeoutMillis: Long
    ): Boolean {

        if (
            !process.isAlive
        ) {
            return true
        }

        return try {

            process.waitFor(
                timeoutMillis,
                TimeUnit.MILLISECONDS
            )

        } catch (
            _: InterruptedException
        ) {

            /*
             * Restore the interrupted state.
             */
            Thread.currentThread()
                .interrupt()

            !process.isAlive
        }
    }
}