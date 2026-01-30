package com.example.chat_poc.markdown

import androidx.compose.runtime.Composable

/**
 * Expect/actual Markdown renderer. SDK-internal only; host app must not render Markdown.
 * Android: Markwon (native TextView + tables). iOS: UILabel + plain text (markdown stripped).
 */
expect object MarkdownRenderer {
    @Composable
    fun Render(markdown: String)
}
