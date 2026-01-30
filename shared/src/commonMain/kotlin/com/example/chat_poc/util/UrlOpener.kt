package com.example.chat_poc.util

/**
 * Opens a URL in the platform's default browser (or external app).
 * Implemented via expect/actual (Android: Intent ACTION_VIEW, iOS: UIApplication openURL).
 */
expect object UrlOpener {
    fun openUrl(url: String)
}
