package com.example.chat_poc.domain

import com.example.chat_poc.api.StartChatApi
import com.example.chat_poc.api.KtorStartChatApi
import com.example.chat_poc.config.ConnectConfig
import com.example.chat_poc.util.ChatLibraryLog

/**
 * Use case: fetch start-chat token for AWS Connect (phase 1: API only).
 * Depends on [StartChatApi] (default: [KtorStartChatApi]); host can inject a different implementation.
 */
class FetchConnectTokenUseCase(
    private val api: StartChatApi = KtorStartChatApi()
) {
    suspend operator fun invoke(config: ConnectConfig): Result<String> {
        ChatLibraryLog.d("UseCase", "fetchToken: apiGateway=${config.apiGatewayUrl.take(50)}..., instanceId=${config.instanceId}")
        return api.fetchToken(config).mapCatching { response ->
            val token = response.effectiveToken()
            if (token == null || token.isBlank()) {
                ChatLibraryLog.e(
                    "UseCase",
                    "Start-chat response had no token (participantToken=${response.participantToken != null}, ParticipantToken=${response.participantTokenCapitalP != null}, token=${response.token != null}). " +
                        "Check API response JSON; expected keys: participantToken, ParticipantToken, or token."
                )
                throw IllegalStateException(
                    "Start-chat response had no token. API may use different JSON keys; check logs and StartChatResponse.kt."
                )
            } else {
                ChatLibraryLog.d("UseCase", "fetchToken: got token (length=${token.length})")
                token
            }
        }
    }
}
