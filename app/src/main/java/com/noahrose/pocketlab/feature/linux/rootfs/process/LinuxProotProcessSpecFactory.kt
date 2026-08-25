package com.noahrose.pocketlab.feature.linux.runtime.process

import com.noahrose.pocketlab.feature.linux.runtime.filesystem.LinuxRuntimePathManager
import com.noahrose.pocketlab.feature.linux.runtime.platform.LinuxNativeRuntimeResolver
import java.io.File

object LinuxProotProcessSpecFactory {

    fun create():
            LinuxProotProcessSpecResult {

        val runtimePaths =
            LinuxRuntimePathManager
                .getPaths()

        if (
            runtimePaths == null
        ) {

            return LinuxProotProcessSpecResult
                .Failure(
                    message =
                        "Linux runtime paths are unavailable."
                )
        }

        /*
         * ------------------------------------------------
         * PROOT EXECUTABLE
         * ------------------------------------------------
         */
        val prootExecutable =
            LinuxNativeRuntimeResolver
                .getProotExecutable()

        if (
            prootExecutable == null ||
            !prootExecutable.exists() ||
            !prootExecutable.isFile ||
            !prootExecutable.canExecute()
        ) {

            return LinuxProotProcessSpecResult
                .Failure(
                    message =
                        "Verified PRoot executable is unavailable."
                )
        }

        /*
         * ------------------------------------------------
         * EXTERNAL PROOT LOADER
         * ------------------------------------------------
         */
        val prootLoader =
            LinuxNativeRuntimeResolver
                .getProotLoaderExecutable()

        if (
            prootLoader == null ||
            !prootLoader.exists() ||
            !prootLoader.isFile ||
            !prootLoader.canExecute()
        ) {

            return LinuxProotProcessSpecResult
                .Failure(
                    message =
                        "Verified PRoot guest loader is unavailable."
                )
        }

        /*
         * ------------------------------------------------
         * UBUNTU ROOTFS
         * ------------------------------------------------
         */
        val rootfs =
            runtimePaths
                .rootfsDirectory

        if (
            !rootfs.exists() ||
            !rootfs.isDirectory
        ) {

            return LinuxProotProcessSpecResult
                .Failure(
                    message =
                        "Ubuntu rootfs is unavailable."
                )
        }

        /*
         * Ubuntu usrmerge commonly uses:
         *
         * /bin -> /usr/bin
         * /bin/sh -> dash
         */
        val shell =
            File(
                rootfs,
                "bin/sh"
            )

        if (
            !shell.exists() ||
            !shell.isFile
        ) {

            return LinuxProotProcessSpecResult
                .Failure(
                    message =
                        "Ubuntu rootfs does not contain /bin/sh."
                )
        }

        /*
         * ------------------------------------------------
         * PROOT HOST TEMPORARY STORAGE
         * ------------------------------------------------
         *
         * This path is used internally by PRoot itself
         * and therefore must remain an Android host path.
         */
        val temporaryDirectory =
            runtimePaths
                .temporaryDirectory

        if (
            !temporaryDirectory.exists() &&
            !temporaryDirectory.mkdirs()
        ) {

            return LinuxProotProcessSpecResult
                .Failure(
                    message =
                        "Unable to prepare the PRoot temporary directory."
                )
        }

        /*
         * ------------------------------------------------
         * UBUNTU GUEST /tmp
         * ------------------------------------------------
         *
         * Software executing inside Ubuntu must see a
         * guest filesystem path such as /tmp.
         *
         * It must NOT receive Android paths such as:
         *
         * /data/user/0/.../cache/linux/tmp
         */
        val guestTemporaryDirectory =
            File(
                rootfs,
                "tmp"
            )

        if (
            !guestTemporaryDirectory.exists() &&
            !guestTemporaryDirectory.mkdirs()
        ) {

            return LinuxProotProcessSpecResult
                .Failure(
                    message =
                        "Unable to prepare the Ubuntu /tmp directory."
                )
        }

        /*
         * Ensure root and guest applications can use
         * Ubuntu's temporary directory.
         */
        guestTemporaryDirectory
            .setReadable(
                true,
                false
            )

        guestTemporaryDirectory
            .setWritable(
                true,
                false
            )

        guestTemporaryDirectory
            .setExecutable(
                true,
                false
            )

        /*
         * ------------------------------------------------
         * LINK2SYMLINK BACKING STORE
         * ------------------------------------------------
         *
         * Android SELinux prevents normal hard-link
         * behavior in some app-storage situations.
         *
         * PRoot's link2symlink extension stores its
         * emulation metadata here.
         */
        val link2SymlinkDirectory =
            File(
                rootfs,
                ".l2s"
            )

        if (
            !link2SymlinkDirectory.exists() &&
            !link2SymlinkDirectory.mkdirs()
        ) {

            return LinuxProotProcessSpecResult
                .Failure(
                    message =
                        "Unable to prepare the PRoot link2symlink directory."
                )
        }

        /*
         * ------------------------------------------------
         * UBUNTU ROOT HOME
         * ------------------------------------------------
         */
        val homeDirectory =
            File(
                rootfs,
                "root"
            )

        if (
            !homeDirectory.exists() &&
            !homeDirectory.mkdirs()
        ) {

            return LinuxProotProcessSpecResult
                .Failure(
                    message =
                        "Unable to prepare the Ubuntu root home directory."
                )
        }

        /*
         * ------------------------------------------------
         * PROOT ARGUMENTS
         * ------------------------------------------------
         */
        val arguments =
            listOf(

                /*
                 * Terminate PRoot children when the
                 * parent runtime terminates.
                 */
                "--kill-on-exit",

                /*
                 * Android hard-link compatibility.
                 */
                "--link2symlink",

                /*
                 * Correct lstat behavior for symlinks.
                 */
                "-L",

                /*
                 * Present root identity inside Ubuntu
                 * without requiring Android root.
                 */
                "-0",

                /*
                 * Ubuntu filesystem root.
                 */
                "-r",
                rootfs.absolutePath,

                /*
                 * Android kernel-backed pseudo filesystems.
                 */
                "-b",
                "/dev",

                "-b",
                "/proc",

                "-b",
                "/sys",

                /*
                 * Initial Ubuntu working directory.
                 */
                "-w",
                "/root",

                /*
                 * Persistent Ubuntu shell.
                 */
                "/bin/sh",
                "-l"
            )

        /*
         * ------------------------------------------------
         * PROOT + UBUNTU ENVIRONMENT
         * ------------------------------------------------
         */
        val environment =
            mapOf(

                /*
                 * Verified external PRoot loader.
                 */
                "PROOT_LOADER" to
                        prootLoader.absolutePath,

                /*
                 * Dedicated hard-link metadata store.
                 *
                 * PRoot receives the Android-visible
                 * rootfs path.
                 */
                "PROOT_L2S_DIR" to
                        link2SymlinkDirectory.absolutePath,

                /*
                 * PRoot's own temporary storage.
                 *
                 * This MUST remain an Android host path.
                 */
                "PROOT_TMP_DIR" to
                        temporaryDirectory.absolutePath,

                /*
                 * ------------------------------------------------
                 * UBUNTU TEMPORARY ENVIRONMENT
                 * ------------------------------------------------
                 *
                 * Guest software such as:
                 *
                 * mktemp
                 * dpkg
                 * ca-certificates
                 * apt
                 *
                 * must use Ubuntu's /tmp rather than
                 * Android's private host path.
                 */
                "TMPDIR" to
                        "/tmp",

                "TMP" to
                        "/tmp",

                "TEMP" to
                        "/tmp",

                /*
                 * Ubuntu identity/environment.
                 */
                "HOME" to
                        "/root",

                "USER" to
                        "root",

                "LOGNAME" to
                        "root",

                "SHELL" to
                        "/bin/sh",

                "TERM" to
                        "xterm-256color",

                "LANG" to
                        "C.UTF-8",

                "LC_ALL" to
                        "C.UTF-8",

                "PATH" to
                        "/usr/local/sbin:" +
                        "/usr/local/bin:" +
                        "/usr/sbin:" +
                        "/usr/bin:" +
                        "/sbin:" +
                        "/bin"
            )

        return LinuxProotProcessSpecResult
            .Ready(
                spec =
                    LinuxProcessSpec(
                        executable =
                            prootExecutable,

                        arguments =
                            arguments,

                        workingDirectory =
                            rootfs,

                        environment =
                            environment
                    )
            )
    }
}