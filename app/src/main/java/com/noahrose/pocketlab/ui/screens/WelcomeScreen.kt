package com.noahrose.pocketlab.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.noahrose.pocketlab.R

@Composable
fun WelcomeScreen(
    onEnterAtlas: () -> Unit
) {

    Surface(
        modifier =
            Modifier
                .fillMaxSize(),
        color =
            MaterialTheme
                .colorScheme
                .background
    ) {

        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(
                        horizontal =
                            28.dp,
                        vertical =
                            32.dp
                    ),
            horizontalAlignment =
                Alignment.CenterHorizontally,
            verticalArrangement =
                Arrangement.Center
        ) {

            Image(
                painter =
                    painterResource(
                        id =
                            R.drawable
                                .atlas_cyberdeck_emblem
                    ),
                contentDescription =
                    "Atlas Cyberdeck emblem",
                modifier =
                    Modifier
                        .size(
                            112.dp
                        )
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
                    "ATLAS CYBERDECK",
                style =
                    MaterialTheme
                        .typography
                        .headlineMedium,
                fontWeight =
                    FontWeight.Bold,
                textAlign =
                    TextAlign.Center
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
                    "Your Cyberdeck. Anywhere.",
                style =
                    MaterialTheme
                        .typography
                        .titleMedium,
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
                            24.dp
                        )
            )

            Text(
                text =
                    "A portable Linux workspace built to make powerful tools practical and approachable on Android.",
                style =
                    MaterialTheme
                        .typography
                        .bodyLarge,
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
                            28.dp
                        )
            )

            WelcomeCapability(
                symbol =
                    "🐧",
                text =
                    "Real Ubuntu ARM64"
            )

            WelcomeCapability(
                symbol =
                    "✓",
                text =
                    "No Android root required"
            )

            WelcomeCapability(
                symbol =
                    "⌘",
                text =
                    "Atlas shell and Ubuntu remain separate"
            )

            WelcomeCapability(
                symbol =
                    "◆",
                text =
                    "Your Linux workspace persists between sessions"
            )

            Spacer(
                modifier =
                    Modifier
                        .height(
                            28.dp
                        )
            )

            Text(
                text =
                    "Ubuntu can be installed and managed from Linux Manager.",
                style =
                    MaterialTheme
                        .typography
                        .bodySmall,
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
                            24.dp
                        )
            )

            Button(
                onClick =
                    onEnterAtlas,
                modifier =
                    Modifier
                        .fillMaxWidth()
            ) {

                Text(
                    text =
                        "ENTER ATLAS"
                )
            }
        }
    }
}

@Composable
private fun WelcomeCapability(
    symbol: String,
    text: String
) {

    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(
                    vertical =
                        6.dp
                ),
        verticalAlignment =
            Alignment.CenterVertically
    ) {

        Text(
            text =
                symbol,
            style =
                MaterialTheme
                    .typography
                    .titleMedium,
            color =
                MaterialTheme
                    .colorScheme
                    .primary
        )

        Spacer(
            modifier =
                Modifier
                    .padding(
                        horizontal =
                            8.dp
                    )
        )

        Text(
            text =
                text,
            style =
                MaterialTheme
                    .typography
                    .bodyMedium
        )
    }
}