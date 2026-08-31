package com.noahrose.pocketlab.ui.components

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.noahrose.pocketlab.feature.filesystem.transfer.AtlasFileTransfer

@Composable
fun AtlasFileImportButton(
    modifier: Modifier = Modifier,
    onFileImported: (
        fileName: String,
        content: String
    ) -> Unit,
    onImportFailed: () -> Unit
) {

    val context =
        LocalContext.current

    val importLauncher =
        rememberLauncherForActivityResult(
            contract =
                ActivityResultContracts.OpenDocument()
        ) { uri ->

            /*
             * User cancellation is not an error.
             */
            if (uri == null) {
                return@rememberLauncherForActivityResult
            }

            val importedFile =
                AtlasFileTransfer
                    .importTextFile(
                        context = context,
                        uri = uri
                    )

            if (importedFile == null) {

                onImportFailed()

                return@rememberLauncherForActivityResult
            }

            onFileImported(
                importedFile.name,
                importedFile.content
            )
        }

    Button(
        modifier =
            modifier,

        onClick = {

            /*
             * Atlas currently imports text-based
             * documents only.
             */
            importLauncher.launch(
                arrayOf(
                    "text/*",
                    "application/json",
                    "application/xml"
                )
            )
        }
    ) {

        Text(
            text =
                "Import"
        )
    }
}