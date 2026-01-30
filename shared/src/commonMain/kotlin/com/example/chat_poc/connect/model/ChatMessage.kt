package com.example.chat_poc.connect.model

/**
 * Simple message model for library callbacks (avoids leaking SDK-specific types into commonMain).
 * When [isMarkdown] is true, platform UI should render [text] with a Markdown renderer.
 * When [quickReplies] is non-null, UI should show [text] as title and option buttons.
 * When [flightStatusPayload] is non-null, UI should render the flight status with a custom widget.
 */
data class ChatMessage(
    val id: String,
    val text: String,
    val participantId: String?,
    val displayName: String?,
    val timestamp: String,
    val direction: MessageDirection = MessageDirection.COMMON,
    val isMarkdown: Boolean = false,
    val quickReplies: List<QuickReply>? = null,
    val flightStatusPayload: FlightStatusPayload? = null,
    /** True when sent locally but not yet confirmed by WebSocket echo. */
    val isPending: Boolean = false,
    /** True when send was attempted but no echo received (show "Failed to send"). */
    val sendFailed: Boolean = false,
)

enum class MessageDirection { INCOMING, OUTGOING, COMMON }
