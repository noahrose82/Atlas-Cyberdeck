package com.noahrose.pocketlab

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.noahrose.pocketlab.ui.navigation.AtlasNavigation
import com.noahrose.pocketlab.ui.screens.AtlasSplashScreen
import com.noahrose.pocketlab.ui.theme.PocketLabTheme
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(savedInstanceState)

        setContent {

            var darkModeEnabled by remember {
                mutableStateOf(false)
            }

            var showSplash by remember {
                mutableStateOf(true)
            }

            LaunchedEffect(Unit) {

                delay(
                    1800L
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
                        darkModeEnabled = darkModeEnabled,
                        onDarkModeChanged = {
                            darkModeEnabled = it
                        }
                    )
                }
            }
        }
    }
}