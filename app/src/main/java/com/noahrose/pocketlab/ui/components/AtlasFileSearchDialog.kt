package com.noahrose.pocketlab.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.noahrose.pocketlab.feature.filesystem.VirtualFileSystem

@Composable
fun AtlasFileSearchDialog(
    onDismiss: () -> Unit
) {

    var searchText by
    remember {
        mutableStateOf("")
    }

    var results by
    remember {
        mutableStateOf<List<String>>(
            emptyList()
        )
    }

    val cleanSearch =
        searchText.trim()

    val searchReady =
        cleanSearch.length >= 2

    AlertDialog(
        onDismissRequest =
            onDismiss,

        title = {

            Text(
                text =
                    "Search Atlas Files"
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
                        "Search the entire Atlas virtual filesystem. Results appear automatically as you type.",

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
                        Modifier.height(
                            12.dp
                        )
                )

                OutlinedTextField(
                    modifier =
                        Modifier
                            .fillMaxWidth(),

                    value =
                        searchText,

                    onValueChange = { value ->

                        searchText =
                            value

                        val query =
                            value.trim()

                        /*
                         * Live search begins as soon
                         * as the user enters at least
                         * two characters.
                         */
                        results =
                            if (query.length >= 2) {

                                VirtualFileSystem
                                    .find(
                                        query
                                    )

                            } else {

                                emptyList()
                            }
                    },

                    singleLine =
                        true,

                    label = {

                        Text(
                            text =
                                "File or folder name"
                        )
                    },

                    supportingText = {

                        when {

                            cleanSearch.isEmpty() -> {

                                Text(
                                    text =
                                        "Type at least 2 characters."
                                )
                            }

                            cleanSearch.length < 2 -> {

                                Text(
                                    text =
                                        "Enter 1 more character to search."
                                )
                            }

                            else -> {

                                Text(
                                    text =
                                        if (results.size == 1) {
                                            "1 match"
                                        } else {
                                            "${results.size} matches"
                                        }
                                )
                            }
                        }
                    }
                )

                /*
                 * ------------------------------------------------
                 * LIVE RESULTS
                 * ------------------------------------------------
                 */
                if (searchReady) {

                    Spacer(
                        modifier =
                            Modifier.height(
                                8.dp
                            )
                    )

                    HorizontalDivider()

                    Spacer(
                        modifier =
                            Modifier.height(
                                12.dp
                            )
                    )

                    if (results.isEmpty()) {

                        Text(
                            text =
                                "No files or folders containing \"$cleanSearch\" were found.",

                            style =
                                MaterialTheme
                                    .typography
                                    .bodyMedium,

                            color =
                                MaterialTheme
                                    .colorScheme
                                    .onSurfaceVariant
                        )

                    } else {

                        Text(
                            text =
                                if (results.size == 1) {
                                    "1 result"
                                } else {
                                    "${results.size} results"
                                },

                            style =
                                MaterialTheme
                                    .typography
                                    .labelLarge,

                            color =
                                MaterialTheme
                                    .colorScheme
                                    .primary
                        )

                        Spacer(
                            modifier =
                                Modifier.height(
                                    4.dp
                                )
                        )

                        Text(
                            text =
                                "Matches containing \"$cleanSearch\"",

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
                                Modifier.height(
                                    8.dp
                                )
                        )

                        LazyColumn(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .heightIn(
                                        max = 300.dp
                                    )
                        ) {

                            items(
                                items =
                                    results,

                                key = {
                                    it
                                }
                            ) { path ->

                                SearchResultRow(
                                    path =
                                        path
                                )
                            }
                        }
                    }
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
                        "Close"
                )
            }
        }
    )
}

@Composable
private fun SearchResultRow(
    path: String
) {

    val isDirectory =
        VirtualFileSystem
            .getDirectoryPaths()
            .any { directoryPath ->

                directoryPath.equals(
                    path,
                    ignoreCase = true
                )
            }

    Surface(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(
                    vertical = 3.dp
                ),

        shape =
            MaterialTheme
                .shapes
                .small,

        tonalElevation =
            1.dp
    ) {

        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = 12.dp,
                        vertical = 10.dp
                    )
        ) {

            Text(
                text =
                    if (isDirectory) {
                        "📁  ${path.substringAfterLast("/")}"
                    } else {
                        "📄  ${path.substringAfterLast("/")}"
                    },

                style =
                    MaterialTheme
                        .typography
                        .bodyMedium,

                maxLines =
                    1,

                overflow =
                    TextOverflow.Ellipsis
            )

            Spacer(
                modifier =
                    Modifier.height(
                        3.dp
                    )
            )

            Text(
                text =
                    path,

                style =
                    MaterialTheme
                        .typography
                        .bodySmall,

                fontFamily =
                    FontFamily.Monospace,

                color =
                    MaterialTheme
                        .colorScheme
                        .onSurfaceVariant,

                maxLines =
                    1,

                overflow =
                    TextOverflow.Ellipsis
            )
        }
    }
}