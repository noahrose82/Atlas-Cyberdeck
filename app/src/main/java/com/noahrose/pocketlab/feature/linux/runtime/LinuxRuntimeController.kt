package com.noahrose.pocketlab.feature.linux.runtime

import com.noahrose.pocketlab.feature.linux.repository.LinuxRepository
import com.noahrose.pocketlab.feature.linux.runtime.safety.LinuxRuntimeCircuitBreaker
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
         * SAFE_MODE is authoritative.
         *
         * Never expose a logical runtime session while
         * the circuit breaker says normal runtime use is
         * blocked.
         */
        if (
            !LinuxRuntimeCircuitBreaker
                .canStartRuntime()
        ) {

            activeSession =
                null

            if (
                !ProotLinuxRuntimeBackend
                    .isProcessAlive()
            ) {

                val installation =
                    LinuxRepository
                        .getInstallation()

                if (
                    installation.running
                ) {

                    LinuxRepository
                        .stopLinux()
                }
            }

            return null
        }

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
         * ------------------------------------------------
         * PRIMARY RUNTIME SAFETY GATE
         * ------------------------------------------------
         *
         * Every normal runtime start request flows through
         * LinuxRuntimeController. SAFE_MODE must therefore
         * be enforced HERE before stale-state reconciliation,
         * feature checks, or backend launch.
         *
         * RECOVERY_ARMED is intentionally allowed through;
         * LinuxGuestCommandExecutor then restricts Ubuntu to
         * recovery-safe commands until health is verified.
         */
        if (
            !LinuxRuntimeCircuitBreaker
                .canStartRuntime()
        ) {

            activeSession =
                null

            /*
             * The breaker normally stops PRoot when it
             * trips. Reconcile the repository flag here
             * as a second layer in case the process is
             * already gone but the transient RUNNING flag
             * was still true.
             */
            if (
                !ProotLinuxRuntimeBackend
                    .isProcessAlive()
            ) {

                LinuxRepository
                    .stopLinux()
            }

            return LinuxRuntimeControlResult
                .SAFE_MODE_BLOCKED
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

    SAFE_MODE_BLOCKED,

    START_FAILED,

    STOP_FAILED
}
