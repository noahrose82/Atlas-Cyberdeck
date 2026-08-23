package com.noahrose.pocketlab.feature.linux.rootfs.provision

import com.noahrose.pocketlab.feature.linux.rootfs.filesystem.LinuxRootfsPathManager
import com.noahrose.pocketlab.feature.linux.rootfs.integrity.LinuxRootfsIntegrityStatus
import com.noahrose.pocketlab.feature.linux.rootfs.integrity.LinuxRootfsIntegrityValidator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object LinuxRootfsDownloadManager {

    private val _state =
        MutableStateFlow<LinuxRootfsDownloadState>(
            LinuxRootfsDownloadState.Idle
        )

    val state: StateFlow<LinuxRootfsDownloadState> =
        _state.asStateFlow()

    suspend fun download():
            LinuxRootfsDownloadResult {

        /*
         * Do not download the archive again when
         * the staged copy already passes SHA-256
         * verification.
         */
        val existingIntegrity =
            LinuxRootfsIntegrityValidator
                .validate()

        if (
            existingIntegrity.status ==
            LinuxRootfsIntegrityStatus.VERIFIED
        ) {

            val archiveSize =
                LinuxRootfsPathManager
                    .getPaths()
                    ?.archiveFile
                    ?.length()
                    ?: 0L

            _state.value =
                LinuxRootfsDownloadState.Ready(
                    archiveSizeBytes =
                        archiveSize,

                    downloadedNow =
                        false
                )

            return LinuxRootfsDownloadResult
                .Success(
                    bytesDownloaded =
                        0L
                )
        }

        _state.value =
            LinuxRootfsDownloadState
                .Downloading

        return when (
            val result =
                LinuxRootfsDownloader
                    .download()
        ) {

            is LinuxRootfsDownloadResult.Success -> {

                val integrity =
                    LinuxRootfsIntegrityValidator
                        .validate()

                if (
                    integrity.status !=
                    LinuxRootfsIntegrityStatus.VERIFIED
                ) {

                    val message =
                        integrity.message
                            ?: "Downloaded rootfs archive failed integrity validation."

                    _state.value =
                        LinuxRootfsDownloadState
                            .Failed(
                                message =
                                    message
                            )

                    LinuxRootfsDownloadResult
                        .Failure(
                            message =
                                message
                        )

                } else {

                    val archiveSize =
                        LinuxRootfsPathManager
                            .getPaths()
                            ?.archiveFile
                            ?.length()
                            ?: result.bytesDownloaded

                    _state.value =
                        LinuxRootfsDownloadState
                            .Ready(
                                archiveSizeBytes =
                                    archiveSize,

                                downloadedNow =
                                    true
                            )

                    result
                }
            }

            is LinuxRootfsDownloadResult.Failure -> {

                _state.value =
                    LinuxRootfsDownloadState
                        .Failed(
                            message =
                                result.message
                        )

                result
            }
        }
    }

    fun reset() {

        _state.value =
            LinuxRootfsDownloadState
                .Idle
    }
}