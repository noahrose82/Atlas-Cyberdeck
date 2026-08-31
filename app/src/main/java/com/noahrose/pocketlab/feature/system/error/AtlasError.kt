package com.noahrose.pocketlab.feature.system.error

data class AtlasError(
    val code: String,
    val title: String,
    val whatHappened: String,
    val whyItHappened: String,
    val dataImpact: String,
    val nextSteps: List<String> = emptyList()
) {

    fun toDisplayText(): String {

        return buildString {

            appendLine(
                whatHappened
            )

            appendLine()

            appendLine(
                "Why:"
            )

            appendLine(
                whyItHappened
            )

            appendLine()

            appendLine(
                dataImpact
            )

            if (
                nextSteps.isNotEmpty()
            ) {

                appendLine()

                appendLine(
                    "Try:"
                )

                nextSteps.forEach { step ->

                    appendLine(
                        "• $step"
                    )
                }
            }

            appendLine()

            append(
                "Error code: $code"
            )
        }
    }
}

object AtlasErrors {

    fun duplicateFile(
        fileName: String,
        destinationPath: String
    ): AtlasError {

        return AtlasError(
            code =
                "ATLAS-FS-409-DUPLICATE",

            title =
                "Move Blocked",

            whatHappened =
                "Atlas could not move \"$fileName\" to $destinationPath.",

            whyItHappened =
                "An item named \"$fileName\" already exists in that folder.",

            dataImpact =
                "Nothing was moved, deleted, or overwritten.",

            nextSteps =
                listOf(
                    "Rename the existing item.",
                    "Rename the item you are moving.",
                    "Choose another folder."
                )
        )
    }

    fun duplicateCopy(
        fileName: String,
        destinationPath: String
    ): AtlasError {

        return AtlasError(
            code =
                "ATLAS-FS-409-DUPLICATE",

            title =
                "Copy Blocked",

            whatHappened =
                "Atlas could not copy \"$fileName\" to $destinationPath.",

            whyItHappened =
                "An item named \"$fileName\" already exists in that folder.",

            dataImpact =
                "Nothing was copied, deleted, or overwritten.",

            nextSteps =
                listOf(
                    "Rename the existing item.",
                    "Rename the item you are copying.",
                    "Choose another folder."
                )
        )
    }

    fun directoryNotEmpty(
        directoryName: String
    ): AtlasError {

        return AtlasError(
            code =
                "ATLAS-FS-409-NOT-EMPTY",

            title =
                "Folder Delete Blocked",

            whatHappened =
                "Atlas could not delete \"$directoryName\".",

            whyItHappened =
                "The folder still contains files or folders.",

            dataImpact =
                "Nothing was deleted.",

            nextSteps =
                listOf(
                    "Open the folder.",
                    "Move or delete its contents.",
                    "Try deleting the empty folder again."
                )
        )
    }

    fun directoryCycle(
        directoryName: String
    ): AtlasError {

        return AtlasError(
            code =
                "ATLAS-FS-409-DIRECTORY-CYCLE",

            title =
                "Folder Move Blocked",

            whatHappened =
                "Atlas could not move \"$directoryName\".",

            whyItHappened =
                "The selected destination is inside \"$directoryName\". " +
                        "A folder cannot be moved inside itself or one of its own subfolders.",

            dataImpact =
                "No files or folders were changed.",

            nextSteps =
                listOf(
                    "Choose a folder outside \"$directoryName\"."
                )
        )
    }

    fun invalidName(
        itemName: String
    ): AtlasError {

        return AtlasError(
            code =
                "ATLAS-FS-422-INVALID-NAME",

            title =
                "Name Not Accepted",

            whatHappened =
                "Atlas could not use \"$itemName\" as the item name.",

            whyItHappened =
                "The name is empty or is not valid for this operation.",

            dataImpact =
                "Nothing was created, renamed, moved, or deleted.",

            nextSteps =
                listOf(
                    "Enter a different name.",
                    "Try the operation again."
                )
        )
    }

    fun itemNotFound(
        itemName: String
    ): AtlasError {

        return AtlasError(
            code =
                "ATLAS-FS-404-NOT-FOUND",

            title =
                "Item Not Found",

            whatHappened =
                "Atlas could not find \"$itemName\".",

            whyItHappened =
                "The item may have been moved, renamed, or deleted.",

            dataImpact =
                "Atlas did not change any filesystem data.",

            nextSteps =
                listOf(
                    "Refresh the folder.",
                    "Check the current location.",
                    "Try the operation again."
                )
        )
    }

    fun filesystemOperationFailed(
        operation: String
    ): AtlasError {

        return AtlasError(
            code =
                "ATLAS-FS-500-OPERATION-FAILED",

            title =
                "Filesystem Operation Failed",

            whatHappened =
                "Atlas could not complete the $operation operation.",

            whyItHappened =
                "Atlas encountered a filesystem problem that did not match a more specific error.",

            dataImpact =
                "Atlas could not confirm that the requested operation completed.",

            nextSteps =
                listOf(
                    "Check the current folder.",
                    "Try the operation again.",
                    "Use the error code when reporting the problem if it continues."
                )
        )
    }
}