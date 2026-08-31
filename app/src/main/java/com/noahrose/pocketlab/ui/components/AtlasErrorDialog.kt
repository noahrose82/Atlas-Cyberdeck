package com.noahrose.pocketlab.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.noahrose.pocketlab.feature.system.error.AtlasError

@Composable
fun AtlasErrorDialog(
    error: AtlasError,
    onDismiss: () -> Unit
) {

    val scrollState =
        rememberScrollState()

    AlertDialog(
        onDismissRequest =
            onDismiss,

        title = {

            Text(
                text =
                    error.title
            )
        },

        text = {

            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .heightIn(
                            max = 460.dp
                        )
                        .verticalScroll(
                            scrollState
                        )
            ) {

                ErrorSectionTitle(
                    text =
                        "What happened"
                )

                Text(
                    text =
                        error.whatHappened,

                    style =
                        MaterialTheme
                            .typography
                            .bodyMedium
                )

                Spacer(
                    modifier =
                        Modifier.height(
                            16.dp
                        )
                )

                ErrorSectionTitle(
                    text =
                        "Why"
                )

                Text(
                    text =
                        error.whyItHappened,

                    style =
                        MaterialTheme
                            .typography
                            .bodyMedium
                )

                Spacer(
                    modifier =
                        Modifier.height(
                            16.dp
                        )
                )

                ErrorSectionTitle(
                    text =
                        "What happened to your data"
                )

                Text(
                    text =
                        error.dataImpact,

                    style =
                        MaterialTheme
                            .typography
                            .bodyMedium
                )

                if (
                    error.nextSteps.isNotEmpty()
                ) {

                    Spacer(
                        modifier =
                            Modifier.height(
                                16.dp
                            )
                    )

                    ErrorSectionTitle(
                        text =
                            "What you can do"
                    )

                    error.nextSteps
                        .forEach { step ->

                            Text(
                                text =
                                    "• $step",

                                modifier =
                                    Modifier
                                        .fillMaxWidth(),

                                style =
                                    MaterialTheme
                                        .typography
                                        .bodyMedium
                            )
                        }
                }

                Spacer(
                    modifier =
                        Modifier.height(
                            20.dp
                        )
                )

                HorizontalDivider()

                Spacer(
                    modifier =
                        Modifier.height(
                            12.dp
                        )
                )

                Text(
                    text =
                        "Error code",

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
                        Modifier.height(
                            6.dp
                        )
                )

                Surface(
                    modifier =
                        Modifier
                            .fillMaxWidth(),

                    shape =
                        MaterialTheme
                            .shapes
                            .small,

                    tonalElevation =
                        2.dp
                ) {

                    Text(
                        text =
                            error.code,

                        modifier =
                            Modifier
                                .fillMaxWidth(),

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
            }
        },

        confirmButton = {

            TextButton(
                onClick =
                    onDismiss
            ) {

                Text(
                    text =
                        "OK"
                )
            }
        }
    )
}

@Composable
private fun ErrorSectionTitle(
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

    Spacer(
        modifier =
            Modifier.height(
                6.dp
            )
    )
}