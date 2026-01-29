package com.example.chat_poc.markdown

import androidx.compose.runtime.Composable

/**
 * Expect/actual Markdown renderer. SDK-internal only; host app must not render Markdown.
 * Uses mikepenz/multiplatform-markdown-renderer on each platform.
 */
expect object MarkdownRenderer {
    @Composable
    fun Render(markdown: String)
}
