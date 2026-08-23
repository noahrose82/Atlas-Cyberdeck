package com.noahrose.pocketlab.feature.linux.rootfs.provision

import com.noahrose.pocketlab.feature.linux.rootfs.extraction.LinuxRootfsExtractionManager
import com.noahrose.pocketlab.feature.linux.rootfs.extraction.LinuxRootfsExtractionResult
import com.noahrose.pocketlab.feature.linux.rootfs.filesystem.LinuxRootfsStagingManager
import com.noahrose.pocketlab.feature.linux.rootfs.filesystem.LinuxRootfsStagingResult

object LinuxRootfsProvisionManager {

    suspend fun provision(
        onProgress: (
            progress: Float,
            step: String
        ) -> Unit
    ): LinuxRootfsProvisionResult {

        onProgress(
            0.10f,
            "Preparing Ubuntu storage..."
        )

        when (
            val staging =
                LinuxRootfsStagingManager
                    .prepare()
        ) {

            LinuxRootfsStagingResult.Ready -> {
                // Continue.
            }

            is LinuxRootfsStagingResult.Failure -> {

                return LinuxRootfsProvisionResult
                    .Failure(
                        message =
                            staging.message
                    )
            }
        }

        onProgress(
            0.20f,
            "Downloading Ubuntu 24.04.4 LTS..."
        )

        when (
            val download =
                LinuxRootfsDownloadManager
                    .download()
        ) {

            is LinuxRootfsDownloadResult.Success -> {
                // Download manager already verifies SHA-256.
            }

            is LinuxRootfsDownloadResult.Failure -> {

                return LinuxRootfsProvisionResult
                    .Failure(
                        message =
                            download.message
                    )
            }
        }

        onProgress(
            0.65f,
            "Ubuntu archive verified."
        )

        onProgress(
            0.70f,
            "Extracting Ubuntu rootfs..."
        )

        when (
            val extraction =
                LinuxRootfsExtractionManager
                    .extract()
        ) {

            is LinuxRootfsExtractionResult.Success -> {
                // Extractor already validates /bin/sh.
            }

            is LinuxRootfsExtractionResult.Failure -> {

                return LinuxRootfsProvisionResult
                    .Failure(
                        message =
                            extraction.message
                    )
            }
        }

        onProgress(
            0.95f,
            "Finalizing Ubuntu environment..."
        )

        return LinuxRootfsProvisionResult
            .Success
    }
}