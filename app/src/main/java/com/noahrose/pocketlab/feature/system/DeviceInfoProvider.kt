package com.noahrose.pocketlab.feature.system

import android.app.ActivityManager
import android.content.Context
import android.os.Build
import android.os.Environment
import android.os.StatFs

object DeviceInfoProvider {

    private var applicationContext: Context? =
        null

    fun initialize(
        context: Context
    ) {

        applicationContext =
            context.applicationContext
    }

    fun getProfile(): DeviceProfile? {

        val context =
            applicationContext
                ?: return null

        val memoryInfo =
            getMemoryInfo(
                context
            )

        val storageInfo =
            getStorageInfo()

        return DeviceProfile(

            manufacturer =
                Build.MANUFACTURER
                    .orEmpty()
                    .ifBlank {
                        "Unknown"
                    },

            model =
                Build.MODEL
                    .orEmpty()
                    .ifBlank {
                        "Unknown"
                    },

            androidVersion =
                Build.VERSION.RELEASE
                    .orEmpty()
                    .ifBlank {
                        "Unknown"
                    },

            apiLevel =
                Build.VERSION.SDK_INT,

            architecture =
                Build.SUPPORTED_ABIS
                    .firstOrNull()
                    ?: "Unknown",

            availableProcessors =
                Runtime
                    .getRuntime()
                    .availableProcessors(),

            totalMemoryMb =
                memoryInfo.first,

            availableMemoryMb =
                memoryInfo.second,

            totalStorageMb =
                storageInfo.first,

            availableStorageMb =
                storageInfo.second,

            atlasName =
                VersionInfo.NAME,

            atlasVersion =
                VersionInfo.VERSION,

            atlasBuild =
                VersionInfo.BUILD,

            atlasCodename =
                VersionInfo.CODENAME
        )
    }

    private fun getMemoryInfo(
        context: Context
    ): Pair<Long, Long> {

        val activityManager =
            context.getSystemService(
                Context.ACTIVITY_SERVICE
            ) as? ActivityManager
                ?: return Pair(
                    0L,
                    0L
                )

        val memoryInfo =
            ActivityManager.MemoryInfo()

        activityManager.getMemoryInfo(
            memoryInfo
        )

        return Pair(
            bytesToMegabytes(
                memoryInfo.totalMem
            ),
            bytesToMegabytes(
                memoryInfo.availMem
            )
        )
    }

    private fun getStorageInfo(): Pair<Long, Long> {

        val storageDirectory =
            Environment.getDataDirectory()

        val statFs =
            StatFs(
                storageDirectory.path
            )

        return Pair(
            bytesToMegabytes(
                statFs.totalBytes
            ),
            bytesToMegabytes(
                statFs.availableBytes
            )
        )
    }

    private fun bytesToMegabytes(
        bytes: Long
    ): Long {

        return bytes /
                (
                        1024L *
                                1024L
                        )
    }
}