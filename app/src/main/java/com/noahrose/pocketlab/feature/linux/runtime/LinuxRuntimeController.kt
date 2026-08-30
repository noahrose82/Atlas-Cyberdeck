package com.noahrose.pocketlab.feature.linux.runtime

import com.noahrose.pocketlab.feature.linux.repository.LinuxRepository
import com.noahrose.pocketlab.feature.linux.runtime.activity.LinuxRuntimeActivityReporter
import com.noahrose.pocketlab.feature.linux.runtime.safety.LinuxRuntimeCircuitBreaker
import com.noahrose.pocketlab.feature.system.bootstrap.DeviceBootstrapManager
import com.noahrose.pocketlab.feature.system.capability.AtlasFeature

object LinuxRuntimeController {

    /*
     * Atlas uses the real PRoot-backed Ubuntu runtime.
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
         * Do not emit routine activity from getSession().
         *
         * The UI calls this during state refreshes, so
         * reporting here would flood the activity history.
         */

        /*
         * SAFE_MODE is authoritative.
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
         * Reconcile a stale session if the native PRoot
         * process exited unexpectedly.
         */
        if (
            !ProotLinuxRuntimeBackend
                .isProcessAlive()
        ) {

            activeSession =
                null

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

        return activeSession
    }

    fun start():
            LinuxRuntimeControlResult {

        /*
         * A new explicit start request begins a fresh
         * activity history.
         */
        LinuxRuntimeActivityReporter
            .clear()

        LinuxRuntimeActivityReporter
            .info(
                "Ubuntu runtime start requested."
            )

        val installation =
            LinuxRepository
                .getInstallation()

        /*
         * Installation must be complete before runtime
         * startup can be attempted.
         */
        LinuxRuntimeActivityReporter
            .info(
                "Checking Ubuntu installation state."
            )

        if (
            installation.isInstalling
        ) {

            LinuxRuntimeActivityReporter
                .warning(
                    "Ubuntu installation is still in progress."
                )

            return LinuxRuntimeControlResult
                .INSTALLATION_IN_PROGRESS
        }

        if (
            !installation.installed
        ) {

            LinuxRuntimeActivityReporter
                .warning(
                    "Ubuntu is not installed."
                )

            return LinuxRuntimeControlResult
                .NOT_INSTALLED
        }

        LinuxRuntimeActivityReporter
            .success(
                "Ubuntu installation state verified."
            )

        /*
         * ------------------------------------------------
         * PRIMARY RUNTIME SAFETY GATE
         * ------------------------------------------------
         */
        LinuxRuntimeActivityReporter
            .info(
                "Checking Atlas runtime safety state."
            )

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

                LinuxRepository
                    .stopLinux()
            }

            LinuxRuntimeActivityReporter
                .error(
                    "Runtime startup blocked by Atlas Safe Mode."
                )

            return LinuxRuntimeControlResult
                .SAFE_MODE_BLOCKED
        }

        LinuxRuntimeActivityReporter
            .success(
                "Runtime safety state allows startup."
            )

        /*
         * Reconcile stale repository RUNNING state.
         */
        LinuxRuntimeActivityReporter
            .info(
                "Checking existing PRoot runtime state."
            )

        if (
            installation.running
        ) {

            if (
                ProotLinuxRuntimeBackend
                    .isProcessAlive()
            ) {

                LinuxRuntimeActivityReporter
                    .success(
                        "Ubuntu runtime is already running."
                    )

                return LinuxRuntimeControlResult
                    .ALREADY_RUNNING
            }

            LinuxRuntimeActivityReporter
                .warning(
                    "Detected stale runtime state; reconciling repository."
                )

            LinuxRepository
                .stopLinux()

            activeSession =
                null
        }

        /*
         * Device capability gate.
         */
        LinuxRuntimeActivityReporter
            .info(
                "Checking Linux device capability."
            )

        val linuxAvailable =
            DeviceBootstrapManager
                .isFeatureAvailable(
                    AtlasFeature.LINUX
                )

        if (
            !linuxAvailable
        ) {

            LinuxRuntimeActivityReporter
                .error(
                    "Linux runtime is unavailable on this device."
                )

            return LinuxRuntimeControlResult
                .FEATURE_UNAVAILABLE
        }

        LinuxRuntimeActivityReporter
            .success(
                "Linux device capability verified."
            )

        /*
         * Delegate the actual native startup sequence to
         * the PRoot backend.
         */
        LinuxRuntimeActivityReporter
            .info(
                "Starting PRoot runtime backend."
            )

        return when (
            val result =
                backend.start()
        ) {

            is LinuxRuntimeBackendResult.Success -> {

                val session =
                    result.session

                LinuxRuntimeActivityReporter
                    .info(
                        "Validating PRoot runtime session."
                    )

                /*
                 * Backend success is not enough.
                 *
                 * Atlas requires both a logical session and
                 * a living native process.
                 */
                if (
                    session == null ||
                    !ProotLinuxRuntimeBackend
                        .isProcessAlive()
                ) {

                    activeSession =
                        null

                    LinuxRepository
                        .stopLinux()

                    LinuxRuntimeActivityReporter
                        .error(
                            "PRoot backend did not produce a valid runtime session."
                        )

                    LinuxRuntimeControlResult
                        .START_FAILED

                } else {

                    activeSession =
                        session

                    LinuxRepository
                        .startLinux()

                    LinuxRuntimeActivityReporter
                        .success(
                            "Ubuntu runtime is running."
                        )

                    LinuxRuntimeControlResult
                        .STARTED
                }
            }

            is LinuxRuntimeBackendResult.Failure -> {

                activeSession =
                    null

                LinuxRepository
                    .stopLinux()

                LinuxRuntimeActivityReporter
                    .error(
                        result.message
                            .takeIf { message ->
                                message.isNotBlank()
                            }
                            ?: "PRoot runtime backend failed to start."
                    )

                LinuxRuntimeControlResult
                    .START_FAILED
            }
        }
    }

    fun stop():
            LinuxRuntimeControlResult {

        LinuxRuntimeActivityReporter
            .info(
                "Ubuntu runtime stop requested."
            )

        val installation =
            LinuxRepository
                .getInstallation()

        if (
            !installation.installed
        ) {

            activeSession =
                null

            LinuxRuntimeActivityReporter
                .warning(
                    "Ubuntu is not installed."
                )

            return LinuxRuntimeControlResult
                .NOT_INSTALLED
        }

        LinuxRuntimeActivityReporter
            .info(
                "Checking PRoot process state."
            )

        val processAlive =
            ProotLinuxRuntimeBackend
                .isProcessAlive()

        /*
         * Repository state and native process state agree
         * that Linux is already stopped.
         */
        if (
            !installation.running &&
            !processAlive
        ) {

            activeSession =
                null

            LinuxRuntimeActivityReporter
                .info(
                    "Ubuntu runtime is already stopped."
                )

            return LinuxRuntimeControlResult
                .ALREADY_STOPPED
        }

        LinuxRuntimeActivityReporter
            .info(
                "Stopping PRoot runtime backend."
            )

        return when (
            val result =
                backend.stop()
        ) {

            is LinuxRuntimeBackendResult.Success -> {

                activeSession =
                    null

                LinuxRepository
                    .stopLinux()

                LinuxRuntimeActivityReporter
                    .success(
                        "Ubuntu runtime stopped."
                    )

                LinuxRuntimeControlResult
                    .STOPPED
            }

            is LinuxRuntimeBackendResult.Failure -> {

                LinuxRuntimeActivityReporter
                    .error(
                        result.message
                            .takeIf { message ->
                                message.isNotBlank()
                            }
                            ?: "PRoot runtime backend failed to stop."
                    )

                /*
                 * Do not report STOPPED if the backend
                 * could not terminate the actual process.
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