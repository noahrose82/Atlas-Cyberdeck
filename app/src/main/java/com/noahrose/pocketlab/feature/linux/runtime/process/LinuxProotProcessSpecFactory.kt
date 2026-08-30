package com.noahrose.pocketlab.feature.linux.runtime.process

import com.noahrose.pocketlab.feature.linux.runtime.filesystem.LinuxRuntimePathManager
import com.noahrose.pocketlab.feature.linux.runtime.platform.LinuxNativeRuntimeResolver
import java.io.File

object LinuxProotProcessSpecFactory {

    fun create():
            LinuxProotProcessSpecResult {

        /*
         * Resolve Atlas Linux runtime paths.
         */
        val runtimePaths =
            try {

                LinuxRuntimePathManager
                    .getPaths()

            } catch (
                exception: IllegalStateException
            ) {

                return LinuxProotProcessSpecResult
                    .Failure(
                        message =
                            exception.message
                                ?: "Linux runtime paths are unavailable."
                    )
            }

        val rootfsDirectory =
            runtimePaths
                .rootfsDirectory

        /*
         * Ubuntu RootFS must already exist before
         * Atlas attempts to launch PRoot.
         */
        if (
            !rootfsDirectory.exists() ||
            !rootfsDirectory.isDirectory
        ) {

            return LinuxProotProcessSpecResult
                .Failure(
                    message =
                        "Ubuntu root filesystem is unavailable: " +
                                rootfsDirectory.absolutePath
                )
        }

        /*
         * Verify the guest shell.
         */
        val guestShell =
            File(
                rootfsDirectory,
                "bin/sh"
            )

        if (
            !guestShell.exists() ||
            !guestShell.isFile
        ) {

            return LinuxProotProcessSpecResult
                .Failure(
                    message =
                        "Ubuntu guest shell is unavailable: " +
                                guestShell.absolutePath
                )
        }

        /*
         * Main Atlas PRoot executable.
         *
         * Android extracts this native library into the
         * application's executable native-library directory.
         */
        val prootExecutable =
            LinuxNativeRuntimeResolver
                .getProotExecutable()

        if (
            prootExecutable == null
        ) {

            return LinuxProotProcessSpecResult
                .Failure(
                    message =
                        "Atlas PRoot executable could not be resolved."
                )
        }

        if (
            !prootExecutable.exists() ||
            !prootExecutable.isFile
        ) {

            return LinuxProotProcessSpecResult
                .Failure(
                    message =
                        "Atlas PRoot executable is missing: " +
                                prootExecutable.absolutePath
                )
        }

        if (
            !prootExecutable.canExecute()
        ) {

            return LinuxProotProcessSpecResult
                .Failure(
                    message =
                        "Atlas PRoot executable is not executable: " +
                                prootExecutable.absolutePath
                )
        }

        /*
         * PRoot loader.
         */
        val prootLoader =
            LinuxNativeRuntimeResolver
                .getProotLoaderExecutable()

        if (
            prootLoader == null
        ) {

            return LinuxProotProcessSpecResult
                .Failure(
                    message =
                        "Atlas PRoot loader could not be resolved."
                )
        }

        if (
            !prootLoader.exists() ||
            !prootLoader.isFile
        ) {

            return LinuxProotProcessSpecResult
                .Failure(
                    message =
                        "Atlas PRoot loader is missing: " +
                                prootLoader.absolutePath
                )
        }

        if (
            !prootLoader.canExecute()
        ) {

            return LinuxProotProcessSpecResult
                .Failure(
                    message =
                        "Atlas PRoot loader is not executable: " +
                                prootLoader.absolutePath
                )
        }

        /*
         * ------------------------------------------------
         * PROOT TEMP STORAGE
         * ------------------------------------------------
         */
        val prootTemporaryDirectory =
            runtimePaths
                .temporaryDirectory

        if (
            !ensureDirectory(
                prootTemporaryDirectory
            )
        ) {

            return LinuxProotProcessSpecResult
                .Failure(
                    message =
                        "Unable to prepare PRoot temporary storage: " +
                                prootTemporaryDirectory.absolutePath
                )
        }

        /*
         * ------------------------------------------------
         * LINK-TO-SYMLINK STORAGE
         * ------------------------------------------------
         *
         * Required for Debian package filesystem behavior
         * under PRoot on Android.
         */
        val link2SymlinkDirectory =
            File(
                rootfsDirectory,
                ".l2s"
            )

        if (
            !ensureDirectory(
                link2SymlinkDirectory
            )
        ) {

            return LinuxProotProcessSpecResult
                .Failure(
                    message =
                        "Unable to prepare PRoot link-to-symlink storage: " +
                                link2SymlinkDirectory.absolutePath
                )
        }

        /*
         * Guest /root.
         */
        val guestRootDirectory =
            File(
                rootfsDirectory,
                "root"
            )

        if (
            !ensureDirectory(
                guestRootDirectory
            )
        ) {

            return LinuxProotProcessSpecResult
                .Failure(
                    message =
                        "Ubuntu root home directory is unavailable: " +
                                guestRootDirectory.absolutePath
                )
        }

        /*
         * Guest /tmp.
         */
        val guestTemporaryDirectory =
            File(
                rootfsDirectory,
                "tmp"
            )

        if (
            !ensureDirectory(
                guestTemporaryDirectory
            )
        ) {

            return LinuxProotProcessSpecResult
                .Failure(
                    message =
                        "Ubuntu temporary directory is unavailable: " +
                                guestTemporaryDirectory.absolutePath
                )
        }

        /*
         * PRoot native libraries live beside the
         * executable.
         */
        val nativeLibraryDirectory =
            prootExecutable
                .parentFile

        if (
            nativeLibraryDirectory == null ||
            !nativeLibraryDirectory.exists() ||
            !nativeLibraryDirectory.isDirectory
        ) {

            return LinuxProotProcessSpecResult
                .Failure(
                    message =
                        "Atlas native runtime directory is unavailable."
                )
        }

        /*
         * ------------------------------------------------
         * PROOT ARGUMENTS
         * ------------------------------------------------
         */
        val arguments =
            buildList {

                /*
                 * Present the guest as UID 0.
                 */
                add(
                    "-0"
                )

                /*
                 * Required for package filesystem
                 * compatibility.
                 */
                add(
                    "--link2symlink"
                )

                /*
                 * Ubuntu root filesystem.
                 */
                add(
                    "-r"
                )

                add(
                    rootfsDirectory
                        .absolutePath
                )

                /*
                 * Android kernel/device interfaces.
                 */
                add(
                    "-b"
                )

                add(
                    "/dev"
                )

                add(
                    "-b"
                )

                add(
                    "/proc"
                )

                add(
                    "-b"
                )

                add(
                    "/sys"
                )

                /*
                 * Start Ubuntu in /root.
                 */
                add(
                    "-w"
                )

                add(
                    "/root"
                )

                /*
                 * Persistent guest shell used by Atlas.
                 */
                add(
                    "/bin/sh"
                )
            }

        /*
         * ------------------------------------------------
         * PROOT ENVIRONMENT
         * ------------------------------------------------
         */
        val environment =
            mapOf(

                "PROOT_LOADER" to
                        prootLoader.absolutePath,

                "PROOT_TMP_DIR" to
                        prootTemporaryDirectory.absolutePath,

                "PROOT_L2S_DIR" to
                        link2SymlinkDirectory.absolutePath,

                "LD_LIBRARY_PATH" to
                        nativeLibraryDirectory.absolutePath,

                "HOME" to
                        "/root",

                "USER" to
                        "root",

                "LOGNAME" to
                        "root",

                "SHELL" to
                        "/bin/sh",

                "PATH" to
                        "/usr/local/sbin:" +
                        "/usr/local/bin:" +
                        "/usr/sbin:" +
                        "/usr/bin:" +
                        "/sbin:" +
                        "/bin",

                "TERM" to
                        "xterm-256color",

                "LANG" to
                        "C.UTF-8"
            )

        return LinuxProotProcessSpecResult
            .Ready(
                spec =
                    LinuxProcessSpec(
                        executable =
                            prootExecutable,

                        arguments =
                            arguments,

                        /*
                         * ProotLinuxRuntimeBackend uses this
                         * directory for Ubuntu DNS sync before
                         * launch.
                         */
                        workingDirectory =
                            rootfsDirectory,

                        environment =
                            environment
                    )
            )
    }

    private fun ensureDirectory(
        directory: File
    ): Boolean {

        if (
            directory.exists()
        ) {

            return directory
                .isDirectory
        }

        return runCatching {

            directory.mkdirs()

        }.getOrDefault(
            false
        )
    }
}