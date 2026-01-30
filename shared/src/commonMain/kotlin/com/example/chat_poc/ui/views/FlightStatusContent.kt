package com.example.chat_poc.ui.views

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.chat_poc.connect.model.FlightLeg
import com.example.chat_poc.connect.model.FlightStatusPayload
import com.example.chat_poc.connect.model.FlightStatus as FlightStatusModel

/**
 * Renders a flight status message (messageType == "FLIGHT_STATUS") as a custom card widget.
 * Used for both Android and iOS from common Compose.
 *
 * @param payload Parsed flight status from the bot message
 * @param onActionClick Optional callback when user taps an action link (e.g. "Track Flight"); host can open [href]
 */
@Composable
fun FlightStatusContent(
    payload: FlightStatusPayload,
    modifier: Modifier = Modifier,
    onActionClick: ((href: String) -> Unit)? = null,
) {
    val airline = payload.airline.ifBlank { "Flight" }
    val flightLabel = "$airline ${payload.flightNumber}".trim()
    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = surfaceVariant),
        shape = CardDefaults.shape,
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: airline + flight number, date
            Text(
                text = flightLabel,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            if (payload.dateLocal.isNotBlank()) {
                Text(
                    text = payload.dateLocal,
                    style = MaterialTheme.typography.bodySmall,
                    color = onSurfaceVariant,
                    modifier = Modifier.padding(top = 2.dp),
                )
            }

            payload.departure?.let { leg ->
                Spacer(modifier = Modifier.height(12.dp))
                LegRow(label = "Departure", leg = leg)
            }
            payload.arrival?.let { leg ->
                Spacer(modifier = Modifier.height(8.dp))
                LegRow(label = "Arrival", leg = leg)
            }
            payload.status?.let { status ->
                if (status.code.isNotBlank()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    StatusRow(status = status)
                }
            }
            val actions = payload.actions?.filter { it.href.isNotBlank() && it.label.isNotBlank() }
            if (!actions.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    for (action in actions) {
                        OutlinedButton(
                            onClick = { onActionClick?.invoke(action.href) },
                        ) {
                            Text(action.label)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LegRow(label: String, leg: FlightLeg) {
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = onSurfaceVariant,
        )
        Row(
            modifier = Modifier.padding(top = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            val airport = leg.airport.ifBlank { "—" }
            Text(
                text = "$airport ${leg.city}".trim(),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
        if (leg.sched.isNotBlank()) {
            Text(
                text = "Scheduled: ${formatTime(leg.sched)}",
                style = MaterialTheme.typography.bodySmall,
                color = onSurfaceVariant,
                modifier = Modifier.padding(top = 2.dp),
            )
        }
        if (leg.est.isNotBlank() && leg.est != leg.sched) {
            Text(
                text = "Estimated: ${formatTime(leg.est)}",
                style = MaterialTheme.typography.bodySmall,
                color = onSurfaceVariant,
                modifier = Modifier.padding(top = 2.dp),
            )
        }
        if (leg.gate.isNotBlank() || leg.terminal.isNotBlank()) {
            Text(
                text = "Gate ${leg.gate} · Terminal ${leg.terminal}".trim(),
                style = MaterialTheme.typography.bodySmall,
                color = onSurfaceVariant,
                modifier = Modifier.padding(top = 2.dp),
            )
        }
    }
}

@Composable
private fun StatusRow(status: FlightStatusModel) {
    val statusText = buildString {
        append(status.code)
        if (status.delayMinutes > 0) append(" (${status.delayMinutes} min delay)")
        if (status.reason.isNotBlank()) append(" — ${status.reason}")
    }
    Text(
        text = "Status: $statusText",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurface,
    )
}

/** Extracts time part from ISO datetime e.g. "2025-10-17T14:05-07:00" -> "14:05". */
private fun formatTime(iso: String): String {
    val t = iso.indexOf('T')
    if (t < 0) return iso
    val end = when {
        iso.indexOf('-', t + 1).takeIf { it > t } != null -> iso.indexOf('-', t + 1)
        iso.indexOf('+', t + 1).takeIf { it > t } != null -> iso.indexOf('+', t + 1)
        else -> iso.length
    }
    return iso.substring(t + 1, end).take(5) // "14:05"
}
