package com.example.chat_poc.connect.parser

import com.example.chat_poc.connect.model.BotMessage
import com.example.chat_poc.connect.model.ChatItem
import com.example.chat_poc.connect.model.FlightStatusPayload
import com.example.chat_poc.connect.model.Sender
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

val DefaultJson = Json {
    ignoreUnknownKeys = true
    isLenient = true
}

/**
 * Parses a single WebSocket frame into zero or one [ChatItem].
 * Pure function; never throws. Returns empty list on invalid JSON or when topic != "aws/chat".
 */
fun parseWebSocketMessage(
    raw: String,
    json: Json = DefaultJson,
): List<ChatItem> {
    if (raw.isBlank()) return emptyList()
    return try {
        val envelope = json.decodeFromString<AwsSocketEnvelope>(raw)
        if (envelope.topic != "aws/chat") return emptyList()
        val contentStr = envelope.content
        if (contentStr.isBlank()) return emptyList()
        val payload = json.decodeFromString<AwsChatPayload>(contentStr)
        when (payload.Type) {
            "EVENT" -> parseEvent(payload)
            "MESSAGE" -> parseMessage(payload, json)
            else -> emptyList()
        }
    } catch (_: Exception) {
        emptyList()
    }
}

private fun parseEvent(payload: AwsChatPayload): List<ChatItem> {
    val contentType = payload.ContentType ?: ""
    return when {
        contentType.contains("chat.ended") -> listOf(ChatItem.ChatEnded)
        else -> emptyList()
    }
}

private fun parseMessage(payload: AwsChatPayload, json: Json): List<ChatItem> {
    val content = payload.Content ?: ""
    val role = payload.ParticipantRole ?: ""
    return when (role) {
        "CUSTOMER" -> listOf(ChatItem.Text(content, Sender.CUSTOMER))
        "SYSTEM" -> listOf(ChatItem.System(content))
        "CUSTOM_BOT" -> parseBotMessage(content, json)
        else -> emptyList()
    }
}

private fun parseBotMessage(content: String, json: Json): List<ChatItem> {
    if (content.isBlank()) return emptyList()
    if (!content.trimStart().startsWith("{")) return listOf(ChatItem.Text(content, Sender.BOT))
    return try {
        val obj = json.parseToJsonElement(content).jsonObject
        val messageType = obj["messageType"]?.jsonPrimitive?.content?.trim('"') ?: ""
        when (messageType) {
            "markdown" -> {
                val bot = json.decodeFromString<BotMessage>(content)
                val text = bot.payload?.text ?: ""
                listOf(ChatItem.Markdown(text))
            }
            "QUICK_REPLIES" -> {
                val bot = json.decodeFromString<BotMessage>(content)
                val pl = bot.payload ?: return listOf(ChatItem.Text(content, Sender.BOT))
                listOf(ChatItem.QuickReplies(pl.title, pl.options ?: emptyList()))
            }
            "FLIGHT_STATUS" -> {
                val payloadObj = obj["payload"] ?: return listOf(ChatItem.Text(content, Sender.BOT))
                val flight = json.decodeFromString<FlightStatusPayload>(payloadObj.toString())
                listOf(ChatItem.FlightStatus(flight))
            }
            else -> listOf(ChatItem.Text(content, Sender.BOT))
        }
    } catch (_: Exception) {
        listOf(ChatItem.Text(content, Sender.BOT))
    }
}
