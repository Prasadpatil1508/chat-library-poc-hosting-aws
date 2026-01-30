package com.example.chat_poc.markdown

import android.graphics.Color
import android.widget.TextView
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import io.noties.markwon.AbstractMarkwonPlugin
import io.noties.markwon.Markwon
import io.noties.markwon.ext.tables.TablePlugin
import io.noties.markwon.ext.tables.TableTheme
import io.noties.markwon.core.MarkwonTheme

actual object MarkdownRenderer {
    @Composable
    actual fun Render(markdown: String) {
        if (markdown.trim().isEmpty()) return
        val context = LocalContext.current
        val textColor = MaterialTheme.colorScheme.onSurface
        val linkColor = MaterialTheme.colorScheme.primary
        val markwon = remember(textColor, linkColor) {
            val tableTheme = TableTheme.emptyBuilder()
                .tableBorderColor(Color.parseColor("#9E9E9E"))
                .tableBorderWidth(1)
                .tableCellPadding(12)
                .tableHeaderRowBackgroundColor(Color.parseColor("#E0E0E0"))
                .tableOddRowBackgroundColor(Color.parseColor("#FFFFFF"))
                .tableEvenRowBackgroundColor(Color.parseColor("#F5F5F5"))
                .build()
            Markwon.builder(context)
                .usePlugin(object : AbstractMarkwonPlugin() {
                    override fun configureTheme(builder: MarkwonTheme.Builder) {
                        builder
                            .bulletListItemStrokeWidth(0)
                            .listItemColor(textColor.toArgb())
                            .linkColor(linkColor.toArgb())
                    }
                })
                .usePlugin(TablePlugin.create(tableTheme))
                .build()
        }
        Column(modifier = Modifier.fillMaxWidth()) {
            AndroidView(
                factory = { ctx ->
                    TextView(ctx).apply {
                        setTextIsSelectable(true)
                        setHorizontallyScrolling(false)
                    }
                },
                update = { textView ->
                    textView.setTextColor(textColor.toArgb())
                    textView.setLinkTextColor(linkColor.toArgb())
                    markwon.setMarkdown(textView, markdown)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp)
            )
        }
    }
}
