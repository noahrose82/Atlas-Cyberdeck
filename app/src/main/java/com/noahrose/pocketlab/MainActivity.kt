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
import com.noahrose.pocketlab.feature.linux.repository.LinuxRepository
import com.noahrose.pocketlab.feature.linux.rootfs.filesystem.LinuxRootfsStagingManager
import com.noahrose.pocketlab.feature.linux.runtime.filesystem.LinuxRuntimeFilesystemManager
import com.noahrose.pocketlab.feature.linux.runtime.filesystem.LinuxRuntimePathManager
import com.noahrose.pocketlab.feature.linux.runtime.platform.LinuxNativeRuntimeResolver
import com.noahrose.pocketlab.feature.linux.runtime.safety.LinuxRuntimeCircuitBreaker
import com.noahrose.pocketlab.ui.components.AtlasSafetyBanner
import com.noahrose.pocketlab.ui.navigation.AtlasNavigation
import com.noahrose.pocketlab.ui.screens.AtlasSplashScreen
import com.noahrose.pocketlab.ui.theme.PocketLabTheme
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

class MainActivity : ComponentActivity() {

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {

        super.onCreate(
            savedInstanceState
        )

        LinuxRepository.initialize(
            applicationContext
        )

        LinuxRuntimePathManager.initialize(
            applicationContext
        )

        /*
         * H4D — restore the persisted safety record only
         * after runtime paths are available. This also seeds
         * the H4C StateFlow before Compose begins observing it.
         */
        LinuxRuntimeCircuitBreaker
            .getSnapshot()

        LinuxRuntimeFilesystemManager.prepare()

        LinuxRootfsStagingManager.prepare()

        LinuxNativeRuntimeResolver.initialize(
            applicationContext
        )

        setContent {

            var darkModeEnabled by remember {
                mutableStateOf(false)
            }

            var showSplash by remember {
                mutableStateOf(true)
            }

            val safetySnapshot by
            LinuxRuntimeCircuitBreaker
                .snapshotFlow
                .collectAsState()

            LaunchedEffect(Unit) {

                delay(
                    1800.milliseconds
                )

                showSplash = false
            }

            PocketLabTheme(
                darkTheme = darkModeEnabled,
                dynamicColor = false
            ) {

                if (showSplash) {

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
                                    darkModeEnabled = it
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

