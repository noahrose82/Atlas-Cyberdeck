package com.noahrose.pocketlab.feature.linux.runtime.metrics

import com.noahrose.pocketlab.feature.linux.runtime.filesystem.LinuxRuntimePathManager
import java.io.File
import java.nio.file.Files
import java.nio.file.LinkOption

data class LinuxInstallationMetrics(
    val packageCount: Int,
    val storageUsedMb: Long
)

object LinuxInstallationMetricsReader {

    private const val BYTES_PER_MIB =
        1024L * 1024L

    fun read():
            LinuxInstallationMetrics? {

        val paths =
            LinuxRuntimePathManager
                .getPaths()
                ?: return null

        return read(
            paths.rootfsDirectory
        )
    }

    /*
     * Kept independent from Android and PRoot so the
     * metric rules can be covered by local JVM tests.
     */
    internal fun read(
        rootfsDirectory: File
    ): LinuxInstallationMetrics? {

        if (
            !rootfsDirectory.exists() ||
            !rootfsDirectory.isDirectory
        ) {
            return null
        }

        val packageCount =
            countInstalledPackages(
                rootfsDirectory
            )
                ?: return null

        val storageBytes =
            calculateStorageBytes(
                rootfsDirectory
            )
                ?: return null

        return LinuxInstallationMetrics(
            packageCount =
                packageCount,

            storageUsedMb =
                storageBytes /
                    BYTES_PER_MIB
        )
    }

    private fun countInstalledPackages(
        rootfsDirectory: File
    ): Int? {

        val statusFile =
            File(
                rootfsDirectory,
                "var/lib/dpkg/status"
            )

        if (
            !statusFile.exists() ||
            !statusFile.isFile
        ) {
            return null
        }

        return runCatching {

            statusFile
                .useLines { lines ->

                    lines.count { line ->

                        line.trim() ==
                            "Status: install ok installed"
                    }
                }

        }.getOrNull()
    }

    private fun calculateStorageBytes(
        rootfsDirectory: File
    ): Long? {

        return runCatching {

            var totalBytes =
                0L

            val stream =
                Files.walk(
                    rootfsDirectory.toPath()
                )

            try {

                stream.forEach { path ->

                    if (
                        Files.isRegularFile(
                            path,
                            LinkOption.NOFOLLOW_LINKS
                        )
                    ) {

                        totalBytes +=
                            runCatching {

                                Files.size(
                                    path
                                )

                            }.getOrDefault(
                                0L
                            )
                    }
                }

            } finally {

                stream.close()
            }

            totalBytes

        }.getOrNull()
    }
}
