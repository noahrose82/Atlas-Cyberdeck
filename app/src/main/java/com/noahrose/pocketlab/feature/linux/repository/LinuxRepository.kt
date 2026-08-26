package com.noahrose.pocketlab.feature.linux.repository

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.noahrose.pocketlab.feature.linux.model.LinuxDistribution
import com.noahrose.pocketlab.feature.linux.model.LinuxInstallation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object LinuxRepository {

    private const val PREFERENCES_NAME =
        "atlas_linux_installation"

    private const val KEY_INSTALLED =
        "installed"

    private const val KEY_DISTRIBUTION =
        "distribution"

    private const val KEY_VERSION =
        "version"

    private const val KEY_PACKAGE_COUNT =
        "package_count"

    private const val KEY_STORAGE_USED_MB =
        "storage_used_mb"

    private var preferences: SharedPreferences? =
        null

    private val defaultInstallation =
        LinuxInstallation(
            distribution =
                LinuxDistribution.UBUNTU,

            version =
                "24.04 LTS",

            installed =
                false,

            running =
                false,

            packageCount =
                0,

            storageUsedMb =
                0L
        )

    private val _installation =
        MutableStateFlow(
            defaultInstallation
        )

    val installation: StateFlow<LinuxInstallation> =
        _installation.asStateFlow()

    fun initialize(
        context: Context
    ) {

        preferences =
            context
                .applicationContext
                .getSharedPreferences(
                    PREFERENCES_NAME,
                    Context.MODE_PRIVATE
                )

        restoreInstallation()
    }

    fun getInstallation():
            LinuxInstallation {

        return _installation.value
    }

    fun updateInstallation(
        updated: LinuxInstallation
    ) {

        _installation.value =
            updated

        /*
         * Only durable installation metadata is
         * written by persistInstallation().
         *
         * Runtime and installation-progress state
         * remain session-only.
         */
        persistInstallation()
    }

    fun updateMetrics(
        packageCount: Int,
        storageUsedMb: Long
    ) {

        val current =
            _installation.value

        if (!current.installed) {
            return
        }

        _installation.value =
            current.copy(
                packageCount =
                    packageCount
                        .coerceAtLeast(
                            0
                        ),

                storageUsedMb =
                    storageUsedMb
                        .coerceAtLeast(
                            0L
                        )
            )

        /*
         * Metrics describe the persistent Ubuntu
         * installation and are safe to restore on
         * the next application launch.
         */
        persistInstallation()
    }

    fun startInstallation() {

        val current =
            _installation.value

        if (
            current.isInstalling ||
            current.installed
        ) {
            return
        }

        _installation.value =
            current.copy(
                running =
                    false,

                isInstalling =
                    true,

                installationProgress =
                    0f,

                installationStep =
                    "Preparing installation..."
            )
    }

    fun completeInstallation() {

        _installation.value =
            _installation
                .value
                .copy(
                    installed =
                        true,

                    running =
                        false,

                    /*
                     * The RootFS metrics reader
                     * replaces these temporary zeros
                     * immediately after provisioning.
                     */
                    packageCount =
                        0,

                    storageUsedMb =
                        0L,

                    isInstalling =
                        false,

                    installationProgress =
                        1f,

                    installationStep =
                        "Installation complete"
                )

        persistInstallation()
    }

    /*
     * Starts the installed Linux runtime.
     *
     * Runtime state is deliberately session-only
     * and is never persisted.
     */
    fun startLinux() {

        val current =
            _installation.value

        if (
            !current.installed ||
            current.isInstalling ||
            current.running
        ) {
            return
        }

        _installation.value =
            current.copy(
                running =
                    true
            )
    }

    /*
     * Stops the active Linux runtime.
     */
    fun stopLinux() {

        val current =
            _installation.value

        if (
            !current.installed ||
            !current.running
        ) {
            return
        }

        _installation.value =
            current.copy(
                running =
                    false
            )
    }

    fun removeLinux() {

        _installation.value =
            _installation
                .value
                .copy(
                    installed =
                        false,

                    running =
                        false,

                    packageCount =
                        0,

                    storageUsedMb =
                        0L,

                    isInstalling =
                        false,

                    installationProgress =
                        0f,

                    installationStep =
                        "Ready"
                )

        persistInstallation()
    }

    private fun restoreInstallation() {

        val prefs =
            preferences
                ?: return

        val installed =
            prefs.getBoolean(
                KEY_INSTALLED,
                false
            )

        val distributionName =
            prefs.getString(
                KEY_DISTRIBUTION,
                LinuxDistribution
                    .UBUNTU
                    .name
            )

        val distribution =
            runCatching {

                LinuxDistribution.valueOf(
                    distributionName
                        ?: LinuxDistribution
                            .UBUNTU
                            .name
                )

            }.getOrDefault(
                LinuxDistribution.UBUNTU
            )

        val version =
            prefs.getString(
                KEY_VERSION,
                "24.04 LTS"
            )
                ?: "24.04 LTS"

        val packageCount =
            prefs.getInt(
                KEY_PACKAGE_COUNT,
                0
            )

        val storageUsedMb =
            prefs.getLong(
                KEY_STORAGE_USED_MB,
                0L
            )

        _installation.value =
            defaultInstallation
                .copy(
                    distribution =
                        distribution,

                    version =
                        version,

                    installed =
                        installed,

                    /*
                     * Runtime sessions never survive
                     * application process restart.
                     */
                    running =
                        false,

                    packageCount =
                        packageCount,

                    storageUsedMb =
                        storageUsedMb,

                    isInstalling =
                        false,

                    installationProgress =
                        if (installed) {
                            1f
                        } else {
                            0f
                        },

                    installationStep =
                        if (installed) {
                            "Installation complete"
                        } else {
                            "Ready"
                        }
                )
    }

    /*
     * Persist only durable installation metadata.
     *
     * Excluded intentionally:
     *
     * running
     * isInstalling
     * installationProgress
     * installationStep
     */
    private fun persistInstallation() {

        val prefs =
            preferences
                ?: return

        val current =
            _installation.value

        prefs.edit {

            putBoolean(
                KEY_INSTALLED,
                current.installed
            )

            putString(
                KEY_DISTRIBUTION,
                current
                    .distribution
                    .name
            )

            putString(
                KEY_VERSION,
                current.version
            )

            putInt(
                KEY_PACKAGE_COUNT,
                current.packageCount
            )

            putLong(
                KEY_STORAGE_USED_MB,
                current.storageUsedMb
            )
        }
    }
}
