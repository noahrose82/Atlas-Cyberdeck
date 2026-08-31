package com.noahrose.pocketlab.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.noahrose.pocketlab.feature.filesystem.FileNode
import com.noahrose.pocketlab.feature.filesystem.VirtualFileSystem
import com.noahrose.pocketlab.feature.system.error.AtlasError
import com.noahrose.pocketlab.feature.system.error.AtlasErrors
import com.noahrose.pocketlab.ui.components.AtlasBreadcrumbBar
import com.noahrose.pocketlab.ui.components.AtlasErrorDialog
import com.noahrose.pocketlab.ui.components.AtlasFileSearchDialog
import com.noahrose.pocketlab.ui.components.AtlasFileTreeDialog

@Composable
fun FilesScreen() {

    val currentPath by
    VirtualFileSystem
        .currentPath
        .collectAsState()

    val currentEntries by
    VirtualFileSystem
        .currentEntries
        .collectAsState()

    var createItemType by
    remember {
        mutableStateOf<CreateItemType?>(
            null
        )
    }

    var selectedFileName by
    remember {
        mutableStateOf<String?>(
            null
        )
    }

    var selectedFileContent by
    remember {
        mutableStateOf("")
    }

    var selectedFileEditing by
    remember {
        mutableStateOf(false)
    }

    var statusMessage by
    remember {
        mutableStateOf<String?>(
            null
        )
    }

    var atlasError by
    remember {
        mutableStateOf<AtlasError?>(
            null
        )
    }

    var renameEntry by
    remember {
        mutableStateOf<FileNode?>(
            null
        )
    }

    var copyFileName by
    remember {
        mutableStateOf<String?>(
            null
        )
    }

    var moveFileName by
    remember {
        mutableStateOf<String?>(
            null
        )
    }

    var moveDirectoryName by
    remember {
        mutableStateOf<String?>(
            null
        )
    }

    var detailsEntry by
    remember {
        mutableStateOf<FileNode?>(
            null
        )
    }

    var deleteEntry by
    remember {
        mutableStateOf<FileNode?>(
            null
        )
    }

    var showSearchDialog by
    remember {
        mutableStateOf(false)
    }

    var showTreeDialog by
    remember {
        mutableStateOf(false)
    }

    /*
     * ------------------------------------------------
     * DIRECT PATH NAVIGATION
     * ------------------------------------------------
     */
    fun navigateToAtlasDirectory(
        path: String
    ): Boolean {

        val normalizedPath =
            path
                .trim()
                .replace(
                    '\\',
                    '/'
                )
                .replace(
                    Regex("/+"),
                    "/"
                )
                .removeSuffix("/")

        if (
            normalizedPath != "~" &&
            !normalizedPath.startsWith("~/")
        ) {
            return false
        }

        val originalPath =
            currentPath

        fun walkPath(
            destinationPath: String
        ): Boolean {

            val returnedToRoot =
                VirtualFileSystem
                    .changeDirectory(
                        "~"
                    )

            if (!returnedToRoot) {
                return false
            }

            if (destinationPath == "~") {
                return true
            }

            val segments =
                destinationPath
                    .removePrefix("~/")
                    .split("/")
                    .filter {
                        it.isNotBlank()
                    }

            segments.forEach { segment ->

                val changed =
                    VirtualFileSystem
                        .changeDirectory(
                            segment
                        )

                if (!changed) {
                    return false
                }
            }

            return true
        }

        val success =
            walkPath(
                normalizedPath
            )

        /*
         * Never strand the user halfway through
         * a failed path navigation.
         */
        if (!success) {

            walkPath(
                originalPath
            )
        }

        return success
    }

    /*
     * ------------------------------------------------
     * OPEN FILE
     * ------------------------------------------------
     */
    fun openFile(
        fileName: String,
        editImmediately: Boolean
    ) {

        val content =
            VirtualFileSystem
                .readFile(
                    fileName
                )

        if (content == null) {

            atlasError =
                AtlasErrors
                    .itemNotFound(
                        fileName
                    )

            return
        }

        selectedFileName =
            fileName

        selectedFileContent =
            content

        selectedFileEditing =
            editImmediately

        statusMessage =
            null
    }

    /*
     * ------------------------------------------------
     * FILE VIEWER / EDITOR
     * ------------------------------------------------
     */
    if (selectedFileName != null) {

        val fileName =
            selectedFileName ?: ""

        BackHandler {

            if (selectedFileEditing) {

                selectedFileContent =
                    VirtualFileSystem
                        .readFile(
                            fileName
                        )
                        ?: selectedFileContent

                selectedFileEditing =
                    false

            } else {

                selectedFileName =
                    null

                selectedFileContent =
                    ""

                selectedFileEditing =
                    false
            }
        }

        AtlasFileViewer(
            fileName =
                fileName,

            fileContent =
                selectedFileContent,

            currentPath =
                currentPath,

            editing =
                selectedFileEditing,

            onContentChanged = {
                selectedFileContent =
                    it
            },

            onEdit = {
                selectedFileEditing =
                    true
            },

            onCancelEdit = {

                selectedFileContent =
                    VirtualFileSystem
                        .readFile(
                            fileName
                        )
                        ?: selectedFileContent

                selectedFileEditing =
                    false
            },

            onSave = {

                val saved =
                    VirtualFileSystem
                        .writeFile(
                            name =
                                fileName,

                            content =
                                selectedFileContent
                        )

                if (saved) {

                    selectedFileEditing =
                        false

                } else {

                    atlasError =
                        AtlasErrors
                            .filesystemOperationFailed(
                                "file save"
                            )
                }
            },

            onBack = {

                selectedFileName =
                    null

                selectedFileContent =
                    ""

                selectedFileEditing =
                    false
            }
        )

        return
    }

    /*
     * ------------------------------------------------
     * ANDROID BACK
     * ------------------------------------------------
     */
    if (currentPath != "~") {

        BackHandler {

            VirtualFileSystem
                .changeDirectory(
                    ".."
                )
        }
    }

    val sortedEntries =
        currentEntries
            .sortedWith(
                compareByDescending<FileNode> {
                    it.isDirectory
                }.thenBy {
                    it.name.lowercase()
                }
            )

    val directoryPaths =
        VirtualFileSystem
            .getDirectoryPaths()

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(
                    horizontal = 16.dp,
                    vertical = 12.dp
                )
    ) {

        Text(
            text =
                "Files",

            style =
                MaterialTheme
                    .typography
                    .headlineLarge
        )

        Spacer(
            modifier =
                Modifier.height(
                    4.dp
                )
        )

        Text(
            text =
                "Atlas Files",

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
                Modifier.height(
                    16.dp
                )
        )

        /*
         * ------------------------------------------------
         * BREADCRUMB LOCATION
         * ------------------------------------------------
         */
        Surface(
            modifier =
                Modifier.fillMaxWidth(),

            shape =
                MaterialTheme
                    .shapes
                    .medium,

            tonalElevation =
                2.dp
        ) {

            Column(
                modifier =
                    Modifier.padding(
                        horizontal = 10.dp,
                        vertical = 10.dp
                    )
            ) {

                Text(
                    text =
                        "Current Location",

                    modifier =
                        Modifier.padding(
                            horizontal = 4.dp
                        ),

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
                            2.dp
                        )
                )

                AtlasBreadcrumbBar(
                    currentPath =
                        currentPath,

                    onPathSelected = {
                            destinationPath ->

                        statusMessage =
                            null

                        val navigated =
                            navigateToAtlasDirectory(
                                destinationPath
                            )

                        if (!navigated) {

                            atlasError =
                                AtlasErrors
                                    .itemNotFound(
                                        destinationPath
                                    )
                        }
                    }
                )
            }
        }

        Spacer(
            modifier =
                Modifier.height(
                    12.dp
                )
        )

        /*
         * ------------------------------------------------
         * FILE ACTIONS
         * ------------------------------------------------
         */
        Row(
            modifier =
                Modifier.fillMaxWidth(),

            horizontalArrangement =
                Arrangement.spacedBy(
                    8.dp
                )
        ) {

            Button(
                modifier =
                    Modifier.weight(
                        1f
                    ),

                enabled =
                    currentPath != "~",

                onClick = {

                    statusMessage =
                        null

                    val changed =
                        VirtualFileSystem
                            .changeDirectory(
                                ".."
                            )

                    if (!changed) {

                        atlasError =
                            AtlasErrors
                                .filesystemOperationFailed(
                                    "directory navigation"
                                )
                    }
                }
            ) {

                Text(
                    "← Up"
                )
            }

            Button(
                modifier =
                    Modifier.weight(
                        1f
                    ),

                onClick = {

                    statusMessage =
                        null

                    createItemType =
                        CreateItemType.FILE
                }
            ) {

                Text(
                    "+ File"
                )
            }

            Button(
                modifier =
                    Modifier.weight(
                        1f
                    ),

                onClick = {

                    statusMessage =
                        null

                    createItemType =
                        CreateItemType.FOLDER
                }
            ) {

                Text(
                    "+ Folder"
                )
            }
        }

        Spacer(
            modifier =
                Modifier.height(
                    8.dp
                )
        )

        /*
         * ------------------------------------------------
         * SEARCH / TREE
         * ------------------------------------------------
         */
        Row(
            modifier =
                Modifier.fillMaxWidth(),

            horizontalArrangement =
                Arrangement.spacedBy(
                    8.dp
                )
        ) {

            Button(
                modifier =
                    Modifier.weight(
                        1f
                    ),

                onClick = {

                    statusMessage =
                        null

                    showSearchDialog =
                        true
                }
            ) {

                Text(
                    "Search"
                )
            }

            Button(
                modifier =
                    Modifier.weight(
                        1f
                    ),

                onClick = {

                    statusMessage =
                        null

                    showTreeDialog =
                        true
                }
            ) {

                Text(
                    "Tree View"
                )
            }
        }

        if (statusMessage != null) {

            Spacer(
                modifier =
                    Modifier.height(
                        10.dp
                    )
            )

            Text(
                text =
                    statusMessage ?: "",

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

        Spacer(
            modifier =
                Modifier.height(
                    16.dp
                )
        )

        HorizontalDivider()

        Spacer(
            modifier =
                Modifier.height(
                    8.dp
                )
        )

        /*
         * ------------------------------------------------
         * DIRECTORY CONTENTS
         * ------------------------------------------------
         */
        if (sortedEntries.isEmpty()) {

            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(
                            vertical = 40.dp
                        ),

                horizontalAlignment =
                    Alignment.CenterHorizontally
            ) {

                Text(
                    text =
                        "This folder is empty.",

                    style =
                        MaterialTheme
                            .typography
                            .bodyLarge,

                    color =
                        MaterialTheme
                            .colorScheme
                            .onSurfaceVariant
                )
            }

        } else {

            LazyColumn(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .weight(
                            1f
                        ),

                verticalArrangement =
                    Arrangement.spacedBy(
                        8.dp
                    )
            ) {

                items(
                    items =
                        sortedEntries,

                    key = { entry ->

                        "${currentPath}:${entry.name}:${entry.isDirectory}"
                    }
                ) { entry ->

                    FileEntryCard(
                        entry =
                            entry,

                        onOpen = {

                            statusMessage =
                                null

                            if (entry.isDirectory) {

                                val changed =
                                    VirtualFileSystem
                                        .changeDirectory(
                                            entry.name
                                        )

                                if (!changed) {

                                    atlasError =
                                        AtlasErrors
                                            .itemNotFound(
                                                entry.name
                                            )
                                }

                            } else {

                                openFile(
                                    fileName =
                                        entry.name,

                                    editImmediately =
                                        false
                                )
                            }
                        },

                        onEdit = {

                            openFile(
                                fileName =
                                    entry.name,

                                editImmediately =
                                    true
                            )
                        },

                        onRename = {

                            renameEntry =
                                entry
                        },

                        onCopy = {

                            copyFileName =
                                entry.name
                        },

                        onMove = {

                            if (entry.isDirectory) {

                                moveDirectoryName =
                                    entry.name

                            } else {

                                moveFileName =
                                    entry.name
                            }
                        },

                        onDetails = {

                            detailsEntry =
                                entry
                        },

                        onDelete = {

                            deleteEntry =
                                entry
                        }
                    )
                }
            }
        }
    }

    /*
     * ------------------------------------------------
     * SEARCH
     * ------------------------------------------------
     */
    if (showSearchDialog) {

        AtlasFileSearchDialog(
            onDismiss = {

                showSearchDialog =
                    false
            },

            onResultSelected = { resultPath ->

                val isDirectory =
                    VirtualFileSystem
                        .getDirectoryPaths()
                        .any { directoryPath ->

                            directoryPath.equals(
                                resultPath,
                                ignoreCase = true
                            )
                        }

                showSearchDialog =
                    false

                if (isDirectory) {

                    val navigated =
                        navigateToAtlasDirectory(
                            resultPath
                        )

                    if (!navigated) {

                        atlasError =
                            AtlasErrors
                                .itemNotFound(
                                    resultPath
                                )
                    }

                } else {

                    val fileName =
                        resultPath
                            .substringAfterLast("/")

                    val parentPath =
                        resultPath
                            .substringBeforeLast(
                                delimiter = "/",
                                missingDelimiterValue = "~"
                            )

                    val navigated =
                        navigateToAtlasDirectory(
                            parentPath
                        )

                    if (navigated) {

                        openFile(
                            fileName =
                                fileName,

                            editImmediately =
                                false
                        )

                    } else {

                        atlasError =
                            AtlasErrors
                                .itemNotFound(
                                    resultPath
                                )
                    }
                }
            }
        )
    }

    /*
     * ------------------------------------------------
     * TREE VIEW
     * ------------------------------------------------
     */
    if (showTreeDialog) {

        AtlasFileTreeDialog(
            onDismiss = {

                showTreeDialog =
                    false
            }
        )
    }

    /*
     * ------------------------------------------------
     * CREATE
     * ------------------------------------------------
     */
    createItemType
        ?.let { itemType ->

            CreateItemDialog(
                itemType =
                    itemType,

                onDismiss = {
                    createItemType =
                        null
                },

                onCreate = { name ->

                    if (name.isBlank()) {

                        atlasError =
                            AtlasErrors
                                .invalidName(
                                    name
                                )

                        createItemType =
                            null

                        return@CreateItemDialog
                    }

                    val success =
                        when (itemType) {

                            CreateItemType.FILE ->

                                VirtualFileSystem
                                    .createFile(
                                        name
                                    )

                            CreateItemType.FOLDER ->

                                VirtualFileSystem
                                    .createDirectory(
                                        name
                                    )
                        }

                    if (success) {

                        statusMessage =
                            if (
                                itemType ==
                                CreateItemType.FILE
                            ) {
                                "Created file: $name"
                            } else {
                                "Created folder: $name"
                            }

                    } else {

                        atlasError =
                            AtlasError(
                                code =
                                    "ATLAS-FS-409-DUPLICATE",

                                title =
                                    "Create Blocked",

                                whatHappened =
                                    "Atlas could not create \"$name\".",

                                whyItHappened =
                                    "An item with that name already exists in this folder.",

                                dataImpact =
                                    "Nothing was created or overwritten.",

                                nextSteps =
                                    listOf(
                                        "Choose a different name.",
                                        "Rename the existing item."
                                    )
                            )
                    }

                    createItemType =
                        null
                }
            )
        }

    /*
     * ------------------------------------------------
     * RENAME
     * ------------------------------------------------
     */
    renameEntry
        ?.let { entry ->

            RenameEntryDialog(
                entry =
                    entry,

                onDismiss = {

                    renameEntry =
                        null
                },

                onRename = { newName ->

                    val oldName =
                        entry.name

                    val duplicateExists =
                        currentEntries
                            .any { existing ->

                                existing !== entry &&
                                        existing.name.equals(
                                            newName,
                                            ignoreCase = true
                                        )
                            }

                    if (duplicateExists) {

                        atlasError =
                            AtlasError(
                                code =
                                    "ATLAS-FS-409-DUPLICATE",

                                title =
                                    "Rename Blocked",

                                whatHappened =
                                    "Atlas could not rename \"$oldName\" to \"$newName\".",

                                whyItHappened =
                                    "An item named \"$newName\" already exists in this folder.",

                                dataImpact =
                                    "Nothing was renamed, deleted, or overwritten.",

                                nextSteps =
                                    listOf(
                                        "Choose a different name.",
                                        "Rename the existing item first."
                                    )
                            )

                        renameEntry =
                            null

                    } else {

                        val success =
                            if (entry.isDirectory) {

                                VirtualFileSystem
                                    .renameDirectory(
                                        sourceName =
                                            oldName,

                                        destinationName =
                                            newName
                                    )

                            } else {

                                VirtualFileSystem
                                    .moveFile(
                                        sourceName =
                                            oldName,

                                        destinationName =
                                            newName
                                    )
                            }

                        if (success) {

                            statusMessage =
                                "Renamed $oldName to $newName."

                        } else {

                            atlasError =
                                AtlasErrors
                                    .filesystemOperationFailed(
                                        "rename"
                                    )
                        }

                        renameEntry =
                            null
                    }
                }
            )
        }

    /*
     * ------------------------------------------------
     * COPY FILE
     * ------------------------------------------------
     */
    copyFileName
        ?.let { sourceName ->

            val availableDestinations =
                directoryPaths
                    .filter {
                        it != currentPath
                    }

            DestinationPickerDialog(
                title =
                    "Copy File",

                itemName =
                    sourceName,

                description =
                    "Choose where to copy this file.",

                destinations =
                    availableDestinations,

                onDismiss = {

                    copyFileName =
                        null
                },

                onDestinationSelected = {
                        destinationPath ->

                    val duplicateExists =
                        VirtualFileSystem
                            .entryExistsInDirectory(
                                destinationPath =
                                    destinationPath,

                                entryName =
                                    sourceName
                            )

                    if (duplicateExists) {

                        atlasError =
                            AtlasErrors
                                .duplicateCopy(
                                    fileName =
                                        sourceName,

                                    destinationPath =
                                        destinationPath
                                )

                    } else {

                        val copied =
                            VirtualFileSystem
                                .copyFileToDirectory(
                                    sourceName =
                                        sourceName,

                                    destinationPath =
                                        destinationPath
                                )

                        if (copied) {

                            statusMessage =
                                "Copied $sourceName to $destinationPath."

                        } else {

                            atlasError =
                                AtlasErrors
                                    .filesystemOperationFailed(
                                        "file copy"
                                    )
                        }
                    }

                    copyFileName =
                        null
                }
            )
        }

    /*
     * ------------------------------------------------
     * MOVE FILE
     * ------------------------------------------------
     */
    moveFileName
        ?.let { sourceName ->

            val availableDestinations =
                directoryPaths
                    .filter {
                        it != currentPath
                    }

            DestinationPickerDialog(
                title =
                    "Move File",

                itemName =
                    sourceName,

                description =
                    "Choose a destination folder.",

                destinations =
                    availableDestinations,

                onDismiss = {

                    moveFileName =
                        null
                },

                onDestinationSelected = {
                        destinationPath ->

                    val duplicateExists =
                        VirtualFileSystem
                            .entryExistsInDirectory(
                                destinationPath =
                                    destinationPath,

                                entryName =
                                    sourceName
                            )

                    if (duplicateExists) {

                        atlasError =
                            AtlasErrors
                                .duplicateFile(
                                    fileName =
                                        sourceName,

                                    destinationPath =
                                        destinationPath
                                )

                    } else {

                        val moved =
                            VirtualFileSystem
                                .moveFileToDirectory(
                                    sourceName =
                                        sourceName,

                                    destinationPath =
                                        destinationPath
                                )

                        if (moved) {

                            statusMessage =
                                "Moved $sourceName to $destinationPath."

                        } else {

                            atlasError =
                                AtlasErrors
                                    .filesystemOperationFailed(
                                        "file move"
                                    )
                        }
                    }

                    moveFileName =
                        null
                }
            )
        }

    /*
     * ------------------------------------------------
     * MOVE DIRECTORY
     * ------------------------------------------------
     */
    moveDirectoryName
        ?.let { sourceName ->

            val sourcePath =
                if (currentPath == "~") {
                    "~/$sourceName"
                } else {
                    "$currentPath/$sourceName"
                }

            val availableDestinations =
                directoryPaths
                    .filter { destination ->

                        destination != currentPath &&
                                destination != sourcePath &&
                                !destination.startsWith(
                                    "$sourcePath/"
                                )
                    }

            DestinationPickerDialog(
                title =
                    "Move Folder",

                itemName =
                    sourceName,

                description =
                    "Choose where to move this folder.",

                destinations =
                    availableDestinations,

                onDismiss = {

                    moveDirectoryName =
                        null
                },

                onDestinationSelected = {
                        destinationPath ->

                    val duplicateExists =
                        VirtualFileSystem
                            .entryExistsInDirectory(
                                destinationPath =
                                    destinationPath,

                                entryName =
                                    sourceName
                            )

                    if (duplicateExists) {

                        atlasError =
                            AtlasError(
                                code =
                                    "ATLAS-FS-409-DUPLICATE",

                                title =
                                    "Folder Move Blocked",

                                whatHappened =
                                    "Atlas could not move \"$sourceName\" to $destinationPath.",

                                whyItHappened =
                                    "An item named \"$sourceName\" already exists in that folder.",

                                dataImpact =
                                    "No files or folders were moved, deleted, or overwritten.",

                                nextSteps =
                                    listOf(
                                        "Rename one of the items.",
                                        "Choose another destination folder."
                                    )
                            )

                    } else {

                        val moved =
                            VirtualFileSystem
                                .moveDirectoryToDirectory(
                                    sourceName =
                                        sourceName,

                                    destinationPath =
                                        destinationPath
                                )

                        if (moved) {

                            statusMessage =
                                "Moved $sourceName to $destinationPath."

                        } else {

                            atlasError =
                                AtlasErrors
                                    .filesystemOperationFailed(
                                        "folder move"
                                    )
                        }
                    }

                    moveDirectoryName =
                        null
                }
            )
        }

    /*
     * ------------------------------------------------
     * DETAILS
     * ------------------------------------------------
     */
    detailsEntry
        ?.let { entry ->

            FileDetailsDialog(
                entry =
                    entry,

                currentPath =
                    currentPath,

                onDismiss = {

                    detailsEntry =
                        null
                }
            )
        }

    /*
     * ------------------------------------------------
     * DELETE
     * ------------------------------------------------
     */
    deleteEntry
        ?.let { entry ->

            DeleteEntryDialog(
                entry =
                    entry,

                onDismiss = {

                    deleteEntry =
                        null
                },

                onDelete = {

                    if (
                        entry.isDirectory &&
                        entry.children.isNotEmpty()
                    ) {

                        atlasError =
                            AtlasErrors
                                .directoryNotEmpty(
                                    entry.name
                                )

                        deleteEntry =
                            null

                        return@DeleteEntryDialog
                    }

                    val deleted =
                        if (entry.isDirectory) {

                            VirtualFileSystem
                                .deleteDirectory(
                                    entry.name
                                )

                        } else {

                            VirtualFileSystem
                                .deleteFile(
                                    entry.name
                                )
                        }

                    if (deleted) {

                        statusMessage =
                            "Deleted ${entry.name}."

                    } else {

                        atlasError =
                            AtlasErrors
                                .filesystemOperationFailed(
                                    "delete"
                                )
                    }

                    deleteEntry =
                        null
                }
            )
        }

    /*
     * ------------------------------------------------
     * ERROR
     * ------------------------------------------------
     */
    atlasError
        ?.let { error ->

            AtlasErrorDialog(
                error =
                    error,

                onDismiss = {

                    atlasError =
                        null
                }
            )
        }
}

@Composable
private fun FileEntryCard(
    entry: FileNode,
    onOpen: () -> Unit,
    onEdit: () -> Unit,
    onRename: () -> Unit,
    onCopy: () -> Unit,
    onMove: () -> Unit,
    onDetails: () -> Unit,
    onDelete: () -> Unit
) {

    var menuExpanded by
    remember {
        mutableStateOf(false)
    }

    Card(
        modifier =
            Modifier.fillMaxWidth()
    ) {

        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .clickable(
                        onClick =
                            onOpen
                    )
                    .padding(
                        start = 16.dp,
                        top = 8.dp,
                        bottom = 8.dp,
                        end = 4.dp
                    ),

            verticalAlignment =
                Alignment.CenterVertically
        ) {

            Text(
                text =
                    if (entry.isDirectory) {
                        "📁"
                    } else {
                        "📄"
                    },

                style =
                    MaterialTheme
                        .typography
                        .headlineSmall
            )

            Spacer(
                modifier =
                    Modifier.width(
                        14.dp
                    )
            )

            Column(
                modifier =
                    Modifier.weight(
                        1f
                    )
            ) {

                Text(
                    text =
                        entry.name,

                    style =
                        MaterialTheme
                            .typography
                            .titleMedium,

                    maxLines =
                        1,

                    overflow =
                        TextOverflow.Ellipsis
                )

                Text(
                    text =
                        if (entry.isDirectory) {
                            "Folder"
                        } else {
                            "Atlas file"
                        },

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

            Box {

                IconButton(
                    onClick = {

                        menuExpanded =
                            true
                    }
                ) {

                    Text(
                        text =
                            "⋮",

                        style =
                            MaterialTheme
                                .typography
                                .headlineSmall
                    )
                }

                DropdownMenu(
                    expanded =
                        menuExpanded,

                    onDismissRequest = {

                        menuExpanded =
                            false
                    }
                ) {

                    DropdownMenuItem(
                        text = {
                            Text("Open")
                        },

                        onClick = {

                            menuExpanded =
                                false

                            onOpen()
                        }
                    )

                    if (!entry.isDirectory) {

                        DropdownMenuItem(
                            text = {
                                Text("Edit")
                            },

                            onClick = {

                                menuExpanded =
                                    false

                                onEdit()
                            }
                        )
                    }

                    DropdownMenuItem(
                        text = {
                            Text("Rename")
                        },

                        onClick = {

                            menuExpanded =
                                false

                            onRename()
                        }
                    )

                    if (!entry.isDirectory) {

                        DropdownMenuItem(
                            text = {
                                Text("Copy")
                            },

                            onClick = {

                                menuExpanded =
                                    false

                                onCopy()
                            }
                        )
                    }

                    DropdownMenuItem(
                        text = {
                            Text("Move")
                        },

                        onClick = {

                            menuExpanded =
                                false

                            onMove()
                        }
                    )

                    DropdownMenuItem(
                        text = {
                            Text("Details")
                        },

                        onClick = {

                            menuExpanded =
                                false

                            onDetails()
                        }
                    )

                    HorizontalDivider()

                    DropdownMenuItem(
                        text = {

                            Text(
                                text =
                                    "Delete",

                                color =
                                    MaterialTheme
                                        .colorScheme
                                        .error
                            )
                        },

                        onClick = {

                            menuExpanded =
                                false

                            onDelete()
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun DestinationPickerDialog(
    title: String,
    itemName: String,
    description: String,
    destinations: List<String>,
    onDismiss: () -> Unit,
    onDestinationSelected: (String) -> Unit
) {

    AlertDialog(
        onDismissRequest =
            onDismiss,

        title = {

            Text(
                title
            )
        },

        text = {

            Column(
                modifier =
                    Modifier.fillMaxWidth()
            ) {

                Text(
                    text =
                        itemName,

                    style =
                        MaterialTheme
                            .typography
                            .titleMedium
                )

                Spacer(
                    modifier =
                        Modifier.height(
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

                Spacer(
                    modifier =
                        Modifier.height(
                            12.dp
                        )
                )

                if (destinations.isEmpty()) {

                    Text(
                        text =
                            "No valid destination folders are available.",

                        color =
                            MaterialTheme
                                .colorScheme
                                .onSurfaceVariant
                    )

                } else {

                    LazyColumn(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .heightIn(
                                    max = 340.dp
                                ),

                        verticalArrangement =
                            Arrangement.spacedBy(
                                4.dp
                            )
                    ) {

                        items(
                            items =
                                destinations,

                            key = {
                                it
                            }
                        ) { path ->

                            DestinationFolderRow(
                                path =
                                    path,

                                onClick = {

                                    onDestinationSelected(
                                        path
                                    )
                                }
                            )
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
                    "Cancel"
                )
            }
        }
    )
}

@Composable
private fun DestinationFolderRow(
    path: String,
    onClick: () -> Unit
) {

    val depth =
        if (path == "~") {
            0
        } else {
            path
                .removePrefix("~/")
                .split("/")
                .size
        }

    Surface(
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable(
                    onClick =
                        onClick
                ),

        shape =
            MaterialTheme
                .shapes
                .small,

        tonalElevation =
            1.dp
    ) {

        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = 12.dp,
                        vertical = 12.dp
                    ),

            verticalAlignment =
                Alignment.CenterVertically
        ) {

            Spacer(
                modifier =
                    Modifier.width(
                        (depth * 8).dp
                    )
            )

            Text(
                "📁"
            )

            Spacer(
                modifier =
                    Modifier.width(
                        10.dp
                    )
            )

            Text(
                modifier =
                    Modifier.weight(
                        1f
                    ),

                text =
                    path,

                style =
                    MaterialTheme
                        .typography
                        .bodyMedium,

                fontFamily =
                    FontFamily.Monospace,

                maxLines =
                    1,

                overflow =
                    TextOverflow.Ellipsis
            )

            Text(
                text =
                    "›",

                color =
                    MaterialTheme
                        .colorScheme
                        .onSurfaceVariant
            )
        }
    }
}

@Composable
private fun AtlasFileViewer(
    fileName: String,
    fileContent: String,
    currentPath: String,
    editing: Boolean,
    onContentChanged: (String) -> Unit,
    onEdit: () -> Unit,
    onCancelEdit: () -> Unit,
    onSave: () -> Unit,
    onBack: () -> Unit
) {

    val scrollState =
        rememberScrollState()

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(
                    horizontal = 16.dp,
                    vertical = 12.dp
                )
    ) {

        Text(
            text =
                "Files",

            style =
                MaterialTheme
                    .typography
                    .headlineLarge
        )

        Spacer(
            modifier =
                Modifier.height(
                    4.dp
                )
        )

        Text(
            text =
                if (editing) {
                    "Editing Atlas File"
                } else {
                    "Atlas Files"
                },

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
                Modifier.height(
                    16.dp
                )
        )

        if (editing) {

            Row(
                modifier =
                    Modifier.fillMaxWidth(),

                horizontalArrangement =
                    Arrangement.spacedBy(
                        8.dp
                    )
            ) {

                Button(
                    modifier =
                        Modifier.weight(
                            1f
                        ),

                    onClick =
                        onCancelEdit
                ) {

                    Text(
                        "Cancel"
                    )
                }

                Button(
                    modifier =
                        Modifier.weight(
                            1f
                        ),

                    onClick =
                        onSave
                ) {

                    Text(
                        "Save"
                    )
                }
            }

        } else {

            Row(
                modifier =
                    Modifier.fillMaxWidth(),

                horizontalArrangement =
                    Arrangement.spacedBy(
                        8.dp
                    )
            ) {

                Button(
                    modifier =
                        Modifier.weight(
                            1f
                        ),

                    onClick =
                        onBack
                ) {

                    Text(
                        "← Back"
                    )
                }

                Button(
                    modifier =
                        Modifier.weight(
                            1f
                        ),

                    onClick =
                        onEdit
                ) {

                    Text(
                        "Edit"
                    )
                }
            }
        }

        Spacer(
            modifier =
                Modifier.height(
                    16.dp
                )
        )

        Text(
            text =
                fileName,

            style =
                MaterialTheme
                    .typography
                    .headlineSmall
        )

        Spacer(
            modifier =
                Modifier.height(
                    4.dp
                )
        )

        Text(
            text =
                if (currentPath == "~") {
                    "~/$fileName"
                } else {
                    "$currentPath/$fileName"
                },

            style =
                MaterialTheme
                    .typography
                    .bodySmall,

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
                    16.dp
                )
        )

        if (editing) {

            OutlinedTextField(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .weight(
                            1f
                        ),

                value =
                    fileContent,

                onValueChange =
                    onContentChanged,

                textStyle =
                    MaterialTheme
                        .typography
                        .bodyMedium
                        .copy(
                            fontFamily =
                                FontFamily.Monospace
                        ),

                label = {

                    Text(
                        "File contents"
                    )
                }
            )

        } else {

            Surface(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .weight(
                            1f
                        ),

                shape =
                    MaterialTheme
                        .shapes
                        .medium,

                tonalElevation =
                    2.dp
            ) {

                Text(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .verticalScroll(
                                scrollState
                            )
                            .padding(
                                16.dp
                            ),

                    text =
                        if (fileContent.isEmpty()) {
                            "<empty file>"
                        } else {
                            fileContent
                        },

                    style =
                        MaterialTheme
                            .typography
                            .bodyMedium,

                    fontFamily =
                        FontFamily.Monospace
                )
            }
        }
    }
}

@Composable
private fun CreateItemDialog(
    itemType: CreateItemType,
    onDismiss: () -> Unit,
    onCreate: (String) -> Unit
) {

    var name by
    remember {
        mutableStateOf("")
    }

    val cleanName =
        name.trim()

    AlertDialog(
        onDismissRequest =
            onDismiss,

        title = {

            Text(
                if (
                    itemType ==
                    CreateItemType.FILE
                ) {
                    "Create File"
                } else {
                    "Create Folder"
                }
            )
        },

        text = {

            OutlinedTextField(
                modifier =
                    Modifier.fillMaxWidth(),

                value =
                    name,

                onValueChange = {
                    name =
                        it
                },

                singleLine =
                    true,

                label = {

                    Text(
                        if (
                            itemType ==
                            CreateItemType.FILE
                        ) {
                            "File name"
                        } else {
                            "Folder name"
                        }
                    )
                }
            )
        },

        confirmButton = {

            TextButton(
                enabled =
                    cleanName.isNotBlank(),

                onClick = {

                    onCreate(
                        cleanName
                    )
                }
            ) {

                Text(
                    "Create"
                )
            }
        },

        dismissButton = {

            TextButton(
                onClick =
                    onDismiss
            ) {

                Text(
                    "Cancel"
                )
            }
        }
    )
}

@Composable
private fun RenameEntryDialog(
    entry: FileNode,
    onDismiss: () -> Unit,
    onRename: (String) -> Unit
) {

    var newName by
    remember(
        entry.name
    ) {

        mutableStateOf(
            entry.name
        )
    }

    AlertDialog(
        onDismissRequest =
            onDismiss,

        title = {

            Text(
                if (entry.isDirectory) {
                    "Rename Folder"
                } else {
                    "Rename File"
                }
            )
        },

        text = {

            OutlinedTextField(
                modifier =
                    Modifier.fillMaxWidth(),

                value =
                    newName,

                onValueChange = {
                    newName =
                        it
                },

                singleLine =
                    true,

                label = {

                    Text(
                        "New name"
                    )
                }
            )
        },

        confirmButton = {

            TextButton(
                enabled =
                    newName
                        .trim()
                        .isNotBlank() &&
                            newName.trim() !=
                            entry.name,

                onClick = {

                    onRename(
                        newName.trim()
                    )
                }
            ) {

                Text(
                    "Rename"
                )
            }
        },

        dismissButton = {

            TextButton(
                onClick =
                    onDismiss
            ) {

                Text(
                    "Cancel"
                )
            }
        }
    )
}

@Composable
private fun FileDetailsDialog(
    entry: FileNode,
    currentPath: String,
    onDismiss: () -> Unit
) {

    val fullPath =
        if (currentPath == "~") {
            "~/${entry.name}"
        } else {
            "$currentPath/${entry.name}"
        }

    val characterCount =
        if (entry.isDirectory) {

            null

        } else {

            VirtualFileSystem
                .readFile(
                    entry.name
                )
                ?.length
                ?: 0
        }

    AlertDialog(
        onDismissRequest =
            onDismiss,

        title = {

            Text(
                entry.name
            )
        },

        text = {

            Column {

                DetailRow(
                    label =
                        "Type",

                    value =
                        if (entry.isDirectory) {
                            "Folder"
                        } else {
                            "Atlas file"
                        }
                )

                DetailRow(
                    label =
                        "Path",

                    value =
                        fullPath
                )

                if (entry.isDirectory) {

                    DetailRow(
                        label =
                            "Items",

                        value =
                            entry
                                .children
                                .size
                                .toString()
                    )

                } else {

                    DetailRow(
                        label =
                            "Characters",

                        value =
                            characterCount
                                .toString()
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
                    "Close"
                )
            }
        }
    )
}

@Composable
private fun DetailRow(
    label: String,
    value: String
) {

    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(
                    vertical = 5.dp
                )
    ) {

        Text(
            text =
                label,

            style =
                MaterialTheme
                    .typography
                    .labelMedium,

            color =
                MaterialTheme
                    .colorScheme
                    .onSurfaceVariant
        )

        Text(
            text =
                value,

            style =
                MaterialTheme
                    .typography
                    .bodyMedium,

            fontFamily =
                if (label == "Path") {
                    FontFamily.Monospace
                } else {
                    FontFamily.Default
                }
        )
    }
}

@Composable
private fun DeleteEntryDialog(
    entry: FileNode,
    onDismiss: () -> Unit,
    onDelete: () -> Unit
) {

    AlertDialog(
        onDismissRequest =
            onDismiss,

        title = {

            Text(
                if (entry.isDirectory) {
                    "Delete Folder?"
                } else {
                    "Delete File?"
                }
            )
        },

        text = {

            Column {

                Text(
                    "Delete ${entry.name}?"
                )

                if (
                    entry.isDirectory &&
                    entry.children.isNotEmpty()
                ) {

                    Spacer(
                        modifier =
                            Modifier.height(
                                8.dp
                            )
                    )

                    Text(
                        text =
                            "This folder contains items. Atlas will not delete it while it is not empty.",

                        color =
                            MaterialTheme
                                .colorScheme
                                .error
                    )
                }
            }
        },

        confirmButton = {

            TextButton(
                onClick =
                    onDelete
            ) {

                Text(
                    text =
                        "Delete",

                    color =
                        MaterialTheme
                            .colorScheme
                            .error
                )
            }
        },

        dismissButton = {

            TextButton(
                onClick =
                    onDismiss
            ) {

                Text(
                    "Cancel"
                )
            }
        }
    )
}

private enum class CreateItemType {
    FILE,
    FOLDER
}