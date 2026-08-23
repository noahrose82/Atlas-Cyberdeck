package com.noahrose.pocketlab.feature.linux.runtime.process

class AndroidLinuxProcessLauncher :
    LinuxProcessLauncher {

    override fun launch(
        spec: LinuxProcessSpec
    ): LinuxProcessLaunchResult {

        if (!spec.executable.canExecute()) {

            return LinuxProcessLaunchResult.Failure(
                message =
                    "Runtime executable is not executable: ${spec.executable.absolutePath}"
            )
        }

        if (!spec.executable.isFile) {

            return LinuxProcessLaunchResult.Failure(
                message =
                    "Runtime executable is not a file: ${spec.executable.absolutePath}"
            )
        }

        val command =
            buildList {

                add(
                    spec.executable.absolutePath
                )

                addAll(
                    spec.arguments
                )
            }

        return try {

            val builder =
                ProcessBuilder(
                    command
                )

            spec.workingDirectory
                ?.let { directory ->

                    builder.directory(
                        directory
                    )
                }

            if (
                spec.environment.isNotEmpty()
            ) {

                builder.environment()
                    .putAll(
                        spec.environment
                    )
            }

            /*
             * Keep stdout and stderr separate.
             *
             * This will allow the terminal/runtime
             * layer to present normal output and
             * errors independently later.
             */
            builder.redirectErrorStream(
                false
            )

            val process =
                builder.start()

            LinuxProcessLaunchResult.Success(
                process =
                    LinuxProcessHandle(
                        process
                    )
            )

        } catch (exception: Exception) {

            LinuxProcessLaunchResult.Failure(
                message =
                    exception.message
                        ?: "Failed to start Linux runtime process.",
                cause =
                    exception
            )
        }
    }
}