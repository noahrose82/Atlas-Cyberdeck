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
fun AtlasFileExportButton(
    fileName: String,
    content: String,
    modifier: Modifier = Modifier,
    onExported: () -> Unit,
    onExportFailed: () -> Unit
) {

    val context =
        LocalContext.current

    val exportLauncher =
        rememberLauncherForActivityResult(
            contract =
                ActivityResultContracts.CreateDocument(
                    "text/plain"
                )
        ) { uri ->

            /*
             * User cancellation is not an error.
             */
            if (uri == null) {
                return@rememberLauncherForActivityResult
            }

            val exported =
                AtlasFileTransfer
                    .exportTextFile(
                        context = context,
                        uri = uri,
                        content = content
                    )

            if (exported) {

                onExported()

            } else {

                onExportFailed()
            }
        }

    Button(
        modifier =
            modifier,

        onClick = {

            /*
             * Android's document picker lets the
             * user choose where the exported Atlas
             * file will be saved.
             *
             * The existing Atlas file is never
             * modified by this operation.
             */
            exportLauncher.launch(
                fileName
            )
        }
    ) {

        Text(
            text =
                "Export"
        )
    }
}