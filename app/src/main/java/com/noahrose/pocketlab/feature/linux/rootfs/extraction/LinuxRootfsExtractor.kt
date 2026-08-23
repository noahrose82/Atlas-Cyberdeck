package com.noahrose.pocketlab.feature.linux.rootfs.extraction

import android.system.Os
import com.noahrose.pocketlab.feature.linux.rootfs.filesystem.LinuxRootfsPathManager
import com.noahrose.pocketlab.feature.linux.rootfs.integrity.LinuxRootfsIntegrityStatus
import com.noahrose.pocketlab.feature.linux.rootfs.integrity.LinuxRootfsIntegrityValidator
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream
import java.io.File
import java.io.OutputStream
import java.nio.file.Files
import java.nio.file.Paths
import java.util.zip.GZIPInputStream

object LinuxRootfsExtractor {

    private const val MAX_ENTRIES =
        250_000

    private const val MAX_EXTRACTED_BYTES =
        2L * 1024L * 1024L * 1024L

    private data class DeferredDirectory(
        val file: File,
        val mode: Int
    )

    private data class DeferredHardLink(
        val destination: File,
        val targetName: String,
        val mode: Int
    )

    fun extract():
            LinuxRootfsExtractionResult {

        val integrity =
            LinuxRootfsIntegrityValidator
                .validate()

        if (
            integrity.status !=
            LinuxRootfsIntegrityStatus.VERIFIED
        ) {

            return LinuxRootfsExtractionResult
                .Failure(
                    message =
                        "Rootfs archive must pass SHA-256 verification before extraction."
                )
        }

        val paths =
            LinuxRootfsPathManager
                .getPaths()

        if (paths == null) {

            return LinuxRootfsExtractionResult
                .Failure(
                    message =
                        "Rootfs paths are unavailable."
                )
        }

        val archive =
            paths.archiveFile

        val destination =
            paths.rootfsDirectory

        val parentDirectory =
            destination.parentFile

        if (parentDirectory == null) {

            return LinuxRootfsExtractionResult
                .Failure(
                    message =
                        "Rootfs parent directory is unavailable."
                )
        }

        val pendingDirectory =
            File(
                parentDirectory,
                "${destination.name}.pending"
            )

        val backupDirectory =
            File(
                parentDirectory,
                "${destination.name}.backup"
            )

        pendingDirectory
            .deleteRecursively()

        if (
            !pendingDirectory.mkdirs()
        ) {

            return LinuxRootfsExtractionResult
                .Failure(
                    message =
                        "Unable to create temporary rootfs directory."
                )
        }

        return try {

            val extractionResult =
                extractArchive(
                    archive =
                        archive,

                    destination =
                        pendingDirectory
                )

            if (
                extractionResult is
                        LinuxRootfsExtractionResult.Failure
            ) {

                pendingDirectory
                    .deleteRecursively()

                return extractionResult
            }

            /*
             * Ubuntu may use usrmerge:
             *
             * /bin -> usr/bin
             * /lib -> usr/lib
             *
             * File.isFile follows symbolic links,
             * so this validates the actual shell
             * target as well.
             */
            val shell =
                File(
                    pendingDirectory,
                    "bin/sh"
                )

            if (
                !shell.exists() ||
                !shell.isFile
            ) {

                pendingDirectory
                    .deleteRecursively()

                return LinuxRootfsExtractionResult
                    .Failure(
                        message =
                            "Extracted rootfs does not contain a valid /bin/sh."
                    )
            }

            backupDirectory
                .deleteRecursively()

            var existingMoved =
                false

            if (destination.exists()) {

                existingMoved =
                    destination.renameTo(
                        backupDirectory
                    )

                if (!existingMoved) {

                    pendingDirectory
                        .deleteRecursively()

                    return LinuxRootfsExtractionResult
                        .Failure(
                            message =
                                "Unable to preserve the existing rootfs."
                        )
                }
            }

            if (
                !pendingDirectory.renameTo(
                    destination
                )
            ) {

                if (existingMoved) {

                    backupDirectory
                        .renameTo(
                            destination
                        )
                }

                pendingDirectory
                    .deleteRecursively()

                return LinuxRootfsExtractionResult
                    .Failure(
                        message =
                            "Unable to activate the extracted rootfs."
                    )
            }

            backupDirectory
                .deleteRecursively()

            extractionResult

        } catch (exception: Exception) {

            pendingDirectory
                .deleteRecursively()

            LinuxRootfsExtractionResult
                .Failure(
                    message =
                        exception.message
                            ?: "Unable to extract the Ubuntu rootfs.",

                    cause =
                        exception
                )
        }
    }

    private fun extractArchive(
        archive: File,
        destination: File
    ): LinuxRootfsExtractionResult {

        var entriesExtracted =
            0

        var bytesExtracted =
            0L

        val deferredDirectories =
            mutableListOf<DeferredDirectory>()

        val deferredHardLinks =
            mutableListOf<DeferredHardLink>()

        archive
            .inputStream()
            .buffered()
            .use { fileInput ->

                GZIPInputStream(
                    fileInput
                )
                    .buffered()
                    .use { gzipInput ->

                        TarArchiveInputStream(
                            gzipInput
                        )
                            .use { tarInput ->

                                while (true) {

                                    val entry =
                                        tarInput
                                            .nextEntry
                                            ?: break

                                    entriesExtracted++

                                    if (
                                        entriesExtracted >
                                        MAX_ENTRIES
                                    ) {

                                        return LinuxRootfsExtractionResult
                                            .Failure(
                                                message =
                                                    "Rootfs archive contains too many entries."
                                            )
                                    }

                                    val outputFile =
                                        resolveSafeDestination(
                                            root =
                                                destination,

                                            entryName =
                                                entry.name
                                        )
                                            ?: return LinuxRootfsExtractionResult
                                                .Failure(
                                                    message =
                                                        "Unsafe rootfs archive path: ${entry.name}"
                                                )

                                    when {

                                        entry.isDirectory -> {

                                            if (
                                                !outputFile.exists() &&
                                                !outputFile.mkdirs()
                                            ) {

                                                return LinuxRootfsExtractionResult
                                                    .Failure(
                                                        message =
                                                            "Unable to create rootfs directory: ${entry.name}"
                                                    )
                                            }

                                            deferredDirectories.add(
                                                DeferredDirectory(
                                                    file =
                                                        outputFile,

                                                    mode =
                                                        entry.mode
                                                )
                                            )
                                        }

                                        entry.isSymbolicLink -> {

                                            val parent =
                                                outputFile
                                                    .parentFile

                                            if (
                                                parent != null &&
                                                !parent.exists() &&
                                                !parent.mkdirs()
                                            ) {

                                                return LinuxRootfsExtractionResult
                                                    .Failure(
                                                        message =
                                                            "Unable to create parent directory for symbolic link: ${entry.name}"
                                                    )
                                            }

                                            if (
                                                outputFile.exists() ||
                                                Files.isSymbolicLink(
                                                    outputFile.toPath()
                                                )
                                            ) {

                                                Files.delete(
                                                    outputFile.toPath()
                                                )
                                            }

                                            Files.createSymbolicLink(
                                                outputFile.toPath(),

                                                Paths.get(
                                                    entry.linkName
                                                )
                                            )
                                        }

                                        entry.isLink -> {

                                            /*
                                             * Android may reject Java hard-link
                                             * creation inside an application
                                             * sandbox.
                                             *
                                             * Defer these entries until the
                                             * archive has been unpacked, then
                                             * safely materialize the hard link
                                             * as an independent file copy.
                                             */
                                            deferredHardLinks.add(
                                                DeferredHardLink(
                                                    destination =
                                                        outputFile,

                                                    targetName =
                                                        entry.linkName,

                                                    mode =
                                                        entry.mode
                                                )
                                            )
                                        }

                                        entry.isFile -> {

                                            val parent =
                                                outputFile
                                                    .parentFile

                                            if (
                                                parent != null &&
                                                !parent.exists() &&
                                                !parent.mkdirs()
                                            ) {

                                                return LinuxRootfsExtractionResult
                                                    .Failure(
                                                        message =
                                                            "Unable to create parent directory: ${entry.name}"
                                                    )
                                            }

                                            if (
                                                entry.size < 0L ||
                                                entry.size >
                                                MAX_EXTRACTED_BYTES
                                            ) {

                                                return LinuxRootfsExtractionResult
                                                    .Failure(
                                                        message =
                                                            "Invalid rootfs entry size: ${entry.name}"
                                                    )
                                            }

                                            outputFile
                                                .outputStream()
                                                .buffered()
                                                .use { output ->

                                                    val copied =
                                                        copyEntry(
                                                            tarInput =
                                                                tarInput,

                                                            output =
                                                                output,

                                                            currentTotal =
                                                                bytesExtracted
                                                        )

                                                    if (copied < 0L) {

                                                        return LinuxRootfsExtractionResult
                                                            .Failure(
                                                                message =
                                                                    "Rootfs extraction exceeded the allowed size."
                                                            )
                                                    }

                                                    bytesExtracted +=
                                                        copied
                                                }

                                            applyMode(
                                                file =
                                                    outputFile,

                                                mode =
                                                    entry.mode
                                            )
                                        }

                                        else -> {

                                            /*
                                             * Device nodes, sockets and other
                                             * privileged filesystem objects
                                             * cannot be safely created by an
                                             * unrooted Android application.
                                             *
                                             * PRoot supplies virtualized
                                             * runtime bindings later.
                                             */
                                        }
                                    }
                                }
                            }
                    }
            }

        /*
         * Ubuntu archives contain hard links.
         *
         * Android's application sandbox can deny
         * Files.createLink(), even when both paths
         * belong to the same application.
         *
         * Materializing the target contents gives
         * Ubuntu the same file data without relying
         * on hard-link inode semantics.
         */
        for (
        hardLink in
        deferredHardLinks
        ) {

            val sourceFile =
                resolveSafeDestination(
                    root =
                        destination,

                    entryName =
                        hardLink.targetName
                )
                    ?: return LinuxRootfsExtractionResult
                        .Failure(
                            message =
                                "Unsafe rootfs hard-link target: ${hardLink.targetName}"
                        )

            if (
                !sourceFile.exists() ||
                !sourceFile.isFile
            ) {

                return LinuxRootfsExtractionResult
                    .Failure(
                        message =
                            "Rootfs hard-link target does not exist: ${hardLink.targetName}"
                    )
            }

            val parent =
                hardLink
                    .destination
                    .parentFile

            if (
                parent != null &&
                !parent.exists() &&
                !parent.mkdirs()
            ) {

                return LinuxRootfsExtractionResult
                    .Failure(
                        message =
                            "Unable to create hard-link destination directory: ${hardLink.destination.name}"
                    )
            }

            if (
                hardLink.destination.exists() ||
                Files.isSymbolicLink(
                    hardLink.destination.toPath()
                )
            ) {

                Files.delete(
                    hardLink.destination.toPath()
                )
            }

            val copied =
                copyHardLinkTarget(
                    source =
                        sourceFile,

                    destination =
                        hardLink.destination,

                    currentTotal =
                        bytesExtracted
                )

            if (copied < 0L) {

                return LinuxRootfsExtractionResult
                    .Failure(
                        message =
                            "Rootfs extraction exceeded the allowed size while materializing hard links."
                    )
            }

            bytesExtracted +=
                copied

            applyMode(
                file =
                    hardLink.destination,

                mode =
                    hardLink.mode
            )
        }

        /*
         * Apply directory permissions only after
         * extraction so restrictive directory modes
         * cannot prevent later entries from being
         * created.
         */
        deferredDirectories
            .asReversed()
            .forEach { directory ->

                applyMode(
                    file =
                        directory.file,

                    mode =
                        directory.mode
                )
            }

        return LinuxRootfsExtractionResult
            .Success(
                entriesExtracted =
                    entriesExtracted,

                bytesExtracted =
                    bytesExtracted
            )
    }

    private fun resolveSafeDestination(
        root: File,
        entryName: String
    ): File? {

        if (entryName.isBlank()) {
            return null
        }

        /*
         * TAR entry names should be relative to the
         * archive root. Reject explicit absolute
         * archive paths.
         */
        if (
            entryName.startsWith("/") ||
            entryName.startsWith("\\")
        ) {
            return null
        }

        val canonicalRoot =
            root.canonicalFile

        val candidate =
            File(
                canonicalRoot,
                entryName
            )
                .canonicalFile

        val rootPath =
            canonicalRoot.path

        val candidatePath =
            candidate.path

        if (
            candidatePath ==
            rootPath
        ) {
            return candidate
        }

        val safePrefix =
            rootPath +
                    File.separator

        if (
            !candidatePath.startsWith(
                safePrefix
            )
        ) {
            return null
        }

        return candidate
    }

    private fun copyEntry(
        tarInput: TarArchiveInputStream,
        output: OutputStream,
        currentTotal: Long
    ): Long {

        val buffer =
            ByteArray(
                DEFAULT_BUFFER_SIZE
            )

        var copied =
            0L

        while (true) {

            val count =
                tarInput.read(
                    buffer
                )

            if (count <= 0) {
                break
            }

            copied +=
                count

            if (
                currentTotal +
                copied >
                MAX_EXTRACTED_BYTES
            ) {
                return -1L
            }

            output.write(
                buffer,
                0,
                count
            )
        }

        return copied
    }

    private fun copyHardLinkTarget(
        source: File,
        destination: File,
        currentTotal: Long
    ): Long {

        val sourceSize =
            source.length()

        if (
            sourceSize < 0L ||
            currentTotal +
            sourceSize >
            MAX_EXTRACTED_BYTES
        ) {
            return -1L
        }

        var copied =
            0L

        source
            .inputStream()
            .buffered()
            .use { input ->

                destination
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

                            copied +=
                                count

                            if (
                                currentTotal +
                                copied >
                                MAX_EXTRACTED_BYTES
                            ) {
                                return -1L
                            }

                            output.write(
                                buffer,
                                0,
                                count
                            )
                        }
                    }
            }

        return copied
    }

    private fun applyMode(
        file: File,
        mode: Int
    ) {

        runCatching {

            Os.chmod(
                file.absolutePath,
                mode and 0x0FFF
            )
        }
    }
}