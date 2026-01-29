package com.example.chat_poc.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Raw API response shape: { "data": { "startChatResult": { "ParticipantToken": "...", ... } } }.
 * Maps to [StartChatResponse] for the rest of the app.
 */
@Serializable
data class StartChatApiResponseDto(
    @SerialName("data") val data: StartChatDataDto? = null,
)

@Serializable
data class StartChatDataDto(
    @SerialName("startChatResult") val startChatResult: StartChatResultDto? = null,
)

@Serializable
data class StartChatResultDto(
    @SerialName("ParticipantToken") val participantToken: String? = null,
    @SerialName("ContactId") val contactId: String? = null,
    @SerialName("ParticipantId") val participantId: String? = null,
)
