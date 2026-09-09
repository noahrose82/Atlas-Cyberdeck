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

    private const val KEY_ATLAS_BRIDGE_ENABLED =
        "atlas_bridge_enabled"

    private const val KEY_ATLAS_BRIDGE_ADDRESS =
        "atlas_bridge_address"

    private var preferences:
            SharedPreferences? =
        null

    /*
     * ------------------------------------------------
     * LINUX QUICK START
     * ------------------------------------------------
     */
    private val mutableLinuxQuickStartEnabled =
        MutableStateFlow(
            false
        )

    val linuxQuickStartEnabled:
            StateFlow<Boolean> =
        mutableLinuxQuickStartEnabled
            .asStateFlow()

    /*
     * ------------------------------------------------
     * ONBOARDING
     * ------------------------------------------------
     */
    private val mutableOnboardingComplete =
        MutableStateFlow(
            false
        )

    val onboardingComplete:
            StateFlow<Boolean> =
        mutableOnboardingComplete
            .asStateFlow()

    /*
     * ------------------------------------------------
     * ATLAS BRIDGE
     * ------------------------------------------------
     *
     * Bridge is intentionally disabled by default.
     *
     * Cyberdeck must always remain fully usable without
     * Atlas Bridge, and no meaningless localhost retries
     * should occur until the user explicitly configures
     * and enables Bridge connectivity.
     */
    private val mutableAtlasBridgeEnabled =
        MutableStateFlow(
            false
        )

    val atlasBridgeEnabled:
            StateFlow<Boolean> =
        mutableAtlasBridgeEnabled
            .asStateFlow()

    private val mutableAtlasBridgeAddress =
        MutableStateFlow(
            ""
        )

    val atlasBridgeAddress:
            StateFlow<String> =
        mutableAtlasBridgeAddress
            .asStateFlow()

    /*
     * Initialize once when Atlas starts.
     *
     * Quick Start defaults to OFF until the user
     * explicitly enables it.
     *
     * Onboarding defaults to incomplete until the
     * user enters Atlas from the Welcome screen.
     *
     * Atlas Bridge defaults to OFF with no configured
     * address so Cyberdeck never depends on Bridge.
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

        mutableAtlasBridgeEnabled.value =
            sharedPreferences
                .getBoolean(
                    KEY_ATLAS_BRIDGE_ENABLED,
                    false
                )

        mutableAtlasBridgeAddress.value =
            sharedPreferences
                .getString(
                    KEY_ATLAS_BRIDGE_ADDRESS,
                    ""
                )
                ?.trim()
                .orEmpty()
    }

    /*
     * ------------------------------------------------
     * LINUX QUICK START
     * ------------------------------------------------
     */
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

    /*
     * ------------------------------------------------
     * ONBOARDING
     * ------------------------------------------------
     */
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

    /*
     * ------------------------------------------------
     * ATLAS BRIDGE
     * ------------------------------------------------
     */
    fun isAtlasBridgeEnabled():
            Boolean {

        return mutableAtlasBridgeEnabled
            .value
    }

    fun getAtlasBridgeAddress():
            String {

        return mutableAtlasBridgeAddress
            .value
    }

    @Synchronized
    fun setAtlasBridgeEnabled(
        enabled: Boolean
    ) {

        val sharedPreferences =
            preferences
                ?: return

        sharedPreferences
            .edit()
            .putBoolean(
                KEY_ATLAS_BRIDGE_ENABLED,
                enabled
            )
            .apply()

        mutableAtlasBridgeEnabled.value =
            enabled
    }

    @Synchronized
    fun setAtlasBridgeAddress(
        address: String
    ) {

        val normalizedAddress =
            address.trim()

        val sharedPreferences =
            preferences
                ?: return

        sharedPreferences
            .edit()
            .putString(
                KEY_ATLAS_BRIDGE_ADDRESS,
                normalizedAddress
            )
            .apply()

        mutableAtlasBridgeAddress.value =
            normalizedAddress
    }
}
