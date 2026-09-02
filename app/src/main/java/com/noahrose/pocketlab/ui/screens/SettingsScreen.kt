package com.noahrose.pocketlab.ui.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.noahrose.pocketlab.R
import com.noahrose.pocketlab.feature.settings.AtlasSettingsRepository
import kotlin.math.roundToInt

@Composable
fun SettingsScreen(
    darkModeEnabled: Boolean,
    onDarkModeChanged: (Boolean) -> Unit
) {

    val linuxQuickStartEnabled by
    AtlasSettingsRepository
        .linuxQuickStartEnabled
        .collectAsState()

    var showCredits by
    remember {
        mutableStateOf(
            false
        )
    }

    var showOpenSource by
    remember {
        mutableStateOf(
            false
        )
    }

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .verticalScroll(
                    rememberScrollState()
                )
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
        SettingsSectionTitle(
            text =
                "Appearance"
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
        SettingsSectionTitle(
            text =
                "Linux Runtime"
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

        /*
         * ------------------------------------------------
         * ABOUT
         * ------------------------------------------------
         */
        SettingsSectionTitle(
            text =
                "About"
        )

        Spacer(
            modifier =
                Modifier
                    .height(
                        8.dp
                    )
        )

        AboutAtlasCard(
            onCreditsClick = {
                showCredits =
                    true
            },

            onOpenSourceClick = {
                showOpenSource =
                    true
            }
        )

        Spacer(
            modifier =
                Modifier
                    .height(
                        18.dp
                    )
        )

        /*
         * ------------------------------------------------
         * ATLAS CAT
         * ------------------------------------------------
         *
         * Settings-only personality detail.
         *
         * It stays outside the navigation bar and never
         * interferes with app controls.
         */
        SettingsCatWalk()

        Spacer(
            modifier =
                Modifier
                    .height(
                        8.dp
                    )
        )


        Spacer(
            modifier =
                Modifier
                    .height(
                        12.dp
                    )
        )
    }

    if (
        showCredits
    ) {

        CreditsDialog(
            onDismiss = {
                showCredits =
                    false
            }
        )
    }

    if (
        showOpenSource
    ) {

        OpenSourceDialog(
            onDismiss = {
                showOpenSource =
                    false
            }
        )
    }
}

@Composable
private fun SettingsSectionTitle(
    text: String
) {

    Text(
        text =
            text,

        style =
            MaterialTheme
                .typography
                .titleSmall,

        color =
            MaterialTheme
                .colorScheme
                .primary
    )
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

@Composable
private fun AboutAtlasCard(
    onCreditsClick: () -> Unit,
    onOpenSourceClick: () -> Unit
) {

    Card(
        modifier =
            Modifier
                .fillMaxWidth()
    ) {

        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(
                        20.dp
                    ),

            horizontalAlignment =
                Alignment.CenterHorizontally
        ) {

            /*
             * Atlas Labs emblem.
             */
            Image(
                painter =
                    painterResource(
                        id =
                            R.drawable.atlas_cyberdeck_emblem
                    ),

                contentDescription =
                    "Atlas Labs emblem",

                modifier =
                    Modifier
                        .size(
                            96.dp
                        ),

                contentScale =
                    ContentScale.Fit
            )

            Spacer(
                modifier =
                    Modifier
                        .height(
                            12.dp
                        )
            )

            Text(
                text =
                    "Atlas Cyberdeck",

                style =
                    MaterialTheme
                        .typography
                        .titleLarge,

                fontWeight =
                    FontWeight.Bold,

                textAlign =
                    TextAlign.Center
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
                    "Your Cyberdeck. Anywhere.",

                style =
                    MaterialTheme
                        .typography
                        .bodyMedium,

                color =
                    MaterialTheme
                        .colorScheme
                        .primary,

                textAlign =
                    TextAlign.Center
            )

            Spacer(
                modifier =
                    Modifier
                        .height(
                            14.dp
                        )
            )

            Text(
                text =
                    "A portable Linux workspace built to make powerful " +
                            "tools practical and approachable on Android.",

                modifier =
                    Modifier
                        .fillMaxWidth(),

                style =
                    MaterialTheme
                        .typography
                        .bodyMedium,

                color =
                    MaterialTheme
                        .colorScheme
                        .onSurfaceVariant,

                textAlign =
                    TextAlign.Center
            )

            Spacer(
                modifier =
                    Modifier
                        .height(
                            16.dp
                        )
            )

            Text(
                text =
                    "Developed by Atlas Labs",

                style =
                    MaterialTheme
                        .typography
                        .bodyMedium,

                fontWeight =
                    FontWeight.SemiBold
            )

            Spacer(
                modifier =
                    Modifier
                        .height(
                            2.dp
                        )
            )

            Text(
                text =
                    "Created by Noah Rose",

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
                            12.dp
                        )
            )

            Text(
                text =
                    "v0.13.0-alpha",

                style =
                    MaterialTheme
                        .typography
                        .labelMedium,

                color =
                    MaterialTheme
                        .colorScheme
                        .onSurfaceVariant
            )

            Spacer(
                modifier =
                    Modifier
                        .height(
                            10.dp
                        )
            )

            Row(
                modifier =
                    Modifier
                        .fillMaxWidth(),

                horizontalArrangement =
                    Arrangement.Center,

                verticalAlignment =
                    Alignment.CenterVertically
            ) {

                TextButton(
                    onClick =
                        onCreditsClick
                ) {

                    Text(
                        text =
                            "Credits"
                    )
                }

                Spacer(
                    modifier =
                        Modifier
                            .width(
                                6.dp
                            )
                )

                TextButton(
                    onClick =
                        onOpenSourceClick
                ) {

                    Text(
                        text =
                            "Open Source"
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingsCatWalk() {

    BoxWithConstraints(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(
                    32.dp
                )
    ) {

        val density =
            LocalDensity.current

        val catWidth =
            30.dp

        val travelDistance =
            with(
                density
            ) {

                (
                        maxWidth -
                                catWidth
                        )
                    .toPx()
                    .coerceAtLeast(
                        0f
                    )
            }

        val transition =
            rememberInfiniteTransition(
                label =
                    "atlasCatWalk"
            )

        val catPosition by
        transition.animateFloat(
            initialValue =
                0f,

            targetValue =
                travelDistance,

            animationSpec =
                infiniteRepeatable(
                    animation =
                        tween(
                            durationMillis =
                                14000,

                            easing =
                                LinearEasing
                        ),

                    repeatMode =
                        RepeatMode.Reverse
                ),

            label =
                "atlasCatPosition"
        )

        Text(
            text =
                "🐈‍⬛",

            modifier =
                Modifier
                    .offset {

                        IntOffset(
                            x =
                                catPosition
                                    .roundToInt(),

                            y =
                                0
                        )
                    },

            fontSize =
                20.sp
        )
    }
}

@Composable
private fun CreditsDialog(
    onDismiss: () -> Unit
) {

    AlertDialog(
        onDismissRequest =
            onDismiss,

        title = {

            Text(
                text =
                    "Credits"
            )
        },

        text = {

            Column {

                Text(
                    text =
                        "Atlas Cyberdeck",

                    style =
                        MaterialTheme
                            .typography
                            .titleMedium,

                    fontWeight =
                        FontWeight.Bold
                )

                Spacer(
                    modifier =
                        Modifier
                            .height(
                                10.dp
                            )
                )

                Text(
                    text =
                        "Created by Noah Rose"
                )

                Text(
                    text =
                        "Developed by Atlas Labs"
                )

                Spacer(
                    modifier =
                        Modifier
                            .height(
                                12.dp
                            )
                )

                Text(
                    text =
                        "Built with Kotlin, Jetpack Compose, Linux, " +
                                "open-source software, and the belief that " +
                                "powerful tools should still feel approachable.",

                    color =
                        MaterialTheme
                            .colorScheme
                            .onSurfaceVariant
                )
            }
        },

        confirmButton = {

            TextButton(
                onClick =
                    onDismiss
            ) {

                Text(
                    text =
                        "Close"
                )
            }
        }
    )
}

@Composable
private fun OpenSourceDialog(
    onDismiss: () -> Unit
) {

    AlertDialog(
        onDismissRequest =
            onDismiss,

        title = {

            Text(
                text =
                    "Open Source"
            )
        },

        text = {

            Column {

                Text(
                    text =
                        "Atlas Cyberdeck is built with open-source software " +
                                "and open technologies.",

                    color =
                        MaterialTheme
                            .colorScheme
                            .onSurfaceVariant
                )

                Spacer(
                    modifier =
                        Modifier
                            .height(
                                12.dp
                            )
                )

                Text(
                    text =
                        "Key components include:",

                    fontWeight =
                        FontWeight.SemiBold
                )

                Spacer(
                    modifier =
                        Modifier
                            .height(
                                6.dp
                            )
                )

                Text(
                    text =
                        "• Android / Jetpack Compose"
                )

                Text(
                    text =
                        "• Ubuntu Linux"
                )

                Text(
                    text =
                        "• PRoot"
                )

                Text(
                    text =
                        "• ConnectBot terminal libraries"
                )

                Spacer(
                    modifier =
                        Modifier
                            .height(
                                12.dp
                            )
                )

                Text(
                    text =
                        "Complete third-party license notices will be " +
                                "maintained with the Atlas Cyberdeck release.",

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
        },

        confirmButton = {

            TextButton(
                onClick =
                    onDismiss
            ) {

                Text(
                    text =
                        "Close"
                )
            }
        }
    )
}