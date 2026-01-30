package com.example.chat_poc.connect.model

import kotlinx.serialization.Serializable

/**
 * Layer 3 — Bot message (parsed from [AwsChatPayload.Content] when Type == "MESSAGE" and ParticipantRole == "CUSTOM_BOT").
 */
@Serializable
data class BotMessage(
    val messageType: String = "",
    val payload: BotPayload? = null,
)

@Serializable
data class BotPayload(
    val text: String? = null,
    val title: String? = null,
    val options: List<QuickReply>? = null,
)

@Serializable
data class QuickReply(
    val id: String = "",
    val label: String = "",
    val value: String = "",
)

/** FLIGHT_STATUS bot payload (nested in CUSTOM_BOT Content when messageType == "FLIGHT_STATUS"). */
@Serializable
data class FlightStatusPayload(
    val airline: String = "",
    val flightNumber: String = "",
    val dateLocal: String = "",
    val departure: FlightLeg? = null,
    val arrival: FlightLeg? = null,
    val status: FlightStatus? = null,
    val actions: List<FlightAction>? = null,
)

@Serializable
data class FlightLeg(
    val airport: String = "",
    val city: String = "",
    val sched: String = "",
    val est: String = "",
    val gate: String = "",
    val terminal: String = "",
)

@Serializable
data class FlightStatus(
    val code: String = "",
    val delayMinutes: Int = 0,
    val reason: String = "",
)

@Serializable
data class FlightAction(
    val type: String = "",
    val label: String = "",
    val href: String = "",
)
