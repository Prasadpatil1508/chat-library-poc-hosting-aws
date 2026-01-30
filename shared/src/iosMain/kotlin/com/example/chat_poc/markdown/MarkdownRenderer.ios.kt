package com.example.chat_poc.markdown

import kotlinx.cinterop.ExperimentalForeignApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.interop.UIKitView
import platform.UIKit.NSLineBreakByWordWrapping
import platform.UIKit.UILabel

/**
 * Strip basic markdown to plain text for display in UILabel (no native markdown lib).
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
    @OptIn(ExperimentalForeignApi::class)
    @Composable
    actual fun Render(markdown: String) {
        if (markdown.trim().isEmpty()) {
            Text(
                text = "",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.fillMaxWidth(),
            )
            return
        }
        val plainText = stripMarkdownToPlainText(markdown)
        Column(modifier = Modifier.fillMaxWidth()) {
            UIKitView(
                factory = {
                    UILabel().apply {
                        setNumberOfLines(0)
                        lineBreakMode = NSLineBreakByWordWrapping
                    }
                },
                update = { label ->
                    label.text = plainText
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp)
            )
        }
    }
}
