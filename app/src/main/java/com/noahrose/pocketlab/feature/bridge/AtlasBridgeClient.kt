package com.noahrose.pocketlab.feature.bridge

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

class AtlasBridgeClient(baseUrl: String) {

    private val normalizedBaseUrl = baseUrl.trim().removeSuffix("/")

    private val client = HttpClient(OkHttp) {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
        install(HttpTimeout) {
            connectTimeoutMillis = 2_000
            requestTimeoutMillis = 10_000
            socketTimeoutMillis = 10_000
        }
    }

    suspend fun connect(identity: CyberdeckIdentity): BridgeConnectResponse =
        client.post("$normalizedBaseUrl/connect") {
            contentType(ContentType.Application.Json)
            setBody(
                BridgeConnectRequest(
                    cyberdeckId = identity.cyberdeckId,
                    deviceName = identity.deviceName
                )
            )
        }.body()

    suspend fun heartbeat(cyberdeckId: String): BridgeHeartbeatResponse =
        client.post("$normalizedBaseUrl/heartbeat") {
            contentType(ContentType.Application.Json)
            setBody(BridgeHeartbeatRequest(cyberdeckId = cyberdeckId))
        }.body()

    suspend fun disconnect(cyberdeckId: String) {
        client.post("$normalizedBaseUrl/disconnect") {
            contentType(ContentType.Application.Json)
            setBody(BridgeDisconnectRequest(cyberdeckId = cyberdeckId))
        }
    }

    /**
     * Authenticates against the desktop AtlasBridge server.
     * Do not log or persist the password or returned session ID.
     * Call from a coroutine, not the UI thread.
     */
    suspend fun login(username: String, password: String): BridgeLoginResponse =
        client.post("$normalizedBaseUrl/auth/login") {
            contentType(ContentType.Application.Json)
            setBody(BridgeLoginRequest(username = username, password = password))
        }.body()

    /**
     * Sends a Partner message using an existing AtlasBridge session.
     * The endpoint currently responds with AI_NOT_CONNECTED until an AI model is configured.
     */
    suspend fun partnerChat(
        sessionId: String,
        message: String
    ): BridgePartnerChatResponse {
        require(sessionId.isNotBlank()) { "AtlasBridge session is required." }
        require(message.isNotBlank()) { "Message cannot be blank." }

        return client.post("$normalizedBaseUrl/partner/chat") {
            header("X-Atlas-Session", sessionId)
            contentType(ContentType.Application.Json)
            setBody(BridgePartnerChatRequest(message = message))
        }.body()
    }

    suspend fun logout(sessionId: String) {
        if (sessionId.isBlank()) return
        client.post("$normalizedBaseUrl/auth/logout") {
            header("X-Atlas-Session", sessionId)
            contentType(ContentType.Application.Json)
        }
    }

    fun close() {
        client.close()
    }
}

@Serializable
data class BridgeConnectRequest(
    val cyberdeckId: String,
    val deviceName: String
)

@Serializable
data class BridgeConnectResponse(
    val status: String,
    val bridgeId: String,
    val cyberdeckId: String,
    val deviceName: String
)

@Serializable
data class BridgeHeartbeatRequest(val cyberdeckId: String)

@Serializable
data class BridgeHeartbeatResponse(
    val status: String,
    val bridgeId: String,
    val cyberdeckId: String,
    val lastSeen: String?
)

@Serializable
data class BridgeDisconnectRequest(val cyberdeckId: String)

@Serializable
data class BridgeLoginRequest(
    val username: String,
    val password: String
)

@Serializable
data class BridgeLoginResponse(
    val status: String,
    val sessionId: String,
    val username: String,
    val role: String,
    val expiresAt: String
)

@Serializable
data class BridgePartnerChatRequest(val message: String)

@Serializable
data class BridgePartnerChatResponse(
    val status: String,
    val message: String
)
