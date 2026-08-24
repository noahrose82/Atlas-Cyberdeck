package com.noahrose.pocketlab.feature.linux.runtime

import com.noahrose.pocketlab.feature.linux.repository.LinuxRepository
import com.noahrose.pocketlab.feature.system.bootstrap.DeviceBootstrapManager
import com.noahrose.pocketlab.feature.system.capability.AtlasFeature

object LinuxRuntimeController {

    /*
     * F3P:
     *
     * The simulated backend has been replaced by
     * the real PRoot-backed Ubuntu runtime.
     */
    private val backend:
            LinuxRuntimeBackend =
        ProotLinuxRuntimeBackend

    private var activeSession:
            LinuxRuntimeSession? =
        null

    fun getSession():
            LinuxRuntimeSession? {

        /*
         * Do not expose a stale session if the
         * underlying PRoot process has already
         * exited.
         */
        if (
            backend ===
            ProotLinuxRuntimeBackend &&
            !ProotLinuxRuntimeBackend
                .isProcessAlive()
        ) {

            activeSession =
                null

            val installation =
                LinuxRepository
                    .getInstallation()

            if (installation.running) {

                LinuxRepository
                    .stopLinux()
            }
        }

        return activeSession
    }

    fun start():
            LinuxRuntimeControlResult {

        val installation =
            LinuxRepository
                .getInstallation()

        if (installation.isInstalling) {

            return LinuxRuntimeControlResult
                .INSTALLATION_IN_PROGRESS
        }

        if (!installation.installed) {

            return LinuxRuntimeControlResult
                .NOT_INSTALLED
        }

        /*
         * Reconcile a stale RUNNING flag before
         * deciding that Linux is already running.
         */
        if (installation.running) {

            if (
                ProotLinuxRuntimeBackend
                    .isProcessAlive()
            ) {

                return LinuxRuntimeControlResult
                    .ALREADY_RUNNING
            }

            LinuxRepository
                .stopLinux()

            activeSession =
                null
        }

        val linuxAvailable =
            DeviceBootstrapManager
                .isFeatureAvailable(
                    AtlasFeature.LINUX
                )

        if (!linuxAvailable) {

            return LinuxRuntimeControlResult
                .FEATURE_UNAVAILABLE
        }

        return when (
            val result =
                backend.start()
        ) {

            is LinuxRuntimeBackendResult.Success -> {

                val session =
                    result.session

                if (
                    session == null ||
                    !ProotLinuxRuntimeBackend
                        .isProcessAlive()
                ) {

                    activeSession =
                        null

                    LinuxRepository
                        .stopLinux()

                    return LinuxRuntimeControlResult
                        .START_FAILED
                }

                activeSession =
                    session

                LinuxRepository
                    .startLinux()

                LinuxRuntimeControlResult
                    .STARTED
            }

            is LinuxRuntimeBackendResult.Failure -> {

                activeSession =
                    null

                LinuxRepository
                    .stopLinux()

                LinuxRuntimeControlResult
                    .START_FAILED
            }
        }
    }

    fun stop():
            LinuxRuntimeControlResult {

        val installation =
            LinuxRepository
                .getInstallation()

        if (!installation.installed) {

            activeSession =
                null

            return LinuxRuntimeControlResult
                .NOT_INSTALLED
        }

        val processAlive =
            ProotLinuxRuntimeBackend
                .isProcessAlive()

        /*
         * Repository and process both agree that
         * the runtime is already stopped.
         */
        if (
            !installation.running &&
            !processAlive
        ) {

            activeSession =
                null

            return LinuxRuntimeControlResult
                .ALREADY_STOPPED
        }

        return when (
            backend.stop()
        ) {

            is LinuxRuntimeBackendResult.Success -> {

                activeSession =
                    null

                LinuxRepository
                    .stopLinux()

                LinuxRuntimeControlResult
                    .STOPPED
            }

            is LinuxRuntimeBackendResult.Failure -> {

                /*
                 * Do not claim STOPPED when the
                 * backend could not terminate the
                 * actual process.
                 */
                LinuxRuntimeControlResult
                    .STOP_FAILED
            }
        }
    }
}

enum class LinuxRuntimeControlResult {

    STARTED,

    STOPPED,

    ALREADY_RUNNING,

    ALREADY_STOPPED,

    NOT_INSTALLED,

    INSTALLATION_IN_PROGRESS,

    FEATURE_UNAVAILABLE,

    START_FAILED,

    STOP_FAILED
}