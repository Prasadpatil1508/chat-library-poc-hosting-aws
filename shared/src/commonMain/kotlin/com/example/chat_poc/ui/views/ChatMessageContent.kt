package com.example.chat_poc.ui.views

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AssistChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.chat_poc.connect.model.ChatMessage
import com.example.chat_poc.ui.ChatUi
import com.example.chat_poc.markdown.MarkdownRenderer

/**
 * Renders a chat message (plain text, Markdown, quick replies, or flight status). Markdown is rendered inside the SDK via
 * [MarkdownRenderer]. When [msg.quickReplies] is non-null, shows a title and clickable options; [onQuickReplyClick]
 * is invoked with the option's value when the user taps an option (caller should send that value to the server).
 * When [msg.flightStatusPayload] is non-null, shows a custom flight-status card; [onFlightActionClick] is invoked
 * with the action href when the user taps an action (e.g. host can open the URL).
 */
@Composable
fun ChatMessageContent(
    msg: ChatMessage,
    modifier: Modifier = Modifier,
    onQuickReplyClick: ((value: String) -> Unit)? = null,
    onFlightActionClick: ((href: String) -> Unit)? = null,
) {
    when {
        msg.sendFailed -> {
            Text(
                text = msg.text,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error,
                modifier = modifier.fillMaxWidth(),
            )
        }
        msg.flightStatusPayload != null -> {
            FlightStatusContent(
                payload = msg.flightStatusPayload,
                modifier = modifier,
                onActionClick = onFlightActionClick,
            )
        }
        msg.quickReplies != null && msg.quickReplies.isNotEmpty() -> {
            Column(modifier = modifier.fillMaxWidth()) {
                if (msg.text.isNotBlank()) {
                    Text(
                        text = msg.text,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(bottom = ChatUi.quickReplyTitleSpacing),
                    )
                }
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(ChatUi.quickReplyChipSpacing),
                    verticalArrangement = Arrangement.spacedBy(ChatUi.quickReplyChipSpacing),
                ) {
                    for (option in msg.quickReplies) {
                        val label = option.label.ifBlank { option.value }
                        AssistChip(
                            onClick = { onQuickReplyClick?.invoke(option.value) },
                            label = { Text(label) },
                        )
                    }
                }
            }
        }
        msg.isMarkdown && msg.text.trim().isNotEmpty() -> {
            MarkdownRenderer.Render(markdown = msg.text)
        }
        else -> {
            Text(
                text = msg.text,
                style = MaterialTheme.typography.bodyMedium,
                modifier = modifier.fillMaxWidth(),
            )
        }
    }
}
