package com.example.chat_poc.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Request body for the start-chat API (get token for AWS Connect).
 */
@Serializable
data class StartChatRequest(
    @SerialName("contactFlowId") val contactFlowId: String,
    @SerialName("instanceId") val instanceId: String,
    @SerialName("region") val region: String,
)
