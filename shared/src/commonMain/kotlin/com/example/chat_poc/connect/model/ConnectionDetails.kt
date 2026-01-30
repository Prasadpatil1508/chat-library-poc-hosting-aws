package com.example.chat_poc.connect.model

/**
 * Result of CreateParticipantConnection (or backend proxy).
 * Used to establish WebSocket and call SendMessage.
 */
data class ConnectionDetails(
    val websocketUrl: String,
    val connectionToken: String,
)
