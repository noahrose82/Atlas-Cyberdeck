package com.noahrose.pocketlab.feature.bridge

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

class AtlasBridgeClient(
    baseUrl: String
) {

    private val normalizedBaseUrl =
        baseUrl
            .trim()
            .removeSuffix(
                "/"
            )

    private val client =
        HttpClient(OkHttp) {

            install(ContentNegotiation) {
                json(
                    Json {
                        ignoreUnknownKeys = true
                    }
                )
            }

            install(HttpTimeout) {
                connectTimeoutMillis = 2_000
                requestTimeoutMillis = 3_000
                socketTimeoutMillis = 3_000
            }
        }

    suspend fun connect(
        identity: CyberdeckIdentity
    ): BridgeConnectResponse =
        client.post(
            "$normalizedBaseUrl/connect"
        ) {
            contentType(
                ContentType.Application.Json
            )

            setBody(
                BridgeConnectRequest(
                    cyberdeckId =
                        identity.cyberdeckId,
                    deviceName =
                        identity.deviceName
                )
            )
        }.body()

    suspend fun heartbeat(
        cyberdeckId: String
    ): BridgeHeartbeatResponse =
        client.post(
            "$normalizedBaseUrl/heartbeat"
        ) {
            contentType(
                ContentType.Application.Json
            )

            setBody(
                BridgeHeartbeatRequest(
                    cyberdeckId =
                        cyberdeckId
                )
            )
        }.body()

    suspend fun disconnect(
        cyberdeckId: String
    ) {
        client.post(
            "$normalizedBaseUrl/disconnect"
        ) {
            contentType(
                ContentType.Application.Json
            )

            setBody(
                BridgeDisconnectRequest(
                    cyberdeckId =
                        cyberdeckId
                )
            )
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
data class BridgeHeartbeatRequest(
    val cyberdeckId: String
)

@Serializable
data class BridgeHeartbeatResponse(
    val status: String,
    val bridgeId: String,
    val cyberdeckId: String,
    val lastSeen: String?
)

@Serializable
data class BridgeDisconnectRequest(
    val cyberdeckId: String
)
