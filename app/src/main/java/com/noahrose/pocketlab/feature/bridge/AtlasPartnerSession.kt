
package com.noahrose.pocketlab.feature.bridge

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import io.ktor.client.plugins.ResponseException
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Shared AtlasBridge authentication session.
 *
 * Credentials and session identifiers remain in memory.
 * No passwords or tokens are saved to disk.
 */
object AtlasPartnerSession {

    private val sessionMutex = Mutex()

    private var client: AtlasBridgeClient? = null
    private var token: String? = null

    var status by mutableStateOf("Not connected")
        private set

    var username by mutableStateOf<String?>(null)
        private set

    var busy by mutableStateOf(false)
        private set

    val isConnected: Boolean
        get() = token != null

    /**
     * Authenticate with AtlasBridge.
     */
    suspend fun login(
        baseUrl: String,
        user: String,
        password: String
    ) {
        sessionMutex.withLock {
            check(!busy) {
                "Bridge session is busy."
            }

            busy = true

            val newClient = AtlasBridgeClient(baseUrl)

            try {
                status = "Signing in..."

                val response = newClient.login(
                    username = user.trim(),
                    password = password
                )

                val previousClient = client

                client = newClient
                token = response.sessionId
                username = response.username

                status = "Authenticated as ${response.username}"

                previousClient?.close()

            } catch (error: Exception) {

                newClient.close()

                status = when (error) {
                    is ResponseException -> {
                        when (error.response.status.value) {
                            401 -> "Login rejected: Unauthorized (401)"
                            403 -> "Login rejected: Forbidden (403)"
                            404 -> "Login endpoint not found (404)"
                            else ->
                                "Bridge returned HTTP ${error.response.status.value}"
                        }
                    }

                    else ->
                        "Login failed: ${error.javaClass.simpleName}"
                }

                throw error

            } finally {
                busy = false
            }
        }
    }

    /**
     * Send a message using the shared authenticated session.
     */
    suspend fun chat(
        message: String
    ): BridgePartnerChatResponse {

        val activeClient = client
            ?: error("Connect to AtlasBridge in Settings.")

        val currentToken = token
            ?: error("Connect to AtlasBridge in Settings.")

        try {
            return activeClient.partnerChat(
                sessionId = currentToken,
                message = message
            )

        } catch (error: ResponseException) {

            if (error.response.status.value == 401) {
                if (token == currentToken) {
                    token = null
                    username = null

                    status =
                        "Session expired — sign in again under Settings"
                }
            }

            throw error
        }
    }

    /**
     * Disconnect and clear the current session.
     */
    suspend fun logout() {

        sessionMutex.withLock {

            busy = true

            val oldClient = client
            val oldToken = token

            client = null
            token = null
            username = null

            status = "Disconnected"

            try {
                if (oldClient != null && oldToken != null) {
                    oldClient.logout(oldToken)
                }

            } catch (_: Exception) {
                // Local session must be cleared even
                // when AtlasBridge is unavailable.

            } finally {
                oldClient?.close()
                busy = false
            }
        }
    }
}
