package com.noahrose.pocketlab.ui.components

import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.noahrose.pocketlab.feature.filesystem.VirtualFileSystem

@Composable
fun AtlasFileTreeDialog(
    onDismiss: () -> Unit
) {

    val currentPath by
    VirtualFileSystem
        .currentPath
        .collectAsState()

    val treeLines =
        VirtualFileSystem
            .buildTree()

    val verticalScrollState =
        rememberScrollState()

    val horizontalScrollState =
        rememberScrollState()

    AlertDialog(
        onDismissRequest =
            onDismiss,

        title = {

            Text(
                text =
                    "Atlas File Tree"
            )
        },

        text = {

            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
            ) {

                Text(
                    text =
                        "Current Location",

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
                            4.dp
                        )
                )

                Text(
                    text =
                        currentPath,

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

                Spacer(
                    modifier =
                        Modifier.height(
                            12.dp
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
                        "Filesystem Structure",

                    style =
                        MaterialTheme
                            .typography
                            .labelLarge
                )

                Spacer(
                    modifier =
                        Modifier.height(
                            8.dp
                        )
                )

                Surface(
                    modifier =
                        Modifier
                            .fillMaxWidth(),

                    shape =
                        MaterialTheme
                            .shapes
                            .medium,

                    tonalElevation =
                        2.dp
                ) {

                    if (treeLines.isEmpty()) {

                        Text(
                            text =
                                "No filesystem entries are available.",

                            modifier =
                                Modifier
                                    .fillMaxWidth(),

                            style =
                                MaterialTheme
                                    .typography
                                    .bodyMedium
                        )

                    } else {

                        Text(
                            text =
                                treeLines
                                    .joinToString(
                                        separator = "\n"
                                    ),

                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .heightIn(
                                        max = 360.dp
                                    )
                                    .verticalScroll(
                                        verticalScrollState
                                    )
                                    .horizontalScroll(
                                        horizontalScrollState
                                    ),

                            style =
                                MaterialTheme
                                    .typography
                                    .bodyMedium,

                            fontFamily =
                                FontFamily.Monospace
                        )
                    }
                }

                Spacer(
                    modifier =
                        Modifier.height(
                            10.dp
                        )
                )

                Text(
                    text =
                        "Tree view starts from the folder you are currently viewing.",

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