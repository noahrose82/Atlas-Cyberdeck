package com.noahrose.pocketlab.feature.linux.rootfs.provision

import com.noahrose.pocketlab.feature.linux.rootfs.filesystem.LinuxRootfsPathManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest

object LinuxRootfsDownloader {

    suspend fun download():
            LinuxRootfsDownloadResult =
        withContext(
            Dispatchers.IO
        ) {

            val descriptor =
                LinuxRootfsSelector
                    .getPreferredRootfs()

            if (descriptor == null) {

                return@withContext LinuxRootfsDownloadResult
                    .Failure(
                        message =
                            "No trusted rootfs descriptor is available for this device."
                    )
            }

            val paths =
                LinuxRootfsPathManager
                    .getPaths()

            if (paths == null) {

                return@withContext LinuxRootfsDownloadResult
                    .Failure(
                        message =
                            "Rootfs staging paths are unavailable."
                    )
            }

            val stagingDirectory =
                paths.stagingDirectory

            if (
                !stagingDirectory.exists() &&
                !stagingDirectory.mkdirs()
            ) {

                return@withContext LinuxRootfsDownloadResult
                    .Failure(
                        message =
                            "Unable to create rootfs staging directory."
                    )
            }

            val destination =
                paths.archiveFile

            val temporaryFile =
                File(
                    stagingDirectory,
                    "${descriptor.archiveName}.part"
                )

            if (temporaryFile.exists()) {
                temporaryFile.delete()
            }

            var connection:
                    HttpURLConnection? =
                null

            try {

                connection =
                    URL(
                        descriptor.downloadUrl
                    )
                        .openConnection() as
                            HttpURLConnection

                connection.requestMethod =
                    "GET"

                connection.connectTimeout =
                    15_000

                connection.readTimeout =
                    30_000

                connection.instanceFollowRedirects =
                    true

                connection.setRequestProperty(
                    "User-Agent",
                    "Atlas-Cyberdeck"
                )

                connection.connect()

                val responseCode =
                    connection.responseCode

                if (
                    responseCode !in
                    200..299
                ) {

                    temporaryFile.delete()

                    return@withContext LinuxRootfsDownloadResult
                        .Failure(
                            message =
                                "Rootfs download failed with HTTP $responseCode."
                        )
                }

                var bytesDownloaded =
                    0L

                connection
                    .inputStream
                    .buffered()
                    .use { input ->

                        temporaryFile
                            .outputStream()
                            .buffered()
                            .use { output ->

                                val buffer =
                                    ByteArray(
                                        DEFAULT_BUFFER_SIZE
                                    )

                                while (true) {

                                    val count =
                                        input.read(
                                            buffer
                                        )

                                    if (count <= 0) {
                                        break
                                    }

                                    output.write(
                                        buffer,
                                        0,
                                        count
                                    )

                                    bytesDownloaded +=
                                        count
                                }
                            }
                    }

                if (
                    !temporaryFile.exists() ||
                    temporaryFile.length() == 0L
                ) {

                    temporaryFile.delete()

                    return@withContext LinuxRootfsDownloadResult
                        .Failure(
                            message =
                                "Downloaded rootfs archive is empty."
                        )
                }

                val actualSha256 =
                    calculateSha256(
                        temporaryFile
                    )

                val expectedSha256 =
                    descriptor
                        .sha256
                        .trim()
                        .lowercase()

                if (
                    !actualSha256.equals(
                        expectedSha256,
                        ignoreCase = true
                    )
                ) {

                    temporaryFile.delete()

                    return@withContext LinuxRootfsDownloadResult
                        .Failure(
                            message =
                                "Downloaded rootfs SHA-256 does not match the trusted Canonical checksum."
                        )
                }

                if (destination.exists()) {

                    if (!destination.delete()) {

                        temporaryFile.delete()

                        return@withContext LinuxRootfsDownloadResult
                            .Failure(
                                message =
                                    "Unable to replace the existing rootfs archive."
                            )
                    }
                }

                if (
                    !temporaryFile.renameTo(
                        destination
                    )
                ) {

                    temporaryFile.delete()

                    return@withContext LinuxRootfsDownloadResult
                        .Failure(
                            message =
                                "Unable to finalize the verified rootfs archive."
                        )
                }

                LinuxRootfsDownloadResult
                    .Success(
                        bytesDownloaded =
                            bytesDownloaded
                    )

            } catch (
                exception: Exception
            ) {

                runCatching {
                    temporaryFile.delete()
                }

                LinuxRootfsDownloadResult
                    .Failure(
                        message =
                            exception.message
                                ?: "Unable to download the Ubuntu rootfs archive.",

                        cause =
                            exception
                    )

            } finally {

                connection
                    ?.disconnect()
            }
        }

    private fun calculateSha256(
        file: File
    ): String {

        val digest =
            MessageDigest
                .getInstance(
                    "SHA-256"
                )

        file.inputStream()
            .buffered()
            .use { input ->

                val buffer =
                    ByteArray(
                        DEFAULT_BUFFER_SIZE
                    )

                while (true) {

                    val count =
                        input.read(
                            buffer
                        )

                    if (count <= 0) {
                        break
                    }

                    digest.update(
                        buffer,
                        0,
                        count
                    )
                }
            }

        return digest
            .digest()
            .joinToString(
                separator = ""
            ) { byte ->

                "%02x".format(
                    byte.toInt() and 0xff
                )
            }
    }
}