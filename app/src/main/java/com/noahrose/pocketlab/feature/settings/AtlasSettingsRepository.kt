package com.noahrose.pocketlab.feature.settings

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object AtlasSettingsRepository {

    private const val PREFERENCES_NAME =
        "atlas_settings"

    private const val KEY_LINUX_QUICK_START =
        "linux_quick_start"

    private const val KEY_ONBOARDING_COMPLETE =
        "onboarding_complete"

    private var preferences:
            SharedPreferences? =
        null

    private val mutableLinuxQuickStartEnabled =
        MutableStateFlow(
            false
        )

    val linuxQuickStartEnabled:
            StateFlow<Boolean> =
        mutableLinuxQuickStartEnabled
            .asStateFlow()

    private val mutableOnboardingComplete =
        MutableStateFlow(
            false
        )

    val onboardingComplete:
            StateFlow<Boolean> =
        mutableOnboardingComplete
            .asStateFlow()

    /*
     * Initialize once when Atlas starts.
     *
     * Quick Start defaults to OFF until the user
     * explicitly enables it.
     *
     * Onboarding defaults to incomplete until the
     * user enters Atlas from the Welcome screen.
     */
    @Synchronized
    fun initialize(
        context: Context
    ) {

        if (
            preferences != null
        ) {
            return
        }

        val sharedPreferences =
            context
                .applicationContext
                .getSharedPreferences(
                    PREFERENCES_NAME,
                    Context.MODE_PRIVATE
                )

        preferences =
            sharedPreferences

        mutableLinuxQuickStartEnabled.value =
            sharedPreferences
                .getBoolean(
                    KEY_LINUX_QUICK_START,
                    false
                )

        mutableOnboardingComplete.value =
            sharedPreferences
                .getBoolean(
                    KEY_ONBOARDING_COMPLETE,
                    false
                )
    }

    fun isLinuxQuickStartEnabled():
            Boolean {

        return mutableLinuxQuickStartEnabled
            .value
    }

    @Synchronized
    fun setLinuxQuickStartEnabled(
        enabled: Boolean
    ) {

        val sharedPreferences =
            preferences
                ?: return

        sharedPreferences
            .edit()
            .putBoolean(
                KEY_LINUX_QUICK_START,
                enabled
            )
            .apply()

        mutableLinuxQuickStartEnabled.value =
            enabled
    }

    fun isOnboardingComplete():
            Boolean {

        return mutableOnboardingComplete
            .value
    }

    @Synchronized
    fun setOnboardingComplete(
        complete: Boolean
    ) {

        val sharedPreferences =
            preferences
                ?: return

        sharedPreferences
            .edit()
            .putBoolean(
                KEY_ONBOARDING_COMPLETE,
                complete
            )
            .apply()

        mutableOnboardingComplete.value =
            complete
    }
}
