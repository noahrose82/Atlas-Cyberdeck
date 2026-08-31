package com.noahrose.pocketlab.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp

@Composable
fun AtlasBreadcrumbBar(
    currentPath: String,
    onPathSelected: (String) -> Unit
) {

    val scrollState =
        rememberScrollState()

    val normalizedPath =
        currentPath
            .trim()
            .replace(
                '\\',
                '/'
            )
            .removeSuffix("/")

    val segments =
        if (
            normalizedPath == "~" ||
            normalizedPath.isBlank()
        ) {

            emptyList()

        } else {

            normalizedPath
                .removePrefix("~/")
                .split("/")
                .filter {
                    it.isNotBlank()
                }
        }

    Row(
        modifier =
            Modifier
                .horizontalScroll(
                    scrollState
                ),

        verticalAlignment =
            Alignment.CenterVertically
    ) {

        /*
         * Root
         */
        TextButton(
            onClick = {

                onPathSelected(
                    "~"
                )
            }
        ) {

            Text(
                text =
                    "~",

                style =
                    MaterialTheme
                        .typography
                        .bodyMedium,

                fontFamily =
                    FontFamily.Monospace,

                color =
                    MaterialTheme
                        .colorScheme
                        .primary
            )
        }

        var accumulatedPath =
            "~"

        segments
            .forEach { segment ->

                accumulatedPath =
                    if (accumulatedPath == "~") {

                        "~/$segment"

                    } else {

                        "$accumulatedPath/$segment"
                    }

                val destinationPath =
                    accumulatedPath

                Spacer(
                    modifier =
                        Modifier.width(
                            2.dp
                        )
                )

                Text(
                    text =
                        "›",

                    style =
                        MaterialTheme
                            .typography
                            .bodyMedium,

                    color =
                        MaterialTheme
                            .colorScheme
                            .onSurfaceVariant
                )

                Spacer(
                    modifier =
                        Modifier.width(
                            2.dp
                        )
                )

                TextButton(
                    onClick = {

                        onPathSelected(
                            destinationPath
                        )
                    }
                ) {

                    Text(
                        text =
                            segment,

                        style =
                            MaterialTheme
                                .typography
                                .bodyMedium,

                        fontFamily =
                            FontFamily.Monospace,

                        color =
                            if (
                                destinationPath ==
                                normalizedPath
                            ) {

                                MaterialTheme
                                    .colorScheme
                                    .onSurface

                            } else {

                                MaterialTheme
                                    .colorScheme
                                    .primary
                            }
                    )
                }
            }
    }
}