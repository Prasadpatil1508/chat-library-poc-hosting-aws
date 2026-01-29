package com.example.chat_poc.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Response from the start-chat API (token for AWS Connect session).
 * Supports both "participantToken" and "ParticipantToken" (some APIs use capital P).
 * Includes contactId and participantId for [ConnectChatDetails] / ChatSession.connect().
 */
@Serializable
data class StartChatResponse(
    @SerialName("participantToken") val participantToken: String? = null,
    @SerialName("ParticipantToken") val participantTokenCapitalP: String? = null,
    @SerialName("token") val token: String? = null,
    @SerialName("sessionId") val sessionId: String? = null,
    @SerialName("contactId") val contactId: String? = null,
    @SerialName("participantId") val participantId: String? = null,
) {
    /** Resolved token: participantToken (either casing) or token. Use this to avoid JVM clash with property getter. */
    fun effectiveToken(): String? = participantToken ?: participantTokenCapitalP ?: token
}
