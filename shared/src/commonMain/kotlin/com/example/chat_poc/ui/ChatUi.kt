package com.example.chat_poc.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.material3.LocalContentColor
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.chat_poc.connect.model.MessageDirection

/**
 * Chat UI constants and composables. Change these values to adjust bubble look and layout.
 * All chat bubble styling is centralized here for easy customization.
 */
object ChatUi {

    /** Corner radius of message bubbles. */
    val bubbleCornerRadius: Dp = 12.dp

    /** Horizontal padding inside a bubble. */
    val bubblePaddingHorizontal: Dp = 12.dp

    /** Vertical padding inside a bubble. */
    val bubblePaddingVertical: Dp = 10.dp

    /** Maximum bubble width as fraction of screen (0.0–1.0). */
    val bubbleMaxWidthFraction: Float = 0.75f

    /** Vertical spacing between messages. */
    val messageSpacing: Dp = 12.dp

    /** Padding around the quick-replies block (no bubble). */
    val quickReplyBlockPadding: Dp = 12.dp

    /** Vertical spacing between title and chips in quick replies. */
    val quickReplyTitleSpacing: Dp = 8.dp

    /** Horizontal/vertical spacing between quick reply chips. */
    val quickReplyChipSpacing: Dp = 8.dp

    /** Time to wait for WebSocket echo of sent message before showing "Failed to send" (ms). */
    val sendEchoTimeoutMs: Long = 15_000L
}

/**
 * Wraps chat message content in a bubble, aligned left (incoming/common), right (outgoing).
 * Uses [ChatUi] for dimensions; colors come from [MaterialTheme.colorScheme].
 */
@Composable
fun ChatBubble(
    direction: MessageDirection,
    modifier: Modifier = Modifier,
    label: String? = null,
    content: @Composable () -> Unit,
) {
    val alignment = when (direction) {
        MessageDirection.OUTGOING -> Alignment.CenterEnd
        MessageDirection.INCOMING -> Alignment.CenterStart
        MessageDirection.COMMON -> Alignment.CenterStart
    }
    val bubbleColor = when (direction) {
        MessageDirection.OUTGOING -> MaterialTheme.colorScheme.primary
        MessageDirection.INCOMING -> MaterialTheme.colorScheme.surfaceVariant
        MessageDirection.COMMON -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f)
    }
    val contentColor = when (direction) {
        MessageDirection.OUTGOING -> MaterialTheme.colorScheme.onPrimary
        MessageDirection.INCOMING -> MaterialTheme.colorScheme.onSurfaceVariant
        MessageDirection.COMMON -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = alignment,
    ) {
        Surface(
            shape = RoundedCornerShape(ChatUi.bubbleCornerRadius),
            color = bubbleColor,
            modifier = Modifier.fillMaxWidth(ChatUi.bubbleMaxWidthFraction),
        ) {
            Column(
                modifier = Modifier.padding(
                    horizontal = ChatUi.bubblePaddingHorizontal,
                    vertical = ChatUi.bubblePaddingVertical,
                ),
            ) {
                if (!label.isNullOrBlank()) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall,
                        color = contentColor,
                        modifier = Modifier.padding(bottom = 4.dp),
                    )
                }
                CompositionLocalProvider(LocalContentColor provides contentColor) {
                    content()
                }
            }
        }
    }
}
