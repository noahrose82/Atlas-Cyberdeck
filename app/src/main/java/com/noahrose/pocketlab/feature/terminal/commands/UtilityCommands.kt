package com.noahrose.pocketlab.feature.terminal.commands

import com.noahrose.pocketlab.feature.filesystem.VirtualFileSystem
import com.noahrose.pocketlab.feature.linux.model.LinuxRuntimeStatus
import com.noahrose.pocketlab.feature.linux.model.runtimeStatus
import com.noahrose.pocketlab.feature.linux.repository.LinuxRepository
import com.noahrose.pocketlab.feature.linux.runtime.LinuxRuntimeController
import com.noahrose.pocketlab.feature.linux.runtime.LinuxRuntimeControlResult
import com.noahrose.pocketlab.feature.linux.runtime.filesystem.LinuxRuntimeAssetValidator
import com.noahrose.pocketlab.feature.linux.runtime.filesystem.LinuxRuntimeFilesystemManager
import com.noahrose.pocketlab.feature.linux.runtime.filesystem.LinuxRuntimeFilesystemResult
import com.noahrose.pocketlab.feature.linux.runtime.integrity.LinuxNativeRuntimeIntegrityValidator
import com.noahrose.pocketlab.feature.linux.runtime.platform.LinuxRuntimeAbiDetector
import com.noahrose.pocketlab.feature.linux.runtime.provision.LinuxRuntimeBinarySelector
import com.noahrose.pocketlab.feature.system.DeviceInfoProvider
import com.noahrose.pocketlab.feature.system.DeviceProfileFormatter
import com.noahrose.pocketlab.feature.system.VersionInfo
import com.noahrose.pocketlab.feature.system.bootstrap.DeviceBootstrapManager
import com.noahrose.pocketlab.feature.system.capability.DeviceCapabilityAnalyzer
import com.noahrose.pocketlab.feature.system.capability.DeviceCapabilityFormatter
import com.noahrose.pocketlab.feature.terminal.handler.CommandHandler
import com.noahrose.pocketlab.feature.terminal.handler.HandlerRegistry
import com.noahrose.pocketlab.feature.terminal.history.CommandHistory
import com.noahrose.pocketlab.feature.terminal.plugin.PluginRegistry
import com.noahrose.pocketlab.feature.terminal.registry.CommandRegistry
import com.noahrose.pocketlab.feature.terminal.script.ScriptEngine

object UtilityCommands : CommandHandler {

    override fun handle(
        commandName: String,
        parts: List<String>,
        output: MutableList<String>
    ): Boolean {

        when (commandName) {

            "help" -> {

                output.add(
                    "Available commands:"
                )

                output.add("")

                CommandRegistry
                    .getAll()
                    .groupBy { command ->
                        command.category
                    }
                    .toSortedMap()
                    .forEach { (category, commands) ->

                        output.add(
                            category
                        )

                        output.add(
                            "-".repeat(
                                category.length
                            )
                        )

                        commands.forEach { command ->

                            output.add(
                                "${command.name} - ${command.description}"
                            )
                        }

                        output.add("")
                    }

                return true
            }

            "sysinfo" -> {

                val profile =
                    DeviceInfoProvider
                        .getProfile()

                if (profile == null) {

                    output.add(
                        "sysinfo: device information unavailable"
                    )

                    return true
                }

                DeviceProfileFormatter
                    .format(
                        profile
                    )
                    .forEach { line ->

                        output.add(
                            line
                        )
                    }

                return true
            }

            "compatibility" -> {

                val profile =
                    DeviceBootstrapManager
                        .getProfile()

                if (profile == null) {

                    output.add(
                        "compatibility: device profile unavailable"
                    )

                    return true
                }

                val capabilities =
                    DeviceCapabilityAnalyzer
                        .analyze(
                            profile
                        )

                DeviceCapabilityFormatter
                    .format(
                        capabilities
                    )
                    .forEach { line ->

                        output.add(
                            line
                        )
                    }

                return true
            }

            "deviceprofile" -> {

                val action =
                    if (
                        parts.size < 2 ||
                        parts[1].isBlank()
                    ) {

                        ""

                    } else {

                        parts[1]
                            .trim()
                            .lowercase()
                    }

                when (action) {

                    "" -> {

                        val bootstrapped =
                            DeviceBootstrapManager
                                .isBootstrapped()

                        output.add(
                            "Atlas Device Profile"
                        )

                        output.add("")

                        output.add(
                            "Bootstrapped : ${
                                if (bootstrapped) {
                                    "YES"
                                } else {
                                    "NO"
                                }
                            }"
                        )

                        val profile =
                            DeviceBootstrapManager
                                .getProfile()

                        if (profile == null) {

                            output.add(
                                "Profile      : unavailable"
                            )

                        } else {

                            output.add(
                                "Profile      : loaded"
                            )

                            output.add("")

                            DeviceProfileFormatter
                                .format(
                                    profile
                                )
                                .forEach { line ->

                                    output.add(
                                        line
                                    )
                                }
                        }

                        return true
                    }

                    "refresh" -> {

                        val refreshed =
                            DeviceBootstrapManager
                                .refresh()

                        if (refreshed) {

                            output.add(
                                "Device profile refreshed."
                            )

                        } else {

                            output.add(
                                "deviceprofile: refresh failed"
                            )
                        }

                        return true
                    }

                    "reset" -> {

                        val reset =
                            DeviceBootstrapManager
                                .reset()

                        if (reset) {

                            output.add(
                                "Device profile reset."
                            )

                            output.add(
                                "A new profile will be created on next bootstrap."
                            )

                        } else {

                            output.add(
                                "deviceprofile: reset failed"
                            )
                        }

                        return true
                    }

                    else -> {

                        output.add(
                            "Usage: deviceprofile [refresh|reset]"
                        )

                        return true
                    }
                }
            }

            "runscript" -> {

                if (
                    parts.size < 2 ||
                    parts[1].isBlank()
                ) {

                    output.add(
                        "Usage: runscript <script.ash>"
                    )

                    return true
                }

                val scriptName =
                    parts[1]
                        .trim()

                if (
                    !scriptName.endsWith(
                        ".ash",
                        ignoreCase = true
                    )
                ) {

                    output.add(
                        "runscript: '$scriptName': Expected an .ash script"
                    )

                    return true
                }

                val scriptContent =
                    VirtualFileSystem
                        .readFile(
                            scriptName
                        )

                if (scriptContent == null) {

                    output.add(
                        "runscript: '$scriptName': Script not found"
                    )

                    return true
                }

                output.add(
                    "Executing script: $scriptName"
                )

                ScriptEngine.execute(
                    script =
                        scriptContent.lines(),
                    output =
                        output
                )

                return true
            }

            "plugins" -> {

                output.add(
                    "Installed Plugins"
                )

                output.add("")

                PluginRegistry
                    .getAll()
                    .forEach { plugin ->

                        output.add(
                            plugin.info.name
                        )

                        output.add(
                            "Version : ${plugin.info.version}"
                        )

                        output.add(
                            "Author  : ${plugin.info.author}"
                        )

                        output.add(
                            "Description : ${plugin.info.description}"
                        )

                        output.add("")
                    }

                return true
            }

            "version" -> {

                output.add(
                    VersionInfo.NAME
                )

                output.add(
                    "Version  : ${VersionInfo.VERSION}"
                )

                output.add(
                    "Build    : ${VersionInfo.BUILD}"
                )

                output.add(
                    "Codename : ${VersionInfo.CODENAME}"
                )

                output.add(
                    "Author   : ${VersionInfo.AUTHOR}"
                )

                return true
            }

            "diagnostics" -> {

                val commandCount =
                    CommandRegistry
                        .getAll()
                        .size

                val handlerCount =
                    HandlerRegistry
                        .getAll()
                        .size

                val pluginCount =
                    PluginRegistry
                        .getAll()
                        .size

                val installation =
                    LinuxRepository
                        .getInstallation()

                val linuxStatus =
                    installation
                        .runtimeStatus()
                        .label

                val filesystemResult =
                    LinuxRuntimeFilesystemManager
                        .getLastPreparationResult()

                val runtimeStorageStatus =
                    when (filesystemResult) {

                        LinuxRuntimeFilesystemResult.Ready ->
                            "READY"

                        is LinuxRuntimeFilesystemResult.Failure ->
                            "FAILED"

                        null ->
                            "NOT PREPARED"
                    }

                val runtimeAssetStatus =
                    LinuxRuntimeAssetValidator
                        .getStatus()
                        .label

                val runtimeAbi =
                    LinuxRuntimeAbiDetector
                        .getPreferredAbi()

                val runtimeAbiStatus =
                    runtimeAbi
                        ?.let { abi ->
                            "${abi.displayName} (${abi.androidName})"
                        }
                        ?: "UNSUPPORTED"

                val runtimeBinary =
                    LinuxRuntimeBinarySelector
                        .getPreferredBinary()

                val runtimeBinaryStatus =
                    runtimeBinary
                        ?.assetName
                        ?: "UNAVAILABLE"

                val runtimeIntegrity =
                    LinuxNativeRuntimeIntegrityValidator
                        .validate()

                val runtimeIntegrityStatus =
                    runtimeIntegrity
                        .status
                        .label

                val runtimeSourceStatus =
                    runtimeBinary
                        ?.source
                        ?.let { source ->
                            "${source.projectName} ${source.version}"
                        }
                        ?: "UNAVAILABLE"

                output.add(
                    "Atlas Cyberdeck Diagnostics"
                )

                output.add("")

                output.add(
                    "Version          : ${VersionInfo.VERSION}"
                )

                output.add(
                    "Filesystem       : ONLINE"
                )

                output.add(
                    "Runtime Storage  : $runtimeStorageStatus"
                )

                output.add(
                    "Runtime Assets   : $runtimeAssetStatus"
                )

                output.add(
                    "Runtime ABI      : $runtimeAbiStatus"
                )

                output.add(
                    "Runtime Binary   : $runtimeBinaryStatus"
                )

                output.add(
                    "Runtime Integrity: $runtimeIntegrityStatus"
                )

                output.add(
                    "Runtime Source   : $runtimeSourceStatus"
                )

                output.add(
                    "Command Registry : ONLINE"
                )

                output.add(
                    "Handlers         : ONLINE"
                )

                output.add(
                    "Plugins          : ONLINE"
                )

                output.add(
                    "Linux Runtime    : $linuxStatus"
                )

                output.add(
                    "Device Profile   : ${
                        if (
                            DeviceBootstrapManager
                                .isBootstrapped()
                        ) {

                            "ONLINE"

                        } else {

                            "NOT INITIALIZED"
                        }
                    }"
                )

                if (
                    filesystemResult is
                            LinuxRuntimeFilesystemResult.Failure
                ) {

                    output.add("")

                    output.add(
                        "Runtime Error   : ${filesystemResult.message}"
                    )
                }

                if (
                    runtimeIntegrity.message != null
                ) {

                    output.add("")

                    output.add(
                        "Integrity Error : ${runtimeIntegrity.message}"
                    )
                }

                output.add("")

                output.add(
                    "Commands         : $commandCount"
                )

                output.add(
                    "Handlers         : $handlerCount"
                )

                output.add(
                    "Plugins          : $pluginCount"
                )

                output.add("")

                val overallStatus =
                    when {

                        filesystemResult is
                                LinuxRuntimeFilesystemResult.Failure ->

                            "DEGRADED"

                        runtimeIntegrity.message != null ->

                            "DEGRADED"

                        else ->

                            "HEALTHY"
                    }

                output.add(
                    "Overall Status   : $overallStatus"
                )

                return true
            }

            "history" -> {

                val history =
                    CommandHistory
                        .getHistory()

                if (history.isEmpty()) {

                    output.add(
                        "No commands in history."
                    )

                } else {

                    history.forEachIndexed {
                            index,
                            command ->

                        output.add(
                            "${index + 1}  $command"
                        )
                    }
                }

                return true
            }

            "clear" -> {

                output.clear()

                return true
            }

            "whoami" -> {

                output.add(
                    "atlas"
                )

                return true
            }

            "pwd" -> {

                output.add(
                    VirtualFileSystem
                        .currentPath
                        .value
                        .replace(
                            "~",
                            "/home/atlas"
                        )
                )

                return true
            }

            "status" -> {

                val installation =
                    LinuxRepository
                        .getInstallation()

                val linuxStatus =
                    installation
                        .runtimeStatus()
                        .label

                output.add(
                    "Atlas Cyberdeck"
                )

                output.add(
                    "Status : ONLINE"
                )

                output.add(
                    "Linux : $linuxStatus"
                )

                output.add(
                    "Terminal : ACTIVE"
                )

                return true
            }

            "linux" -> {

                val action =
                    if (
                        parts.size < 2 ||
                        parts[1].isBlank()
                    ) {

                        "status"

                    } else {

                        parts[1]
                            .trim()
                            .lowercase()
                    }

                when (action) {

                    "status" -> {

                        val installation =
                            LinuxRepository
                                .getInstallation()

                        val installationStatus =
                            if (installation.installed) {

                                "INSTALLED"

                            } else {

                                "NOT INSTALLED"
                            }

                        val runtimeState =
                            installation
                                .runtimeStatus()

                        val runtimeStatus =
                            when (runtimeState) {

                                LinuxRuntimeStatus.NOT_INSTALLED ->
                                    "UNAVAILABLE"

                                else ->
                                    runtimeState.label
                            }

                        val distribution =
                            installation
                                .distribution
                                .name
                                .lowercase()
                                .replaceFirstChar { character ->
                                    character.uppercase()
                                }

                        output.add(
                            "Linux Runtime"
                        )

                        output.add(
                            "Distribution : $distribution"
                        )

                        output.add(
                            "Version      : ${installation.version}"
                        )

                        output.add(
                            "Installation : $installationStatus"
                        )

                        output.add(
                            "Runtime      : $runtimeStatus"
                        )

                        return true
                    }

                    "start" -> {

                        when (
                            LinuxRuntimeController
                                .start()
                        ) {

                            LinuxRuntimeControlResult.STARTED -> {

                                output.add(
                                    "Linux runtime started."
                                )
                            }

                            LinuxRuntimeControlResult.ALREADY_RUNNING -> {

                                output.add(
                                    "Linux runtime is already running."
                                )
                            }

                            LinuxRuntimeControlResult.NOT_INSTALLED -> {

                                output.add(
                                    "linux: Ubuntu is not installed."
                                )

                                output.add(
                                    "Open Linux Manager to install the environment."
                                )
                            }

                            LinuxRuntimeControlResult.INSTALLATION_IN_PROGRESS -> {

                                output.add(
                                    "linux: installation is currently in progress."
                                )
                            }

                            LinuxRuntimeControlResult.FEATURE_UNAVAILABLE -> {

                                output.add(
                                    "linux: runtime is unavailable on this device."
                                )
                            }

                            LinuxRuntimeControlResult.START_FAILED -> {

                                output.add(
                                    "linux: unable to start runtime."
                                )
                            }

                            else -> {

                                output.add(
                                    "linux: unable to start runtime."
                                )
                            }
                        }

                        return true
                    }

                    "stop" -> {

                        when (
                            LinuxRuntimeController
                                .stop()
                        ) {

                            LinuxRuntimeControlResult.STOPPED -> {

                                output.add(
                                    "Linux runtime stopped."
                                )
                            }

                            LinuxRuntimeControlResult.ALREADY_STOPPED -> {

                                output.add(
                                    "Linux runtime is already stopped."
                                )
                            }

                            LinuxRuntimeControlResult.NOT_INSTALLED -> {

                                output.add(
                                    "linux: Ubuntu is not installed."
                                )
                            }

                            LinuxRuntimeControlResult.STOP_FAILED -> {

                                output.add(
                                    "linux: unable to stop runtime."
                                )
                            }

                            else -> {

                                output.add(
                                    "linux: unable to stop runtime."
                                )
                            }
                        }

                        return true
                    }

                    else -> {

                        output.add(
                            "Usage: linux [status|start|stop]"
                        )

                        return true
                    }
                }
            }

            "neofetch" -> {

                val installation =
                    LinuxRepository
                        .getInstallation()

                val linuxStatus =
                    installation
                        .runtimeStatus()
                        .label

                output.add(
                    "${VersionInfo.NAME} ${VersionInfo.VERSION}"
                )

                output.add(
                    "Build   : ${VersionInfo.BUILD}"
                )

                output.add(
                    "Codename: ${VersionInfo.CODENAME}"
                )

                output.add(
                    "Author  : ${VersionInfo.AUTHOR}"
                )

                output.add(
                    "Shell   : Atlas Terminal"
                )

                if (installation.installed) {

                    val distribution =
                        installation
                            .distribution
                            .name
                            .lowercase()
                            .replaceFirstChar { character ->
                                character.uppercase()
                            }

                    output.add(
                        "Linux   : $distribution ${installation.version}"
                    )

                    output.add(
                        "Runtime : $linuxStatus"
                    )

                } else {

                    output.add(
                        "Linux   : Not installed"
                    )
                }

                return true
            }
        }

        return false
    }
}