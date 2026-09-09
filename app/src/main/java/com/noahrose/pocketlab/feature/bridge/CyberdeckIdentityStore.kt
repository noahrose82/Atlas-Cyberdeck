package com.noahrose.pocketlab.feature.bridge

import android.content.Context
import android.os.Build
import java.util.UUID

class CyberdeckIdentityStore(
    context: Context
) {

    private val preferences =
        context.getSharedPreferences(
            "atlas_bridge_identity",
            Context.MODE_PRIVATE
        )

    fun getIdentity(): CyberdeckIdentity {
        val existingId =
            preferences.getString(
                KEY_CYBERDECK_ID,
                null
            )

        val cyberdeckId =
            existingId
                ?: UUID.randomUUID()
                    .toString()
                    .also { generatedId ->

                        preferences
                            .edit()
                            .putString(
                                KEY_CYBERDECK_ID,
                                generatedId
                            )
                            .apply()
                    }

        return CyberdeckIdentity(
            cyberdeckId = cyberdeckId,
            deviceName =
                Build.MODEL
                    .takeIf {
                        it.isNotBlank()
                    }
                    ?: "Atlas Cyberdeck"
        )
    }

    companion object {
        private const val KEY_CYBERDECK_ID =
            "cyberdeck_id"
    }
}

data class CyberdeckIdentity(
    val cyberdeckId: String,
    val deviceName: String
)
