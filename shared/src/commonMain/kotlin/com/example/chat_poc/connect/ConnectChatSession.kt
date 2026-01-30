package com.example.chat_poc.connect

/**
 * Abstraction for an AWS Connect chat session (SOLID: interface segregation, dependency inversion).
 * Implementations: platform SDK (Android) or shared Ktor (Android/iOS) using backend connection endpoint.
 *
 * Flow: [connect] with [ConnectChatDetails] from start-chat → [onConnectionEstablished] → [sendMessage] / [onMessageReceived] → [disconnect].
 *
 * @see [mobileChatExamples](https://github.com/amazon-connect/amazon-connect-chat-ui-examples/tree/master/mobileChatExamples)
 */
interface ConnectChatSession {

    /**
     * Connect to the chat using start-chat details (participant token, optional contactId/participantId).
     * Callbacks (e.g. [onConnectionEstablished]) are invoked on connection success/failure.
     */
    suspend fun connect(details: ConnectChatDetails): Result<Unit>

    /** Disconnect and release resources. */
    suspend fun disconnect(): Result<Unit>

    /** Send a plain-text message. Connection must be established. */
    suspend fun sendMessage(text: String): Result<Unit>

    /** Callback when WebSocket/session is established. */
    var onConnectionEstablished: (() -> Unit)?

    /** Callback when connection is lost or closed. */
    var onConnectionBroken: ((Throwable?) -> Unit)?

    /** Callback when a new chat message is received (e.g. from agent). */
    var onMessageReceived: ((ChatMessage) -> Unit)?

    /** Callback when a parsed [ChatItem] is received (e.g. for SwiftUI/UI that renders by item type). */
    var onChatItemReceived: ((ChatItem) -> Unit)?

    /** Callback when the full transcript is updated (e.g. after getTranscript). */
    var onTranscriptUpdated: ((List<ChatMessage>) -> Unit)?
}

/**
 * Simple message model for library callbacks (avoids leaking SDK-specific types into commonMain).
 * When [isMarkdown] is true, platform UI should render [text] with a Markdown renderer.
 * When [quickReplies] is non-null, UI should show [text] as title and option buttons; tapping an option sends [QuickReply.value] to the server.
 * When [flightStatusPayload] is non-null, UI should render the flight status with a custom flight-status widget.
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
)

enum class MessageDirection { INCOMING, OUTGOING, COMMON }
