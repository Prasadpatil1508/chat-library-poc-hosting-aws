package com.example.chat_poc.markdown

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Strip basic markdown to plain text (no native markdown lib on iOS).
 * Removes **, *, ##, #, list markers, and table pipes for readability.
 */
private fun stripMarkdownToPlainText(markdown: String): String = buildString {
    var i = 0
    val s = markdown
    while (i < s.length) {
        when {
            s.startsWith("**", i) -> i += 2
            s.startsWith("*", i) && (i == 0 || !s[i - 1].isLetterOrDigit()) -> i += 1
            s.startsWith("## ", i) -> i += 3
            s.startsWith("# ", i) -> i += 2
            s.startsWith("- ", i) -> i += 2
            s.startsWith("| ", i) -> i += 2
            s[i] == '|' && i + 1 < s.length && s[i + 1] == ' ' -> i += 2
            s[i] == '|' -> { append(' '); i += 1 }
            else -> { append(s[i]); i += 1 }
        }
    }
}.replace(Regex("  +"), " ").trim()

actual object MarkdownRenderer {
    @Composable
    actual fun Render(markdown: String) {
        val plainText = if (markdown.trim().isEmpty()) "" else stripMarkdownToPlainText(markdown)
        Text(
            text = plainText,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
        )
    }
}
