package com.example.chat_poc.connect.parser

import kotlinx.serialization.Serializable

/**
 * Layer 1 — WebSocket envelope. [content] is always a JSON string and must be parsed again.
 */
@Serializable
data class AwsSocketEnvelope(
    val topic: String,
    val contentType: String? = null,
    val content: String = "",
)

/**
 * Layer 2 — Amazon Connect chat payload (parsed from [AwsSocketEnvelope.content]).
 */
@Serializable
data class AwsChatPayload(
    val Type: String = "",
    val Content: String? = null,
    val ContentType: String? = null,
    val ParticipantRole: String? = null,
    val DisplayName: String? = null,
    val AbsoluteTime: String? = null,
    val ContactId: String? = null,
    val InitialContactId: String? = null,
)
