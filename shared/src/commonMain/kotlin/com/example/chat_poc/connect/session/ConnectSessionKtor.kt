package com.example.chat_poc.connect.session

import com.example.chat_poc.api.createHttpClient
import com.example.chat_poc.config.ConnectConfig
import com.example.chat_poc.connect.api.AwsParticipantConnectionApi
import com.example.chat_poc.connect.api.ParticipantConnectionApi
import com.example.chat_poc.connect.model.ChatItem
import com.example.chat_poc.connect.model.ChatMessage
import com.example.chat_poc.connect.model.ConnectChatDetails
import com.example.chat_poc.connect.model.MessageDirection
import com.example.chat_poc.connect.model.Sender
import com.example.chat_poc.connect.parser.DefaultJson
import com.example.chat_poc.connect.parser.parseWebSocketMessage
import com.example.chat_poc.util.ChatLibraryLog
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

/**
 * Shared [ConnectChatSession] implementation using AWS Connect Participant Service directly.
 */
class ConnectSessionKtor(
    private val config: ConnectConfig,
    private val client: HttpClient = createHttpClient(),
    private val connectionApi: ParticipantConnectionApi = AwsParticipantConnectionApi(client),
    private val json: Json = DefaultJson,
) : ConnectChatSession {

    override var onConnectionEstablished: (() -> Unit)? = null
    override var onConnectionBroken: ((Throwable?) -> Unit)? = null
    override var onMessageReceived: ((ChatMessage) -> Unit)? = null
    override var onChatItemReceived: ((ChatItem) -> Unit)? = null
    override var onTranscriptUpdated: ((List<ChatMessage>) -> Unit)? = null

    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var connectionToken: String? = null
    private var participantRegion: String? = null
    private var webSocketJob: kotlinx.coroutines.Job? = null

    override suspend fun connect(details: ConnectChatDetails): Result<Unit> = runCatching {
        require(config.isValid()) { "ConnectConfig (region, etc.) is required for ConnectSessionKtor" }
        ChatLibraryLog.d("ConnectSessionKtor", "connect: CreateParticipantConnection region=${config.region}...")
        val conn = connectionApi.getConnection(config.region, details.participantToken).getOrThrow()
        connectionToken = conn.connectionToken
        participantRegion = config.region
        ChatLibraryLog.d("ConnectSessionKtor", "connect: websocketUrl=${conn.websocketUrl.take(60)}...")
        webSocketJob = scope.launch {
            runWebSocket(conn.websocketUrl)
        }
        onConnectionEstablished?.invoke()
        Unit
    }.onFailure { e ->
        ChatLibraryLog.e("ConnectSessionKtor", "connect failed: ${e.message}")
        onConnectionBroken?.invoke(e)
    }

    private suspend fun runWebSocket(websocketUrl: String) {
        try {
            client.webSocket(websocketUrl) {
                val subscribe = buildJsonObject {
                    put("topic", "aws/subscribe")
                    put("content", buildJsonObject {
                        put("topics", buildJsonArray { add(JsonPrimitive("aws/chat")) })
                    })
                }
                send(Frame.Text(subscribe.toString()))
                ChatLibraryLog.d("ConnectSessionKtor", "WebSocket: subscribed to aws/chat")
                for (frame in incoming) {
                    if (frame is Frame.Text) {
                        val text = frame.readText()
                        val dump = if (text.length <= 1500) text else "${text.take(1500)}...[${text.length} chars total]"
                        ChatLibraryLog.d("ConnectSessionKtor", "WebSocket raw: $dump")
                        parseAndEmitMessage(text)
                    }
                }
            }
        } catch (e: Exception) {
            if (scope.isActive) {
                ChatLibraryLog.e("ConnectSessionKtor", "WebSocket error: ${e.message}")
                onConnectionBroken?.invoke(e)
            }
        }
    }

    private fun parseAndEmitMessage(raw: String) {
        val items = parseWebSocketMessage(raw, json)
        for (item in items) {
            onChatItemReceived?.invoke(item)
            val msg = chatItemToChatMessage(item)
            onMessageReceived?.invoke(msg)
        }
    }

    private fun chatItemToChatMessage(item: ChatItem): ChatMessage = when (item) {
        is ChatItem.Text -> ChatMessage(
            id = "",
            text = item.text,
            participantId = null,
            displayName = when (item.sender) {
                Sender.CUSTOMER -> "Customer"
                Sender.BOT -> "AI Assistant"
                Sender.SYSTEM -> "SYSTEM"
            },
            timestamp = "",
            direction = when (item.sender) {
                Sender.CUSTOMER -> MessageDirection.OUTGOING
                else -> MessageDirection.INCOMING
            },
        )
        is ChatItem.Markdown -> ChatMessage(
            id = "",
            text = item.text,
            participantId = null,
            displayName = "AI Assistant",
            timestamp = "",
            direction = MessageDirection.INCOMING,
            isMarkdown = true,
        )
        is ChatItem.QuickReplies -> ChatMessage(
            id = "",
            text = item.title ?: "",
            participantId = null,
            displayName = "AI Assistant",
            timestamp = "",
            direction = MessageDirection.INCOMING,
            quickReplies = item.options,
        )
        is ChatItem.FlightStatus -> ChatMessage(
            id = "",
            text = "",
            participantId = null,
            displayName = "AI Assistant",
            timestamp = "",
            direction = MessageDirection.INCOMING,
            flightStatusPayload = item.payload,
        )
        is ChatItem.System -> ChatMessage(
            id = "",
            text = item.text,
            participantId = null,
            displayName = "SYSTEM_MESSAGE",
            timestamp = "",
            direction = MessageDirection.COMMON,
        )
        is ChatItem.ChatEnded -> ChatMessage(
            id = "",
            text = "Chat ended",
            participantId = null,
            displayName = null,
            timestamp = "",
            direction = MessageDirection.COMMON,
        )
    }

    override suspend fun sendMessage(text: String): Result<Unit> = runCatching {
        val token = connectionToken
        val region = participantRegion
        require(token != null && region != null) { "Not connected" }
        val sendUrl = "https://participant.connect.${region.trim()}.amazonaws.com/participant/message"
        ChatLibraryLog.d("ConnectSessionKtor", "sendMessage: POST $sendUrl")
        client.post {
            url(sendUrl)
            header("X-Amz-Bearer", token)
            contentType(ContentType.Application.Json)
            setBody(mapOf("Content" to text, "ContentType" to "text/plain"))
        }.body<String>()
        Unit
    }.onFailure { e ->
        ChatLibraryLog.e("ConnectSessionKtor", "sendMessage failed: ${e.message}")
    }

    override suspend fun disconnect(): Result<Unit> = runCatching {
        webSocketJob?.cancel()
        webSocketJob = null
        connectionToken = null
        participantRegion = null
        ChatLibraryLog.d("ConnectSessionKtor", "disconnect: done")
    }
}
