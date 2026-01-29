package com.example.chat_poc.api

import com.example.chat_poc.config.ConnectConfig
import com.example.chat_poc.model.StartChatApiResponseDto
import com.example.chat_poc.model.StartChatRequest
import com.example.chat_poc.model.StartChatResponse
import com.example.chat_poc.util.ChatLibraryLog
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.isSuccess
import io.ktor.http.contentType
import kotlinx.serialization.json.Json

/**
 * Start-chat API implementation using Ktor (commonMain).
 * Uses [createHttpClient] for the engine (OkHttp/Darwin provided by platform).
 * Logs raw response/error body for debugging API structure.
 */
class KtorStartChatApi(
    private val client: HttpClient = createHttpClient()
) : StartChatApi {

    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    override suspend fun fetchToken(config: ConnectConfig): Result<StartChatResponse> =
        runCatching {
            require(config.isValid()) { "ConnectConfig must have all non-blank fields" }
            val url = config.apiGatewayUrl.trimEnd('/')
            ChatLibraryLog.d("StartChatApi", "POST $url (contactFlowId=${config.contactFlowId.take(8)}..., instanceId=${config.instanceId.take(8)}...)")
            val request = StartChatRequest(
                contactFlowId = config.contactFlowId,
                instanceId = config.instanceId,
                region = config.region
            )
            val response = client.post(url) {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
            val status = response.status
            val rawBody = response.body<String>()
            // Dump full response for debugging API structure
            ChatLibraryLog.d("StartChatApi", "HTTP ${status.value} $status")
            ChatLibraryLog.d("StartChatApi", "Raw response body: $rawBody")
            if (!status.isSuccess()) {
                ChatLibraryLog.e("StartChatApi", "API error: $status — $rawBody")
                return@runCatching null
            }
            val dto = json.decodeFromString<StartChatApiResponseDto>(rawBody)
            val result = dto.data?.startChatResult
            val token = result?.participantToken
            val parsed = StartChatResponse(
                participantTokenCapitalP = token,
                contactId = result?.contactId,
                participantId = result?.participantId,
            )
            ChatLibraryLog.d("StartChatApi", "Parsed: token present=${token != null}, contactId=${result?.contactId != null}, participantId=${result?.participantId != null}")
            parsed
        }.mapCatching { parsed ->
            parsed ?: throw IllegalStateException("Request returned non-success status (see logs for raw body)")
        }.onFailure { e ->
            ChatLibraryLog.e("StartChatApi", "Request failed: ${e.message}")
            // If Ktor threw with a response (e.g. 4xx), try to log it
            val withResponse = e.message ?: ""
            if (withResponse.isNotEmpty()) ChatLibraryLog.e("StartChatApi", "Error detail: $withResponse")
        }
}
