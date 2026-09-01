package com.noahrose.pocketlab.feature.linux.runtime.process

import com.noahrose.pocketlab.feature.linux.runtime.filesystem.LinuxRuntimePathManager
import com.noahrose.pocketlab.feature.linux.runtime.platform.LinuxNativeRuntimeResolver
import java.io.File

object LinuxProotProcessSpecFactory {

    fun create():
            LinuxProotProcessSpecResult {

        /*
         * Resolve Atlas runtime storage.
         *
         * LinuxRuntimePathManager fails explicitly when
         * initialization has not occurred, so convert
         * that condition into a normal backend failure.
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
         * A provisioned Ubuntu root filesystem must exist
         * before PRoot startup can proceed.
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
         * Verify that the guest shell actually exists.
         *
         * Atlas keeps one persistent shell process alive
         * and communicates with it through stdin/stdout.
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
         * This executable lives in Android's extracted
         * native-library directory.
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
         * External PRoot loader.
         *
         * Android permits execution from the application's
         * extracted native-library directory, so Atlas keeps
         * the loader there as well.
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
         * PROOT TEMPORARY STORAGE
         * ------------------------------------------------
         *
         * This directory belongs to PRoot itself.
         *
         * It is deliberately separate from Ubuntu /tmp.
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
         * Android application storage does not provide
         * normal Linux hard-link semantics required by
         * Debian package operations.
         *
         * PRoot --link2symlink translates hard-link
         * operations into PRoot-managed symbolic links.
         *
         * PROOT_L2S_DIR stores the backing objects used
         * by that emulation.
         *
         * IMPORTANT:
         *
         * Never casually delete this directory.
         * Installed packages may depend on its contents.
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
         * ------------------------------------------------
         * GUEST-VISIBLE L2S MIRROR
         * ------------------------------------------------
         *
         * PROOT_L2S_DIR contains an Android host absolute
         * path, for example:
         *
         * /data/user/0/.../linux/rootfs/.l2s
         *
         * PRoot's link2symlink implementation can write
         * symbolic-link targets containing that absolute
         * path.
         *
         * Once PRoot changes the visible root to Ubuntu,
         * that Android path does not automatically exist
         * inside the guest.
         *
         * Therefore create the guest-side destination tree
         * before installing the self-bind below.
         *
         * Example guest-visible backing location:
         *
         * rootfs/
         *   data/
         *     user/
         *       0/
         *         .../
         *           linux/
         *             rootfs/
         *               .l2s/
         *
         * The actual contents are supplied by PRoot's bind.
         */
        val link2SymlinkGuestMirror =
            File(
                rootfsDirectory,
                link2SymlinkDirectory
                    .absolutePath
                    .removePrefix(
                        File.separator
                    )
            )

        if (
            !ensureDirectory(
                link2SymlinkGuestMirror
            )
        ) {

            return LinuxProotProcessSpecResult
                .Failure(
                    message =
                        "Unable to prepare the guest-visible PRoot " +
                                "link-to-symlink path: " +
                                link2SymlinkGuestMirror.absolutePath
                )
        }

        /*
         * Ensure the guest's normal working directory is
         * available.
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
         * Ensure Ubuntu /tmp exists.
         *
         * Guest /tmp and PRoot's host temporary directory
         * intentionally remain separate.
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
         * Native libraries used by PRoot live beside the
         * executable in Android's native-library directory.
         */
        val nativeLibraryDirectory =
            prootExecutable
                .parentFile

        if (
            nativeLibraryDirectory == null ||
            !nativeLibraryDirectory.exists()
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
         *
         * -0
         *     Present guest processes as root.
         *
         * --link2symlink
         *     Emulate hard links using PRoot-managed
         *     symbolic-link storage.
         *
         * -r
         *     Use the provisioned Ubuntu rootfs.
         *
         * -b
         *     Expose required Android/kernel paths.
         *
         * -w
         *     Enter Ubuntu at /root.
         *
         * /bin/sh
         *     Keep a persistent guest shell alive for the
         *     Atlas command bridge.
         */
        val arguments =
            buildList {

                add(
                    "-0"
                )

                add(
                    "--link2symlink"
                )

                add(
                    "-r"
                )

                add(
                    rootfsDirectory
                        .absolutePath
                )

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
                 * Keep PRoot's absolute L2S backing path
                 * reachable at the SAME absolute path from
                 * inside Ubuntu.
                 *
                 * The guest destination tree was explicitly
                 * created above before this bind is added.
                 */
                add(
                    "-b"
                )

                add(
                    "${link2SymlinkDirectory.absolutePath}:" +
                            link2SymlinkDirectory.absolutePath
                )

                add(
                    "-w"
                )

                add(
                    "/root"
                )

                add(
                    "/bin/sh"
                )
            }

        /*
         * ------------------------------------------------
         * PROOT / UBUNTU ENVIRONMENT
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
                         * The backend intentionally uses
                         * this host path to locate the
                         * Ubuntu rootfs for DNS sync.
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

            directory
                .mkdirs()

        }.getOrDefault(
            false
        )
    }
}

