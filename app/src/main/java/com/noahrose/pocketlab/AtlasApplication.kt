package com.noahrose.pocketlab

import android.app.Application
import com.noahrose.pocketlab.feature.filesystem.persistence.PersistenceManager
import com.noahrose.pocketlab.feature.terminal.alias.CommandAliases
import com.noahrose.pocketlab.feature.terminal.environment.EnvironmentVariables
import com.noahrose.pocketlab.feature.terminal.persistence.ShellConfigPersistence

class AtlasApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        PersistenceManager.initialize(
            this
        )

        ShellConfigPersistence.initialize(
            this
        )

        CommandAliases.restoreUserAliases(
            ShellConfigPersistence.loadAliases()
        )

        EnvironmentVariables.restoreUserVariables(
            ShellConfigPersistence
                .loadEnvironmentVariables()
        )
    }
}