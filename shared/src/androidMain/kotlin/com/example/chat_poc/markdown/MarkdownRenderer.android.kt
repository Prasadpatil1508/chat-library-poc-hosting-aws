package com.example.chat_poc.markdown

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.mikepenz.markdown.m3.Markdown

actual object MarkdownRenderer {
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
        // No verticalScroll: used inside LazyColumn items; list scrolling handles overflow.
        Markdown(
            content = markdown,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
