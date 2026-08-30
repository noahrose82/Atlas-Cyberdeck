package com.noahrose.pocketlab.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.noahrose.pocketlab.feature.settings.AtlasSettingsRepository

@Composable
fun SettingsScreen(
    darkModeEnabled: Boolean,
    onDarkModeChanged: (Boolean) -> Unit
) {

    val linuxQuickStartEnabled by
    AtlasSettingsRepository
        .linuxQuickStartEnabled
        .collectAsState()

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(
                    20.dp
                )
    ) {

        Text(
            text =
                "Settings",

            style =
                MaterialTheme
                    .typography
                    .headlineLarge
        )

        Spacer(
            modifier =
                Modifier
                    .height(
                        24.dp
                    )
        )

        /*
         * ------------------------------------------------
         * APPEARANCE
         * ------------------------------------------------
         */
        Text(
            text =
                "Appearance",

            style =
                MaterialTheme
                    .typography
                    .titleSmall,

            color =
                MaterialTheme
                    .colorScheme
                    .primary
        )

        Spacer(
            modifier =
                Modifier
                    .height(
                        8.dp
                    )
        )

        SettingSwitchCard(
            title =
                "Matrix Mode",

            description =
                "Use the neon-green Atlas Cyberdeck theme.",

            checked =
                darkModeEnabled,

            onCheckedChange =
                onDarkModeChanged
        )

        Spacer(
            modifier =
                Modifier
                    .height(
                        24.dp
                    )
        )

        /*
         * ------------------------------------------------
         * LINUX RUNTIME
         * ------------------------------------------------
         */
        Text(
            text =
                "Linux Runtime",

            style =
                MaterialTheme
                    .typography
                    .titleSmall,

            color =
                MaterialTheme
                    .colorScheme
                    .primary
        )

        Spacer(
            modifier =
                Modifier
                    .height(
                        8.dp
                    )
        )

        SettingSwitchCard(
            title =
                "Quick Start",

            description =
                "Automatically start Ubuntu when Atlas Cyberdeck launches.",

            checked =
                linuxQuickStartEnabled,

            onCheckedChange = { enabled ->

                AtlasSettingsRepository
                    .setLinuxQuickStartEnabled(
                        enabled
                    )
            }
        )

        Spacer(
            modifier =
                Modifier
                    .height(
                        8.dp
                    )
        )

        Text(
            text =
                "Quick Start only runs when Ubuntu is installed, " +
                        "Linux is available, and runtime safety is NORMAL.",

            style =
                MaterialTheme
                    .typography
                    .bodySmall,

            color =
                MaterialTheme
                    .colorScheme
                    .onSurfaceVariant
        )

        Spacer(
            modifier =
                Modifier
                    .height(
                        24.dp
                    )
        )

        Text(
            text =
                "Atlas Cyberdeck v0.13.0-alpha",

            style =
                MaterialTheme
                    .typography
                    .bodySmall,

            color =
                MaterialTheme
                    .colorScheme
                    .onSurfaceVariant
        )
    }
}

@Composable
private fun SettingSwitchCard(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {

    Card(
        modifier =
            Modifier
                .fillMaxWidth()
    ) {

        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(
                        16.dp
                    ),

            verticalAlignment =
                Alignment.CenterVertically
        ) {

            Column(
                modifier =
                    Modifier
                        .weight(
                            1f
                        )
            ) {

                Text(
                    text =
                        title,

                    style =
                        MaterialTheme
                            .typography
                            .titleMedium
                )

                Spacer(
                    modifier =
                        Modifier
                            .height(
                                4.dp
                            )
                )

                Text(
                    text =
                        description,

                    style =
                        MaterialTheme
                            .typography
                            .bodyMedium,

                    color =
                        MaterialTheme
                            .colorScheme
                            .onSurfaceVariant
                )
            }

            Switch(
                checked =
                    checked,

                onCheckedChange =
                    onCheckedChange
            )
        }
    }
}