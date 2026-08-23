package com.noahrose.pocketlab.feature.system

data class DeviceProfile(

    val manufacturer: String,

    val model: String,

    val androidVersion: String,

    val apiLevel: Int,

    val architecture: String,

    val availableProcessors: Int,

    val totalMemoryMb: Long,

    val availableMemoryMb: Long,

    val totalStorageMb: Long,

    val availableStorageMb: Long,

    val atlasName: String,

    val atlasVersion: String,

    val atlasBuild: String,

    val atlasCodename: String
)