package com.noahrose.pocketlab.feature.linux.rootfs.integrity

import com.noahrose.pocketlab.feature.linux.rootfs.filesystem.LinuxRootfsPathManager
import com.noahrose.pocketlab.feature.linux.rootfs.provision.LinuxRootfsSelector
import java.io.File
import java.security.MessageDigest

object LinuxRootfsIntegrityValidator {

    fun validate():
            LinuxRootfsIntegrityResult {

        val descriptor =
            LinuxRootfsSelector
                .getPreferredRootfs()

        if (descriptor == null) {

            return LinuxRootfsIntegrityResult(
                status =
                    LinuxRootfsIntegrityStatus
                        .HASH_NOT_PINNED,

                message =
                    "No trusted rootfs descriptor is available."
            )
        }

        val paths =
            LinuxRootfsPathManager
                .getPaths()

        if (paths == null) {

            return LinuxRootfsIntegrityResult(
                status =
                    LinuxRootfsIntegrityStatus
                        .VALIDATION_FAILED,

                message =
                    "Rootfs paths are unavailable."
            )
        }

        val archive =
            paths.archiveFile

        if (
            !archive.exists() ||
            !archive.isFile
        ) {

            return LinuxRootfsIntegrityResult(
                status =
                    LinuxRootfsIntegrityStatus
                        .ARCHIVE_MISSING,

                expectedSha256 =
                    descriptor.sha256
            )
        }

        val expectedSha256 =
            descriptor
                .sha256
                .trim()
                .lowercase()

        if (expectedSha256.isBlank()) {

            return LinuxRootfsIntegrityResult(
                status =
                    LinuxRootfsIntegrityStatus
                        .HASH_NOT_PINNED,

                message =
                    "No trusted SHA-256 is pinned for this rootfs."
            )
        }

        return try {

            val actualSha256 =
                calculateSha256(
                    archive
                )

            if (
                actualSha256.equals(
                    expectedSha256,
                    ignoreCase = true
                )
            ) {

                LinuxRootfsIntegrityResult(
                    status =
                        LinuxRootfsIntegrityStatus
                            .VERIFIED,

                    expectedSha256 =
                        expectedSha256,

                    actualSha256 =
                        actualSha256
                )

            } else {

                LinuxRootfsIntegrityResult(
                    status =
                        LinuxRootfsIntegrityStatus
                            .HASH_MISMATCH,

                    expectedSha256 =
                        expectedSha256,

                    actualSha256 =
                        actualSha256,

                    message =
                        "Rootfs archive SHA-256 does not match the trusted Canonical checksum."
                )
            }

        } catch (exception: Exception) {

            LinuxRootfsIntegrityResult(
                status =
                    LinuxRootfsIntegrityStatus
                        .VALIDATION_FAILED,

                expectedSha256 =
                    expectedSha256,

                message =
                    exception.message
                        ?: "Unable to validate the rootfs archive."
            )
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