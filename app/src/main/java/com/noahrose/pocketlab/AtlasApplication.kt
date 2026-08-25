package com.noahrose.pocketlab

import android.app.Application
import com.noahrose.pocketlab.feature.filesystem.persistence.PersistenceManager
import com.noahrose.pocketlab.feature.linux.runtime.network.LinuxGuestDnsManager
import com.noahrose.pocketlab.feature.system.DeviceInfoProvider
import com.noahrose.pocketlab.feature.system.bootstrap.DeviceBootstrapManager
import com.noahrose.pocketlab.feature.system.bootstrap.DeviceProfilePersistence
import com.noahrose.pocketlab.feature.terminal.alias.CommandAliases
import com.noahrose.pocketlab.feature.terminal.environment.EnvironmentVariables
import com.noahrose.pocketlab.feature.terminal.persistence.ShellConfigPersistence

class AtlasApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        /*
         * Virtual filesystem.
         */
        PersistenceManager.initialize(
            this
        )

        /*
         * Shell persistence.
         */
        ShellConfigPersistence.initialize(
            this
        )

        /*
         * Device discovery.
         */
        DeviceInfoProvider.initialize(
            this
        )

        /*
         * Device profile persistence.
         */
        DeviceProfilePersistence.initialize(
            this
        )

        /*
         * Ubuntu guest DNS synchronization.
         *
         * Makes Android's active DNS configuration
         * available to the rootless Ubuntu runtime.
         */
        LinuxGuestDnsManager.initialize(
            this
        )

        /*
         * Automatically bootstrap this
         * installation's device profile.
         */
        DeviceBootstrapManager.bootstrap()

        /*
         * Restore aliases.
         */
        CommandAliases.restoreUserAliases(
            ShellConfigPersistence.loadAliases()
        )

        /*
         * Restore environment variables.
         */
        EnvironmentVariables.restoreUserVariables(
            ShellConfigPersistence
                .loadEnvironmentVariables()
        )
    }
}