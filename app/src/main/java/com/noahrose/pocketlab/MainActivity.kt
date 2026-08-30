package com.noahrose.pocketlab

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.noahrose.pocketlab.feature.linux.repository.LinuxRepository
import com.noahrose.pocketlab.feature.linux.rootfs.filesystem.LinuxRootfsStagingManager
import com.noahrose.pocketlab.feature.linux.runtime.filesystem.LinuxRuntimeFilesystemManager
import com.noahrose.pocketlab.feature.linux.runtime.filesystem.LinuxRuntimePathManager
import com.noahrose.pocketlab.feature.linux.runtime.platform.LinuxNativeRuntimeResolver
import com.noahrose.pocketlab.feature.linux.runtime.safety.LinuxRuntimeCircuitBreaker
import com.noahrose.pocketlab.feature.linux.runtime.startup.LinuxQuickStartManager
import com.noahrose.pocketlab.feature.settings.AtlasSettingsRepository
import com.noahrose.pocketlab.ui.components.AtlasSafetyBanner
import com.noahrose.pocketlab.ui.navigation.AtlasNavigation
import com.noahrose.pocketlab.ui.screens.AtlasSplashScreen
import com.noahrose.pocketlab.ui.theme.PocketLabTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

class MainActivity : ComponentActivity() {

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {

        super.onCreate(
            savedInstanceState
        )

        /*
         * ------------------------------------------------
         * ATLAS SETTINGS
         * ------------------------------------------------
         *
         * Load persisted application preferences before
         * any startup behavior depends on them.
         */
        AtlasSettingsRepository
            .initialize(
                applicationContext
            )

        /*
         * ------------------------------------------------
         * LINUX INSTALLATION STATE
         * ------------------------------------------------
         */
        LinuxRepository
            .initialize(
                applicationContext
            )

        /*
         * ------------------------------------------------
         * RUNTIME FILESYSTEM PATHS
         * ------------------------------------------------
         */
        LinuxRuntimePathManager
            .initialize(
                applicationContext
            )

        /*
         * ------------------------------------------------
         * RUNTIME SAFETY
         * ------------------------------------------------
         *
         * Restore the persisted safety record only after
         * runtime paths are available.
         *
         * This also seeds the safety StateFlow before
         * Compose begins observing it.
         */
        LinuxRuntimeCircuitBreaker
            .getSnapshot()

        /*
         * ------------------------------------------------
         * RUNTIME FILESYSTEM
         * ------------------------------------------------
         */
        LinuxRuntimeFilesystemManager
            .prepare()

        /*
         * ------------------------------------------------
         * ROOTFS STAGING
         * ------------------------------------------------
         */
        LinuxRootfsStagingManager
            .prepare()

        /*
         * ------------------------------------------------
         * NATIVE PROOT RUNTIME
         * ------------------------------------------------
         */
        LinuxNativeRuntimeResolver
            .initialize(
                applicationContext
            )

        /*
         * ------------------------------------------------
         * QUICK START
         * ------------------------------------------------
         *
         * This does nothing unless the user has explicitly
         * enabled Quick Start.
         *
         * LinuxQuickStartManager also independently checks:
         *
         * - Ubuntu is installed
         * - installation is not in progress
         * - runtime is not already running
         * - device Linux capability is available
         * - safety mode is strictly NORMAL
         *
         * SAFE_MODE and RECOVERY_ARMED are never started
         * automatically.
         */
        lifecycleScope
            .launch {

                LinuxQuickStartManager
                    .startIfEligible()
            }

        /*
         * ------------------------------------------------
         * COMPOSE UI
         * ------------------------------------------------
         */
        setContent {

            var darkModeEnabled by
            remember {

                mutableStateOf(
                    false
                )
            }

            var showSplash by
            remember {

                mutableStateOf(
                    true
                )
            }

            val safetySnapshot by
            LinuxRuntimeCircuitBreaker
                .snapshotFlow
                .collectAsState()

            /*
             * Atlas splash remains visible while startup
             * initialization and optional Quick Start can
             * occur in the background.
             */
            LaunchedEffect(
                Unit
            ) {

                delay(
                    1800.milliseconds
                )

                showSplash =
                    false
            }

            PocketLabTheme(
                darkTheme =
                    darkModeEnabled,

                dynamicColor =
                    false
            ) {

                if (
                    showSplash
                ) {

                    AtlasSplashScreen()

                } else {

                    Column(
                        modifier =
                            Modifier
                                .fillMaxSize()
                    ) {

                        AtlasSafetyBanner(
                            snapshot =
                                safetySnapshot
                        )

                        Box(
                            modifier =
                                Modifier
                                    .weight(
                                        1f
                                    )
                                    .fillMaxSize()
                        ) {

                            AtlasNavigation(
                                darkModeEnabled =
                                    darkModeEnabled,

                                onDarkModeChanged = {
                                        enabled ->

                                    darkModeEnabled =
                                        enabled
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}