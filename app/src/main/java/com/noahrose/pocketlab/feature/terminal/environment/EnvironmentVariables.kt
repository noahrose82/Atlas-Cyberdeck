package com.noahrose.pocketlab.feature.terminal.environment

import com.noahrose.pocketlab.feature.filesystem.VirtualFileSystem

object EnvironmentVariables {

    fun valueOf(name: String): String? {

        return when (name.uppercase()) {

            "USER" -> "atlas"

            "HOME" -> "/home/atlas"

            "PWD" ->
                VirtualFileSystem.currentPath.value
                    .replace("~", "/home/atlas")

            "HOSTNAME" -> "cyberdeck"

            "SHELL" -> "/bin/atlas"

            else -> null
        }
    }
}