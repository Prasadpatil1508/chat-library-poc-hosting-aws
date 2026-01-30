package com.example.chat_poc.util

import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSURL
import platform.UIKit.UIApplication

@OptIn(ExperimentalForeignApi::class)
actual object UrlOpener {
    actual fun openUrl(url: String) {
        val nsUrl = NSURL.URLWithString(url) ?: return
        @Suppress("DEPRECATION")
        UIApplication.sharedApplication.openURL(nsUrl)
    }
}
