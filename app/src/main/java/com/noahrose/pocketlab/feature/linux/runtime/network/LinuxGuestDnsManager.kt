package com.noahrose.pocketlab.feature.linux.runtime.network

import android.content.Context
import android.net.ConnectivityManager
import java.io.File

object LinuxGuestDnsManager {

    @Volatile
    private var applicationContext:
            Context? =
        null

    /*
     * ------------------------------------------------
     * INITIALIZATION
     * ------------------------------------------------
     */
    fun initialize(
        context: Context
    ) {

        applicationContext =
            context.applicationContext
    }

    /*
     * ------------------------------------------------
     * DNS SYNCHRONIZATION
     * ------------------------------------------------
     *
     * Reads Android's currently active DNS servers
     * and writes them into:
     *
     * /etc/resolv.conf
     *
     * inside the Ubuntu rootfs.
     *
     * This avoids hard-coded public DNS servers and
     * allows Atlas to follow Wi-Fi, cellular, VPN,
     * and other Android network configurations.
     */
    fun synchronize(
        rootfsDirectory: File
    ): LinuxGuestDnsSyncResult {

        val context =
            applicationContext
                ?: return LinuxGuestDnsSyncResult
                    .Failure(
                        message =
                            "Linux guest DNS manager is not initialized."
                    )

        return try {

            val connectivityManager =
                context.getSystemService(
                    ConnectivityManager::class.java
                )
                    ?: return LinuxGuestDnsSyncResult
                        .Failure(
                            message =
                                "Android ConnectivityManager is unavailable."
                        )

            val activeNetwork =
                connectivityManager
                    .activeNetwork
                    ?: return LinuxGuestDnsSyncResult
                        .Skipped(
                            message =
                                "Android has no active network."
                        )

            val linkProperties =
                connectivityManager
                    .getLinkProperties(
                        activeNetwork
                    )
                    ?: return LinuxGuestDnsSyncResult
                        .Skipped(
                            message =
                                "Android network link properties are unavailable."
                        )

            /*
             * Android may include IPv6 scope IDs:
             *
             * fe80::1%wlan0
             *
             * resolv.conf only needs the address.
             */
            val dnsServers =
                linkProperties
                    .dnsServers
                    .mapNotNull { address ->

                        address
                            .hostAddress
                            ?.substringBefore(
                                "%"
                            )
                            ?.trim()
                            ?.takeIf { value ->

                                value.isNotBlank()
                            }
                    }
                    .distinct()

            if (
                dnsServers.isEmpty()
            ) {

                return LinuxGuestDnsSyncResult
                    .Skipped(
                        message =
                            "Android reported no DNS servers."
                    )
            }

            val etcDirectory =
                File(
                    rootfsDirectory,
                    "etc"
                )

            if (
                !etcDirectory.exists() &&
                !etcDirectory.mkdirs()
            ) {

                return LinuxGuestDnsSyncResult
                    .Failure(
                        message =
                            "Unable to create Ubuntu /etc directory."
                    )
            }

            val resolvConf =
                File(
                    etcDirectory,
                    "resolv.conf"
                )

            val content =
                buildString {

                    appendLine(
                        "# Managed by Atlas Cyberdeck"
                    )

                    appendLine(
                        "# Synchronized from Android network configuration"
                    )

                    dnsServers
                        .forEach { server ->

                            appendLine(
                                "nameserver $server"
                            )
                        }
                }

            resolvConf
                .writeText(
                    content
                )

            LinuxGuestDnsSyncResult
                .Success(
                    dnsServers =
                        dnsServers
                )

        } catch (
            exception: SecurityException
        ) {

            LinuxGuestDnsSyncResult
                .Failure(
                    message =
                        "Android network-state permission is unavailable.",
                    cause =
                        exception
                )

        } catch (
            exception: Exception
        ) {

            LinuxGuestDnsSyncResult
                .Failure(
                    message =
                        exception.message
                            ?: "Unable to synchronize Ubuntu DNS.",
                    cause =
                        exception
                )
        }
    }
}

sealed interface LinuxGuestDnsSyncResult {

    data class Success(
        val dnsServers: List<String>
    ) : LinuxGuestDnsSyncResult

    data class Skipped(
        val message: String
    ) : LinuxGuestDnsSyncResult

    data class Failure(
        val message: String,
        val cause: Throwable? = null
    ) : LinuxGuestDnsSyncResult
}