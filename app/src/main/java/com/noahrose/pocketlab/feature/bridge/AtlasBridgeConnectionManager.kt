package com.noahrose.pocketlab.feature.bridge

import android.content.Context
import com.noahrose.pocketlab.feature.settings.AtlasSettingsRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class AtlasBridgeConnectionManager(
    context: Context
) {

    private val applicationContext =
        context.applicationContext

    private val identityStore =
        CyberdeckIdentityStore(
            applicationContext
        )

    private val scope =
        CoroutineScope(
            SupervisorJob() +
                    Dispatchers.IO
        )

    private var connectionJob: Job? =
        null

    @Volatile
    var connected: Boolean = false
        private set

    init {
        /*
         * Initialization is idempotent. Calling it here
         * guarantees Bridge settings are available even if
         * application startup order changes later.
         */
        AtlasSettingsRepository
            .initialize(
                applicationContext
            )
    }

    fun start() {
        if (
            connectionJob?.isActive == true
        ) {
            return
        }

        connectionJob =
            scope.launch {
                runConnectionLoop()
            }
    }

    private suspend fun runConnectionLoop() {

        val identity =
            identityStore
                .getIdentity()

        while (
            scope.isActive
        ) {

            val bridgeEnabled =
                AtlasSettingsRepository
                    .isAtlasBridgeEnabled()

            val configuredAddress =
                normalizedConfiguredAddress()

            /*
             * Bridge is optional. When disabled or not yet
             * configured, Cyberdeck performs no Bridge
             * network traffic and continues normally.
             */
            if (
                !bridgeEnabled ||
                configuredAddress.isBlank()
            ) {

                connected =
                    false

                delay(
                    SETTINGS_CHECK_INTERVAL_MILLIS
                )

                continue
            }

            val bridgeClient =
                AtlasBridgeClient(
                    baseUrl =
                        configuredAddress
                )

            try {

                val connectResponse =
                    bridgeClient
                        .connect(
                            identity
                        )

                connected =
                    connectResponse.status ==
                            "CONNECTED"

                if (
                    !connected
                ) {

                    delay(
                        RETRY_DELAY_MILLIS
                    )

                    continue
                }

                while (
                    scope.isActive &&
                    connected
                ) {

                    delay(
                        HEARTBEAT_INTERVAL_MILLIS
                    )

                    /*
                     * Re-read settings before every heartbeat.
                     * Disabling Bridge or changing its address
                     * ends the current relationship cleanly,
                     * then the outer loop applies the latest
                     * configuration.
                     */
                    val stillEnabled =
                        AtlasSettingsRepository
                            .isAtlasBridgeEnabled()

                    val currentAddress =
                        normalizedConfiguredAddress()

                    if (
                        !stillEnabled ||
                        currentAddress !=
                        configuredAddress
                    ) {

                        connected =
                            false

                        try {

                            bridgeClient
                                .disconnect(
                                    identity.cyberdeckId
                                )

                        } catch (
                            exception: CancellationException
                        ) {

                            throw exception

                        } catch (
                            exception: Exception
                        ) {

                            /*
                             * Bridge may already be unavailable.
                             * Cyberdeck must never depend on a
                             * successful disconnect response.
                             */
                        }

                        break
                    }

                    val heartbeat =
                        bridgeClient
                            .heartbeat(
                                identity.cyberdeckId
                            )

                    connected =
                        heartbeat.status ==
                                "ALIVE"
                }

            } catch (
                exception: CancellationException
            ) {

                throw exception

            } catch (
                exception: Exception
            ) {

                connected =
                    false

                delay(
                    RETRY_DELAY_MILLIS
                )

            } finally {

                bridgeClient
                    .close()
            }
        }
    }

    fun stop() {

        connectionJob
            ?.cancel()

        connectionJob =
            null

        connected =
            false
    }

    suspend fun disconnect() {

        val identity =
            identityStore
                .getIdentity()

        val configuredAddress =
            normalizedConfiguredAddress()

        if (
            configuredAddress.isNotBlank()
        ) {

            val bridgeClient =
                AtlasBridgeClient(
                    baseUrl =
                        configuredAddress
                )

            try {

                bridgeClient
                    .disconnect(
                        identity.cyberdeckId
                    )

            } catch (
                exception: Exception
            ) {

                /*
                 * Bridge may already be unavailable.
                 * Cyberdeck shutdown must never depend on it.
                 */

            } finally {

                bridgeClient
                    .close()
            }
        }

        stop()
    }

    fun close() {

        stop()

        scope
            .cancel()
    }

    private fun normalizedConfiguredAddress():
            String {

        return AtlasSettingsRepository
            .getAtlasBridgeAddress()
            .trim()
            .removeSuffix(
                "/"
            )
    }

    companion object {

        private const val HEARTBEAT_INTERVAL_MILLIS =
            5_000L

        private const val RETRY_DELAY_MILLIS =
            5_000L

        private const val SETTINGS_CHECK_INTERVAL_MILLIS =
            1_000L
    }
}
