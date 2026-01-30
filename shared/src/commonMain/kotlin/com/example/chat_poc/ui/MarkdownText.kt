package com.example.chat_poc.ui

import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import com.example.chat_poc.util.UrlOpener

/**
 * Simple markdown-style text (bold and links) aligned with
 * [amazon-connect-chat-ui-examples CommonUtils.MarkdownText](https://github.com/amazon-connect/amazon-connect-chat-ui-examples/blob/master/mobileChatExamples/androidChatExample/app/src/main/java/com/blitz/androidchatexample/utils/CommonUtils.kt).
 * Supports **bold** and [text](url). Tapping a link opens [url] via [UrlOpener].
 */
@Composable
fun MarkdownText(
    text: String,
    color: Color = MaterialTheme.colorScheme.onSurface,
    onLinkClick: ((url: String) -> Unit)? = { UrlOpener.openUrl(it) },
) {
    val annotatedString = buildAnnotatedString {
        val regex = """\*\*(.*?)\*\*|\[(.*?)\]\((.*?)\)""".toRegex()
        var currentIndex = 0
        regex.findAll(text).forEach { matchResult ->
            val (beforeMatchIndex, matchEnd) = matchResult.range.first to matchResult.range.last + 1
            append(text.substring(currentIndex, beforeMatchIndex))
            val (boldText, linkText, linkUrl) = matchResult.destructured
            when {
                boldText.isNotEmpty() -> {
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = color)) {
                        append(boldText)
                    }
                }
                linkUrl.isNotEmpty() -> {
                    pushStringAnnotation(tag = "URL", annotation = linkUrl)
                    withStyle(
                        SpanStyle(
                            color = MaterialTheme.colorScheme.primary,
                            textDecoration = TextDecoration.Underline,
                        )
                    ) {
                        append(linkText.ifEmpty { linkUrl })
                    }
                    pop()
                }
            }
            currentIndex = matchEnd
        }
        append(text.substring(currentIndex, text.length))
    }
    ClickableText(
        text = annotatedString,
        style = MaterialTheme.typography.bodyMedium.copy(color = color),
        onClick = { offset ->
            annotatedString.getStringAnnotations(tag = "URL", start = offset, end = offset)
                .firstOrNull()?.let { annotation ->
                    onLinkClick?.invoke(annotation.item)
                }
        },
    )
}
