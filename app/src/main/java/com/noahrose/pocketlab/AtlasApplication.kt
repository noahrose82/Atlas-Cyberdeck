package com.noahrose.pocketlab

import android.app.Application
import com.noahrose.pocketlab.feature.filesystem.persistence.PersistenceManager

class AtlasApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        PersistenceManager.initialize(this)
    }
}