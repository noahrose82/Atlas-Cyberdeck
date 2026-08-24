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

        if (runtimePaths == null) {

            return LinuxProotProcessSpecResult
                .Failure(
                    message =
                        "Linux runtime paths are unavailable."
                )
        }

        /*
         * Main Atlas PRoot executable.
         *
         * This lives inside Android's extracted
         * native-library directory, which Android
         * permits the application to execute.
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
         * External PRoot ELF loader.
         *
         * Modern Android does not permit direct
         * execve() of guest binaries stored inside
         * writable application data.
         *
         * The loader therefore also lives inside
         * ApplicationInfo.nativeLibraryDir.
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

        val rootfs =
            runtimePaths.rootfsDirectory

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
         * File.isFile follows symbolic links.
         *
         * Ubuntu's usrmerge layout commonly has:
         *
         * /bin -> usr/bin
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

        val temporaryDirectory =
            runtimePaths.temporaryDirectory

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

        val arguments =
            listOf(
                /*
                 * Ensure the PRoot process tree
                 * terminates with the parent.
                 */
                "--kill-on-exit",

                /*
                 * Present root identity inside the
                 * guest without Android root.
                 */
                "-0",

                /*
                 * Ubuntu filesystem root.
                 */
                "-r",
                rootfs.absolutePath,

                /*
                 * Bind Android kernel-backed
                 * pseudo-filesystems into Ubuntu.
                 */
                "-b",
                "/dev",

                "-b",
                "/proc",

                /*
                 * Guest working directory.
                 */
                "-w",
                "/root",

                /*
                 * Ubuntu login shell.
                 */
                "/bin/sh",
                "-l"
            )

        val environment =
            mapOf(
                /*
                 * Critical Android execution fix.
                 *
                 * Instead of PRoot extracting its
                 * bundled loader into writable app
                 * storage, use the verified loader
                 * packaged in Android's executable
                 * native-library directory.
                 */
                "PROOT_LOADER" to
                        prootLoader.absolutePath,

                /*
                 * Atlas-specific PRoot supports
                 * these temporary-directory
                 * variables.
                 */
                "PROOT_TMP_DIR" to
                        temporaryDirectory.absolutePath,

                "TMPDIR" to
                        temporaryDirectory.absolutePath,

                /*
                 * Ubuntu guest environment.
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