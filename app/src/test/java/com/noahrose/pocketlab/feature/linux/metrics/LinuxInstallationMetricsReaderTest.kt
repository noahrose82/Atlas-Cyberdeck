package com.noahrose.pocketlab.feature.linux.runtime.metrics

import java.io.File
import java.nio.file.Files
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LinuxInstallationMetricsReaderTest {

    @Test
    fun read_countsOnlyInstalledPackages_andMeasuresRootfsStorage() {

        val rootfs =
            Files.createTempDirectory(
                "atlas-rootfs-metrics"
            )
                .toFile()

        try {

            val dpkgDirectory =
                File(
                    rootfs,
                    "var/lib/dpkg"
                )

            assertTrue(
                dpkgDirectory.mkdirs()
            )

            File(
                dpkgDirectory,
                "status"
            )
                .writeText(
                    """
                    Package: alpha
                    Status: install ok installed

                    Package: beta
                    Status: deinstall ok config-files

                    Package: gamma
                    Status: install ok installed
                    """.trimIndent()
                )

            val payload =
                File(
                    rootfs,
                    "usr/share/atlas-test.bin"
                )

            assertTrue(
                payload.parentFile
                    ?.mkdirs() == true
            )

            payload.writeBytes(
                ByteArray(
                    2 * 1024 * 1024
                )
            )

            val metrics =
                LinuxInstallationMetricsReader
                    .read(
                        rootfs
                    )

            requireNotNull(
                metrics
            )

            assertEquals(
                2,
                metrics.packageCount
            )

            assertTrue(
                metrics.storageUsedMb >= 2L
            )

        } finally {

            rootfs.deleteRecursively()
        }
    }

    @Test
    fun read_returnsNull_whenDpkgStatusIsMissing() {

        val rootfs =
            Files.createTempDirectory(
                "atlas-rootfs-missing-status"
            )
                .toFile()

        try {

            assertNull(
                LinuxInstallationMetricsReader
                    .read(
                        rootfs
                    )
            )

        } finally {

            rootfs.deleteRecursively()
        }
    }
}
