package com.example.chat_poc.connect.api

import com.example.chat_poc.connect.model.ConnectionDetails
import com.example.chat_poc.util.ChatLibraryLog
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * Calls AWS Connect Participant Service CreateParticipantConnection directly.
 * Uses participant token (from start-chat) and region.
 *
 * Request: POST https://participant-connect.{region}.amazonaws.com/participant/connection
 * Header: X-Amz-Bearer: {participantToken}
 * Body: { "Type": ["WEBSOCKET", "CONNECTION_CREDENTIALS"] }
 */
class AwsParticipantConnectionApi(
    private val client: HttpClient,
    private val json: Json = Json { ignoreUnknownKeys = true; isLenient = true },
) : ParticipantConnectionApi {

    override suspend fun getConnection(region: String, participantToken: String): Result<ConnectionDetails> =
        runCatching {
            val baseUrl = "https://participant.connect.${region.trim()}.amazonaws.com"
            val url = "$baseUrl/participant/connection"
            ChatLibraryLog.d("AwsParticipantConnectionApi", "POST $url (CreateParticipantConnection)")
            val response = client.post {
                url(url)
                header("X-Amz-Bearer", participantToken)
                contentType(ContentType.Application.Json)
                setBody(mapOf("Type" to listOf("WEBSOCKET", "CONNECTION_CREDENTIALS")))
            }
            val raw = response.body<String>()
            ChatLibraryLog.d("AwsParticipantConnectionApi", "Response: ${raw.take(300)}...")
            val dto = json.decodeFromString<CreateParticipantConnectionResponse>(raw)
            val websocketUrl = dto.websocket?.url?.takeIf { it.isNotBlank() }
                ?: throw IllegalStateException("Response missing Websocket.Url")
            val connectionToken = dto.connectionCredentials?.connectionToken?.takeIf { it.isNotBlank() }
                ?: throw IllegalStateException("Response missing ConnectionCredentials.ConnectionToken")
            ConnectionDetails(websocketUrl = websocketUrl, connectionToken = connectionToken)
        }.onFailure { e ->
            ChatLibraryLog.e("AwsParticipantConnectionApi", "getConnection failed: ${e.message}")
        }

    @Serializable
    private data class CreateParticipantConnectionResponse(
        @SerialName("ConnectionCredentials") val connectionCredentials: ConnectionCredentialsDto? = null,
        @SerialName("Websocket") val websocket: WebsocketDto? = null,
    )

    @Serializable
    private data class ConnectionCredentialsDto(
        @SerialName("ConnectionToken") val connectionToken: String? = null,
        @SerialName("Expiry") val expiry: String? = null,
    )

    @Serializable
    private data class WebsocketDto(
        @SerialName("Url") val url: String? = null,
        @SerialName("ConnectionExpiry") val connectionExpiry: String? = null,
    )
}
