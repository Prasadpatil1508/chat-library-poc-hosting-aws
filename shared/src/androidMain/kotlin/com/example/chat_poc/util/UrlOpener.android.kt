package com.example.chat_poc.util

import android.content.Context
import android.content.Intent
import android.net.Uri

actual object UrlOpener {
    @Volatile
    private var appContext: Context? = null

    /** Set by the host (e.g. ChatBottomSheetDialog) so [openUrl] can start the browser. */
    fun setContext(context: Context?) {
        appContext = context
    }

    actual fun openUrl(url: String) {
        val ctx = appContext ?: return
        val uri = runCatching { Uri.parse(url) }.getOrNull() ?: return
        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        runCatching {
            ctx.startActivity(intent)
        }.onFailure {
            ChatLibraryLog.e("UrlOpener", "Failed to open URL: ${it.message}")
        }
    }
}
