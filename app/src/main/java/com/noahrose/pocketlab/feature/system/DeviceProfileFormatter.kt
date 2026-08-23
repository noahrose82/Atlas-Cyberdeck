package com.noahrose.pocketlab.feature.system

object DeviceProfileFormatter {

    fun format(
        profile: DeviceProfile
    ): List<String> {

        return listOf(

            "Atlas Cyberdeck System Information",

            "",

            "Device",

            "Manufacturer : ${profile.manufacturer}",

            "Model        : ${profile.model}",

            "Android      : ${profile.androidVersion}",

            "API Level    : ${profile.apiLevel}",

            "Architecture : ${profile.architecture}",

            "Processors   : ${profile.availableProcessors}",

            "",

            "Memory",

            "Total        : ${profile.totalMemoryMb} MB",

            "Available    : ${profile.availableMemoryMb} MB",

            "",

            "Storage",

            "Total        : ${profile.totalStorageMb} MB",

            "Available    : ${profile.availableStorageMb} MB",

            "",

            "Atlas",

            "Name         : ${profile.atlasName}",

            "Version      : ${profile.atlasVersion}",

            "Build        : ${profile.atlasBuild}",

            "Codename     : ${profile.atlasCodename}"
        )
    }
}