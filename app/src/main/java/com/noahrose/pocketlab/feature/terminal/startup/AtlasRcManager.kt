package com.noahrose.pocketlab.feature.terminal.startup

import com.noahrose.pocketlab.feature.filesystem.VirtualFileSystem
import com.noahrose.pocketlab.feature.terminal.script.ScriptEngine

object AtlasRcManager {

    private const val FILE_NAME =
        ".atlasrc"

    fun execute(
        output: MutableList<String>
    ): Boolean {

        val content =
            VirtualFileSystem.readFile(
                FILE_NAME
            ) ?: return false

        ScriptEngine.execute(
            script = content.lines(),
            output = output,
            showPrompts = false
        )

        return true
    }

    fun exists(): Boolean {

        return VirtualFileSystem.readFile(
            FILE_NAME
        ) != null
    }
}