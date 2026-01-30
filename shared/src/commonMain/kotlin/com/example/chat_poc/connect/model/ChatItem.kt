package com.example.chat_poc.connect.model

/**
 * Domain model for parsed WebSocket messages. Platform-neutral; no AWS or platform APIs.
 */
sealed class ChatItem {
    data class Text(val text: String, val sender: Sender) : ChatItem()
    data class Markdown(val text: String) : ChatItem()
    data class QuickReplies(val title: String?, val options: List<QuickReply>) : ChatItem()
    data class FlightStatus(val payload: FlightStatusPayload) : ChatItem()
    data class System(val text: String) : ChatItem()
    object ChatEnded : ChatItem()
}

enum class Sender {
    CUSTOMER,
    BOT,
    SYSTEM,
}
