package com.example.chat_poc.connect.session

import com.example.chat_poc.connect.model.ChatItem
import com.example.chat_poc.connect.model.ChatMessage
import com.example.chat_poc.connect.model.ConnectChatDetails

/**
 * Abstraction for an AWS Connect chat session.
 * Implementations: platform SDK (Android) or shared Ktor (Android/iOS).
 *
 * Flow: [connect] with [ConnectChatDetails] from start-chat → [onConnectionEstablished] → [sendMessage] / [onMessageReceived] → [disconnect].
 */
interface ConnectChatSession {

    suspend fun connect(details: ConnectChatDetails): Result<Unit>

    suspend fun disconnect(): Result<Unit>

    suspend fun sendMessage(text: String): Result<Unit>

    var onConnectionEstablished: (() -> Unit)?
    var onConnectionBroken: ((Throwable?) -> Unit)?
    var onMessageReceived: ((ChatMessage) -> Unit)?
    var onChatItemReceived: ((ChatItem) -> Unit)?
    var onTranscriptUpdated: ((List<ChatMessage>) -> Unit)?
}
