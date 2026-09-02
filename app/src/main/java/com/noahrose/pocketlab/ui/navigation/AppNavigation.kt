package com.noahrose.pocketlab.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.noahrose.pocketlab.feature.boot.BootScreen
import com.noahrose.pocketlab.feature.dashboard.DashboardScreen
import com.noahrose.pocketlab.feature.settings.AtlasSettingsRepository
import com.noahrose.pocketlab.ui.screens.FilesScreen
import com.noahrose.pocketlab.ui.screens.LinuxScreen
import com.noahrose.pocketlab.ui.screens.SettingsScreen
import com.noahrose.pocketlab.ui.screens.TerminalScreen
import com.noahrose.pocketlab.ui.screens.WelcomeScreen

private const val BOOT_ROUTE =
    "boot"

private const val WELCOME_ROUTE =
    "welcome"

private const val DASHBOARD_ROUTE =
    "dashboard"

private const val TERMINAL_ROUTE =
    "terminal"

private const val FILES_ROUTE =
    "files"

private const val LINUX_ROUTE =
    "linux"

private const val SETTINGS_ROUTE =
    "settings"

private data class NavigationItem(
    val route: String,
    val label: String,
    val symbol: String
)

@Composable
fun AtlasNavigation(
    darkModeEnabled: Boolean,
    onDarkModeChanged: (Boolean) -> Unit
) {

    val navController =
        rememberNavController()

    val navigationItems =
        listOf(
            NavigationItem(
                route =
                    DASHBOARD_ROUTE,
                label =
                    "Dashboard",
                symbol =
                    "⌂"
            ),
            NavigationItem(
                route =
                    TERMINAL_ROUTE,
                label =
                    "Terminal",
                symbol =
                    ">_"
            ),
            NavigationItem(
                route =
                    FILES_ROUTE,
                label =
                    "Files",
                symbol =
                    "▣"
            ),
            NavigationItem(
                route =
                    LINUX_ROUTE,
                label =
                    "Linux",
                symbol =
                    "◆"
            ),
            NavigationItem(
                route =
                    SETTINGS_ROUTE,
                label =
                    "Settings",
                symbol =
                    "⚙"
            )
        )

    val backStackEntry by
    navController
        .currentBackStackEntryAsState()

    val currentRoute =
        backStackEntry
            ?.destination
            ?.route

    val showBottomBar =
        currentRoute != null &&
                currentRoute != BOOT_ROUTE &&
                currentRoute != WELCOME_ROUTE

    Scaffold(
        bottomBar = {

            if (
                showBottomBar
            ) {

                NavigationBar {

                    navigationItems
                        .forEach { item ->

                            NavigationBarItem(
                                selected =
                                    currentRoute ==
                                            item.route,

                                onClick = {

                                    navController
                                        .navigate(
                                            item.route
                                        ) {

                                            popUpTo(
                                                navController
                                                    .graph
                                                    .findStartDestination()
                                                    .id
                                            ) {

                                                saveState =
                                                    true
                                            }

                                            launchSingleTop =
                                                true

                                            restoreState =
                                                true
                                        }
                                },

                                icon = {

                                    Text(
                                        text =
                                            item.symbol
                                    )
                                },

                                label = {

                                    Text(
                                        text =
                                            item.label
                                    )
                                }
                            )
                        }
                }
            }
        }
    ) { innerPadding ->

        NavHost(
            navController =
                navController,

            startDestination =
                BOOT_ROUTE,

            modifier =
                Modifier
                    .padding(
                        innerPadding
                    )
        ) {

            composable(
                BOOT_ROUTE
            ) {

                BootScreen(
                    onBootComplete = {

                        val destination =
                            if (
                                AtlasSettingsRepository
                                    .isOnboardingComplete()
                            ) {

                                DASHBOARD_ROUTE

                            } else {

                                WELCOME_ROUTE
                            }

                        navController
                            .navigate(
                                destination
                            ) {

                                popUpTo(
                                    BOOT_ROUTE
                                ) {

                                    inclusive =
                                        true
                                }

                                launchSingleTop =
                                    true
                            }
                    }
                )
            }

            composable(
                WELCOME_ROUTE
            ) {

                WelcomeScreen(
                    onEnterAtlas = {

                        AtlasSettingsRepository
                            .setOnboardingComplete(
                                true
                            )

                        navController
                            .navigate(
                                DASHBOARD_ROUTE
                            ) {

                                popUpTo(
                                    WELCOME_ROUTE
                                ) {

                                    inclusive =
                                        true
                                }

                                launchSingleTop =
                                    true
                            }
                    }
                )
            }

            composable(
                DASHBOARD_ROUTE
            ) {

                DashboardScreen(
                    onNavigateToTerminal = {

                        navController
                            .navigate(
                                TERMINAL_ROUTE
                            ) {

                                launchSingleTop =
                                    true
                            }
                    },

                    onNavigateToLinux = {

                        navController
                            .navigate(
                                LINUX_ROUTE
                            ) {

                                launchSingleTop =
                                    true
                            }
                    },

                    onNavigateToFiles = {

                        navController
                            .navigate(
                                FILES_ROUTE
                            ) {

                                launchSingleTop =
                                    true
                            }
                    },

                    onNavigateToSettings = {

                        navController
                            .navigate(
                                SETTINGS_ROUTE
                            ) {

                                launchSingleTop =
                                    true
                            }
                    }
                )
            }

            composable(
                TERMINAL_ROUTE
            ) {

                TerminalScreen()
            }

            composable(
                FILES_ROUTE
            ) {

                FilesScreen()
            }

            composable(
                LINUX_ROUTE
            ) {

                LinuxScreen(
                    onBack = {

                        navController
                            .navigate(
                                DASHBOARD_ROUTE
                            ) {

                                popUpTo(
                                    DASHBOARD_ROUTE
                                ) {

                                    inclusive =
                                        false
                                }

                                launchSingleTop =
                                    true
                            }
                    }
                )
            }

            composable(
                SETTINGS_ROUTE
            ) {

                SettingsScreen(
                    darkModeEnabled =
                        darkModeEnabled,

                    onDarkModeChanged =
                        onDarkModeChanged
                )
            }
        }
    }
}