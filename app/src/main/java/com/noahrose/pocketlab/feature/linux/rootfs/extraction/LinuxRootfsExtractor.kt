package com.noahrose.pocketlab.feature.linux.rootfs.extraction

import android.system.Os
import com.noahrose.pocketlab.feature.linux.rootfs.filesystem.LinuxRootfsPathManager
import com.noahrose.pocketlab.feature.linux.rootfs.integrity.LinuxRootfsIntegrityStatus
import com.noahrose.pocketlab.feature.linux.rootfs.integrity.LinuxRootfsIntegrityValidator
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream
import java.io.File
import java.io.OutputStream
import java.nio.file.Files
import java.nio.file.LinkOption
import java.nio.file.Path
import java.nio.file.Paths
import java.util.zip.GZIPInputStream

/**
 * Extracts a verified Ubuntu rootfs into app-private storage.
 *
 * Safety properties:
 * - No entry may escape the pending rootfs, including through symlink parents.
 * - Absolute guest symlinks are rewritten to safe relative links within the rootfs.
 * - Existing pending and backup directories are never silently destroyed.
 * - An existing installation is restored on activation failure when possible.
 * - A successfully replaced installation's backup is retained for recovery.
 *
 * A successful extraction is NOT proof that PRoot can boot Ubuntu. Test separately.
 */
object LinuxRootfsExtractor {

    private const val MAX_ENTRIES = 250_000
    private const val MAX_EXTRACTED_BYTES = 2L * 1024L * 1024L * 1024L

    private data class DeferredDirectory(val file: File, val mode: Int)
    private data class DeferredHardLink(
        val destination: File,
        val targetName: String,
        val mode: Int
    )

    fun extract(): LinuxRootfsExtractionResult {
        val integrity = LinuxRootfsIntegrityValidator.validate()
        if (integrity.status != LinuxRootfsIntegrityStatus.VERIFIED) {
            return failure("Rootfs archive must pass SHA-256 verification before extraction.")
        }

        val paths = LinuxRootfsPathManager.getPaths()
            ?: return failure("Rootfs paths are unavailable.")
        val archive = paths.archiveFile
        val destination = paths.rootfsDirectory
        val parent = destination.parentFile
            ?: return failure("Rootfs parent directory is unavailable.")

        if (!parent.isDirectory && !parent.mkdirs()) {
            return failure("Unable to create rootfs parent directory.")
        }

        val pending = File(parent, "${destination.name}.pending")
        val backup = File(parent, "${destination.name}.backup")

        // Do not destroy data from an earlier interrupted installation.
        if (existsWithoutFollowingLinks(pending)) {
            return failure(
                "Pending rootfs already exists. Inspect or recover it before retrying: ${pending.absolutePath}"
            )
        }
        if (existsWithoutFollowingLinks(backup)) {
            return failure(
                "Rootfs backup already exists. Inspect or recover it before retrying: ${backup.absolutePath}"
            )
        }
        if (!pending.mkdir()) {
            return failure("Unable to create temporary rootfs directory.")
        }

        var movedOldInstallation = false
        var newInstallationActivated = false
        try {
            val result = extractArchive(archive, pending)
            if (result is LinuxRootfsExtractionResult.Failure) {
                deleteTreeWithoutFollowingLinks(pending)
                return result
            }

            val shell = File(pending, "bin/sh")
            if (!shell.isFile || !isWithinRoot(pending, shell)) {
                deleteTreeWithoutFollowingLinks(pending)
                return failure("Extracted rootfs does not contain a safe, valid /bin/sh.")
            }

            if (existsWithoutFollowingLinks(destination)) {
                if (!destination.isDirectory || Files.isSymbolicLink(destination.toPath())) {
                    deleteTreeWithoutFollowingLinks(pending)
                    return failure("Existing rootfs path is not a normal directory.")
                }
                if (!destination.renameTo(backup)) {
                    deleteTreeWithoutFollowingLinks(pending)
                    return failure("Unable to preserve existing Ubuntu installation.")
                }
                movedOldInstallation = true
            }

            if (!pending.renameTo(destination)) {
                val restored = !movedOldInstallation || backup.renameTo(destination)
                if (restored) {
                    deleteTreeWithoutFollowingLinks(pending)
                }
                return failure(
                    if (restored) "Unable to activate new rootfs; previous installation restored."
                    else "CRITICAL: Activation failed and backup restoration failed. " +
                            "Do not retry or delete files; inspect ${backup.absolutePath}."
                )
            }

            newInstallationActivated = true
            // Keep the backup if an older installation was replaced. Boot verification
            // and an explicit recovery/cleanup action should happen before deletion.
            return result
        } catch (e: Exception) {
            if (!newInstallationActivated) {
                if (movedOldInstallation && !existsWithoutFollowingLinks(destination)) {
                    val restored = backup.renameTo(destination)
                    if (!restored) {
                        return LinuxRootfsExtractionResult.Failure(
                            message = "CRITICAL: Extraction failed and previous rootfs could not be restored. " +
                                    "Backup remains at ${backup.absolutePath}.",
                            cause = e
                        )
                    }
                }
                deleteTreeWithoutFollowingLinks(pending)
            }
            return LinuxRootfsExtractionResult.Failure(
                message = e.message ?: "Unable to extract the Ubuntu rootfs.",
                cause = e
            )
        }
    }

    private fun extractArchive(
        archive: File,
        destination: File
    ): LinuxRootfsExtractionResult {
        var entriesExtracted = 0
        var bytesExtracted = 0L
        val deferredDirectories = mutableListOf<DeferredDirectory>()
        val deferredHardLinks = mutableListOf<DeferredHardLink>()

        archive.inputStream().buffered().use { fileInput ->
            GZIPInputStream(fileInput).buffered().use { gzipInput ->
                TarArchiveInputStream(gzipInput).use { tarInput ->
                    while (true) {
                        val entry = tarInput.nextEntry ?: break
                        entriesExtracted++
                        if (entriesExtracted > MAX_ENTRIES) {
                            return failure("Rootfs archive contains too many entries.")
                        }

                        val outputFile = resolveSafeDestination(destination, entry.name)
                            ?: return failure("Unsafe rootfs archive path: ${entry.name}")

                        when {
                            entry.isDirectory -> {
                                if (existsWithoutFollowingLinks(outputFile) &&
                                    (!outputFile.isDirectory || Files.isSymbolicLink(outputFile.toPath()))
                                ) {
                                    return failure("Directory collides with a non-directory: ${entry.name}")
                                }
                                if (!outputFile.isDirectory && !outputFile.mkdirs()) {
                                    return failure("Unable to create rootfs directory: ${entry.name}")
                                }
                                deferredDirectories.add(DeferredDirectory(outputFile, entry.mode))
                            }

                            entry.isSymbolicLink -> {
                                val target = safeSymbolicLinkTarget(
                                    destination, outputFile, entry.linkName
                                ) ?: return failure("Unsafe symbolic link: ${entry.name} -> ${entry.linkName}")
                                if (!ensureParent(outputFile)) {
                                    return failure("Unable to create symlink parent: ${entry.name}")
                                }
                                if (existsWithoutFollowingLinks(outputFile)) {
                                    return failure("Duplicate or conflicting rootfs entry: ${entry.name}")
                                }
                                Files.createSymbolicLink(outputFile.toPath(), target)
                            }

                            entry.isLink -> {
                                if (!ensureParent(outputFile)) {
                                    return failure("Unable to create hard-link parent: ${entry.name}")
                                }
                                if (existsWithoutFollowingLinks(outputFile)) {
                                    return failure("Duplicate or conflicting rootfs entry: ${entry.name}")
                                }
                                deferredHardLinks.add(
                                    DeferredHardLink(outputFile, entry.linkName, entry.mode)
                                )
                            }

                            entry.isFile -> {
                                if (!ensureParent(outputFile)) {
                                    return failure("Unable to create file parent: ${entry.name}")
                                }
                                if (existsWithoutFollowingLinks(outputFile)) {
                                    return failure("Duplicate or conflicting rootfs entry: ${entry.name}")
                                }
                                if (entry.size < 0L || entry.size > MAX_EXTRACTED_BYTES ||
                                    entry.size > MAX_EXTRACTED_BYTES - bytesExtracted
                                ) {
                                    return failure("Invalid or excessive rootfs entry size: ${entry.name}")
                                }
                                var copied = 0L
                                outputFile.outputStream().buffered().use { output ->
                                    copied = copyEntry(tarInput, output, MAX_EXTRACTED_BYTES - bytesExtracted)
                                }
                                if (copied < 0L || copied != entry.size) {
                                    return failure("Invalid rootfs entry data: ${entry.name}")
                                }
                                bytesExtracted += copied
                                applyMode(outputFile, entry.mode)
                            }

                            else -> {
                                // Skip privileged special files (devices, FIFOs, sockets).
                                // The PRoot runtime is responsible for virtual bindings.
                            }
                        }
                    }
                }
            }
        }

        // Android may not allow hard links in the sandbox. Materialize copies.
        for (hardLink in deferredHardLinks) {
            val source = resolveSafeDestination(destination, hardLink.targetName)
                ?: return failure("Unsafe hard-link target: ${hardLink.targetName}")
            if (!source.isFile || !isWithinRoot(destination, source)) {
                return failure("Missing or unsafe hard-link target: ${hardLink.targetName}")
            }
            if (!ensureParent(hardLink.destination) ||
                existsWithoutFollowingLinks(hardLink.destination)
            ) {
                return failure("Invalid hard-link destination: ${hardLink.destination.name}")
            }
            val copied = copyHardLinkTarget(
                source, hardLink.destination, MAX_EXTRACTED_BYTES - bytesExtracted
            )
            if (copied < 0L) {
                return failure("Rootfs hard-link copies exceeded the allowed size.")
            }
            bytesExtracted += copied
            applyMode(hardLink.destination, hardLink.mode)
        }

        // Apply directory permissions last so extraction can traverse directories.
        deferredDirectories.asReversed().forEach { applyMode(it.file, it.mode) }
        return LinuxRootfsExtractionResult.Success(entriesExtracted, bytesExtracted)
    }

    /** Resolve against the physical root, rejecting traversal and existing symlink escapes. */
    private fun resolveSafeDestination(root: File, entryName: String): File? {
        if (entryName.isBlank() || entryName.startsWith('/') ||
            entryName.startsWith('\\') || '\u0000' in entryName
        ) return null
        return try {
            val canonicalRoot = root.canonicalFile
            val candidate = File(canonicalRoot, entryName).canonicalFile
            if (isUnder(canonicalRoot.toPath(), candidate.toPath())) candidate else null
        } catch (_: Exception) {
            null
        }
    }

    /**
     * A guest absolute link /usr/bin is rewritten as a relative link into
     * the extracted root, rather than pointing to the Android host's /usr/bin.
     * Relative links may use .. only if their normalized destination stays inside root.
     * Canonical checks also reject routes through previously created escaping links.
     */
    private fun safeSymbolicLinkTarget(root: File, link: File, linkName: String): Path? {
        if (linkName.isBlank() || '\u0000' in linkName || linkName.startsWith('\\')) return null
        return try {
            val rootPath = root.canonicalFile.toPath()
            val parentPath = link.parentFile!!.canonicalFile.toPath()
            val requested = Paths.get(linkName)
            val intended = if (requested.isAbsolute) {
                rootPath.resolve(linkName.removePrefix("/"))
            } else {
                parentPath.resolve(requested)
            }.normalize()
            if (!isUnder(rootPath, intended)) return null
            // canonicalFile resolves any already-created symbolic links in the path.
            if (!isWithinRoot(root, intended.toFile())) return null
            // Always create a relative link; absolute links could escape on Android.
            parentPath.relativize(intended)
        } catch (_: Exception) {
            null
        }
    }

    private fun ensureParent(file: File): Boolean {
        val parent = file.parentFile ?: return false
        if (existsWithoutFollowingLinks(parent)) {
            return parent.isDirectory && !Files.isSymbolicLink(parent.toPath())
        }
        return parent.mkdirs()
    }

    private fun isWithinRoot(root: File, candidate: File): Boolean = try {
        isUnder(root.canonicalFile.toPath(), candidate.canonicalFile.toPath())
    } catch (_: Exception) {
        false
    }

    private fun isUnder(root: Path, candidate: Path): Boolean =
        candidate == root || candidate.startsWith(root)

    private fun existsWithoutFollowingLinks(file: File): Boolean =
        Files.exists(file.toPath(), LinkOption.NOFOLLOW_LINKS)

    /** Files.walk does not follow symbolic links by default. */
    private fun deleteTreeWithoutFollowingLinks(directory: File) {
        if (!existsWithoutFollowingLinks(directory)) return
        Files.walk(directory.toPath()).use { stream ->
            stream.sorted(Comparator.reverseOrder()).forEach { Files.deleteIfExists(it) }
        }
    }

    private fun copyEntry(
        input: TarArchiveInputStream,
        output: OutputStream,
        remaining: Long
    ): Long {
        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
        var countTotal = 0L
        while (true) {
            val count = input.read(buffer)
            if (count <= 0) break
            if (count > remaining - countTotal) return -1L
            output.write(buffer, 0, count)
            countTotal += count
        }
        return countTotal
    }

    private fun copyHardLinkTarget(source: File, destination: File, remaining: Long): Long {
        val length = source.length()
        if (length < 0L || length > remaining) return -1L
        var copied = 0L
        source.inputStream().buffered().use { input ->
            destination.outputStream().buffered().use { output ->
                val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                while (true) {
                    val read = input.read(buffer)
                    if (read <= 0) break
                    if (read > remaining - copied) return -1L
                    output.write(buffer, 0, read)
                    copied += read
                }
            }
        }
        return copied
    }

    private fun applyMode(file: File, mode: Int) {
        runCatching { Os.chmod(file.absolutePath, mode and 0x0FFF) }
    }

    private fun failure(message: String): LinuxRootfsExtractionResult =
        LinuxRootfsExtractionResult.Failure(message = message)
}
