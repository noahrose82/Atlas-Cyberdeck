package com.noahrose.pocketlab.feature.system

import com.noahrose.pocketlab.BuildConfig

object VersionInfo {

    const val NAME =
        "Atlas Cyberdeck"

    val VERSION: String
        get() =
            BuildConfig.VERSION_NAME

    val BUILD: String
        get() =
            BuildConfig.VERSION_CODE
                .toString()

    const val CODENAME =
        "Forge"

    const val AUTHOR =
        "Atlas Labs"
}