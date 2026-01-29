package com.example.chat_poc.connect

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
                listOf(ChatItem.Markdown(formatFlightStatusAsMarkdown(flight)))
            }
            else -> listOf(ChatItem.Text(content, Sender.BOT))
        }
    } catch (_: Exception) {
        listOf(ChatItem.Text(content, Sender.BOT))
    }
}

private fun formatFlightStatusAsMarkdown(f: FlightStatusPayload): String = buildString {
    val airline = f.airline.ifBlank { "Flight" }
    append("**$airline ${f.flightNumber}**")
    if (f.dateLocal.isNotBlank()) append(" · $f.dateLocal")
    append("\n\n")
    f.departure?.let { d ->
        append("**Departure** ${d.airport.ifBlank { "—" }} ${d.city}\n")
        if (d.sched.isNotBlank()) append("- Scheduled: ${d.sched}\n")
        if (d.est.isNotBlank() && d.est != d.sched) append("- Estimated: ${d.est}\n")
        if (d.gate.isNotBlank() || d.terminal.isNotBlank()) append("- Gate ${d.gate} · Terminal ${d.terminal}\n")
        append("\n")
    }
    f.arrival?.let { a ->
        append("**Arrival** ${a.airport.ifBlank { "—" }} ${a.city}\n")
        if (a.sched.isNotBlank()) append("- Scheduled: ${a.sched}\n")
        if (a.est.isNotBlank() && a.est != a.sched) append("- Estimated: ${a.est}\n")
        if (a.gate.isNotBlank() || a.terminal.isNotBlank()) append("- Gate ${a.gate} · Terminal ${a.terminal}\n")
        append("\n")
    }
    f.status?.let { s ->
        if (s.code.isNotBlank()) {
            append("**Status:** ${s.code}")
            if (s.delayMinutes > 0) append(" (${s.delayMinutes} min delay)")
            if (s.reason.isNotBlank()) append(" — ${s.reason}")
            append("\n\n")
        }
    }
    f.actions?.filter { it.href.isNotBlank() && it.label.isNotBlank() }?.forEach { a ->
        append("[${a.label}](${a.href})\n")
    }
}
