package com.noahrose.pocketlab.feature.system.bootstrap

import android.content.Context
import com.noahrose.pocketlab.feature.system.DeviceProfile
import org.json.JSONObject
import java.io.File

object DeviceProfilePersistence {

    private const val FILE_NAME =
        "atlas_device_profile.json"

    private var applicationContext: Context? =
        null

    fun initialize(
        context: Context
    ) {
        applicationContext =
            context.applicationContext
    }

    fun save(
        profile: DeviceProfile
    ): Boolean {

        val context =
            applicationContext
                ?: return false

        return try {

            val json =
                JSONObject().apply {

                    put(
                        "manufacturer",
                        profile.manufacturer
                    )

                    put(
                        "model",
                        profile.model
                    )

                    put(
                        "androidVersion",
                        profile.androidVersion
                    )

                    put(
                        "apiLevel",
                        profile.apiLevel
                    )

                    put(
                        "architecture",
                        profile.architecture
                    )

                    put(
                        "availableProcessors",
                        profile.availableProcessors
                    )

                    put(
                        "totalMemoryMb",
                        profile.totalMemoryMb
                    )

                    put(
                        "availableMemoryMb",
                        profile.availableMemoryMb
                    )

                    put(
                        "totalStorageMb",
                        profile.totalStorageMb
                    )

                    put(
                        "availableStorageMb",
                        profile.availableStorageMb
                    )

                    put(
                        "atlasName",
                        profile.atlasName
                    )

                    put(
                        "atlasVersion",
                        profile.atlasVersion
                    )

                    put(
                        "atlasBuild",
                        profile.atlasBuild
                    )

                    put(
                        "atlasCodename",
                        profile.atlasCodename
                    )
                }

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

    fun load(): DeviceProfile? {

        val context =
            applicationContext
                ?: return null

        val file =
            File(
                context.filesDir,
                FILE_NAME
            )

        if (!file.exists()) {
            return null
        }

        return try {

            val json =
                JSONObject(
                    file.readText()
                )

            DeviceProfile(

                manufacturer =
                    json.optString(
                        "manufacturer",
                        "Unknown"
                    ),

                model =
                    json.optString(
                        "model",
                        "Unknown"
                    ),

                androidVersion =
                    json.optString(
                        "androidVersion",
                        "Unknown"
                    ),

                apiLevel =
                    json.optInt(
                        "apiLevel",
                        0
                    ),

                architecture =
                    json.optString(
                        "architecture",
                        "Unknown"
                    ),

                availableProcessors =
                    json.optInt(
                        "availableProcessors",
                        0
                    ),

                totalMemoryMb =
                    json.optLong(
                        "totalMemoryMb",
                        0L
                    ),

                availableMemoryMb =
                    json.optLong(
                        "availableMemoryMb",
                        0L
                    ),

                totalStorageMb =
                    json.optLong(
                        "totalStorageMb",
                        0L
                    ),

                availableStorageMb =
                    json.optLong(
                        "availableStorageMb",
                        0L
                    ),

                atlasName =
                    json.optString(
                        "atlasName",
                        "Atlas Cyberdeck"
                    ),

                atlasVersion =
                    json.optString(
                        "atlasVersion",
                        "Unknown"
                    ),

                atlasBuild =
                    json.optString(
                        "atlasBuild",
                        "Unknown"
                    ),

                atlasCodename =
                    json.optString(
                        "atlasCodename",
                        "Unknown"
                    )
            )

        } catch (exception: Exception) {

            null
        }
    }

    fun exists(): Boolean {

        val context =
            applicationContext
                ?: return false

        return File(
            context.filesDir,
            FILE_NAME
        ).exists()
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
}