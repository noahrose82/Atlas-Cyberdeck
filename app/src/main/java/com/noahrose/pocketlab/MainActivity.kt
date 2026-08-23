package com.noahrose.pocketlab

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.noahrose.pocketlab.feature.linux.repository.LinuxRepository
import com.noahrose.pocketlab.feature.linux.rootfs.filesystem.LinuxRootfsStagingManager
import com.noahrose.pocketlab.feature.linux.runtime.filesystem.LinuxRuntimeFilesystemManager
import com.noahrose.pocketlab.feature.linux.runtime.filesystem.LinuxRuntimePathManager
import com.noahrose.pocketlab.feature.linux.runtime.platform.LinuxNativeRuntimeResolver
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

        /*
         * Restore persistent Linux installation metadata.
         */
        LinuxRepository.initialize(
            applicationContext
        )

        /*
         * Resolve Atlas runtime filesystem locations.
         */
        LinuxRuntimePathManager.initialize(
            applicationContext
        )

        /*
         * Prepare and validate writable runtime storage.
         */
        LinuxRuntimeFilesystemManager.prepare()

        /*
         * Prepare Ubuntu rootfs archive staging storage.
         */
        LinuxRootfsStagingManager.prepare()

        /*
         * Resolve native runtime executables installed
         * from the signed Atlas APK.
         */
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