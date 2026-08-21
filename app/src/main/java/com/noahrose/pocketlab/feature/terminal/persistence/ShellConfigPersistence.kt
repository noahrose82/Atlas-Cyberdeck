package com.noahrose.pocketlab.feature.terminal.persistence

import android.content.Context
import org.json.JSONObject
import java.io.File

object ShellConfigPersistence {

    private const val FILE_NAME =
        "atlas_shell_config.json"

    private var applicationContext: Context? =
        null

    fun initialize(
        context: Context
    ) {

        applicationContext =
            context.applicationContext
    }

    /*
     * Save user-defined aliases.
     *
     * Local JVM unit tests do not provide an
     * Android Context, so persistence simply
     * becomes a no-op in that environment.
     */
    fun saveAliases(
        aliases: Map<String, String>
    ): Boolean {

        if (applicationContext == null) {
            return false
        }

        val config =
            loadJson()

        val aliasJson =
            JSONObject()

        aliases.forEach { (name, command) ->

            aliasJson.put(
                name,
                command
            )
        }

        config.put(
            "aliases",
            aliasJson
        )

        return saveJson(
            config
        )
    }

    fun loadAliases(): Map<String, String> {

        if (applicationContext == null) {
            return emptyMap()
        }

        val config =
            loadJson()

        val aliasJson =
            config.optJSONObject(
                "aliases"
            ) ?: return emptyMap()

        val aliases =
            mutableMapOf<String, String>()

        val keys =
            aliasJson.keys()

        while (keys.hasNext()) {

            val key =
                keys.next()

            aliases[key] =
                aliasJson.optString(
                    key,
                    ""
                )
        }

        return aliases
    }

    /*
     * Save user-defined environment variables.
     */
    fun saveEnvironmentVariables(
        variables: Map<String, String>
    ): Boolean {

        if (applicationContext == null) {
            return false
        }

        val config =
            loadJson()

        val variableJson =
            JSONObject()

        variables.forEach { (name, value) ->

            variableJson.put(
                name,
                value
            )
        }

        config.put(
            "environment",
            variableJson
        )

        return saveJson(
            config
        )
    }

    fun loadEnvironmentVariables(): Map<String, String> {

        if (applicationContext == null) {
            return emptyMap()
        }

        val config =
            loadJson()

        val variableJson =
            config.optJSONObject(
                "environment"
            ) ?: return emptyMap()

        val variables =
            mutableMapOf<String, String>()

        val keys =
            variableJson.keys()

        while (keys.hasNext()) {

            val key =
                keys.next()

            variables[key] =
                variableJson.optString(
                    key,
                    ""
                )
        }

        return variables
    }

    fun clear(): Boolean {

        val context =
            applicationContext
                ?: return false

        val file =
            File(
                context.filesDir,
                FILE_NAME
            )

        return !file.exists() ||
                file.delete()
    }

    private fun loadJson(): JSONObject {

        val context =
            applicationContext
                ?: return JSONObject()

        val file =
            File(
                context.filesDir,
                FILE_NAME
            )

        if (!file.exists()) {
            return JSONObject()
        }

        return try {

            JSONObject(
                file.readText()
            )

        } catch (exception: Exception) {

            JSONObject()
        }
    }

    private fun saveJson(
        json: JSONObject
    ): Boolean {

        val context =
            applicationContext
                ?: return false

        return try {

            val file =
                File(
                    context.filesDir,
                    FILE_NAME
                )

            file.writeText(
                json.toString(2)
            )

            true

        } catch (exception: Exception) {

            false
        }
    }
}