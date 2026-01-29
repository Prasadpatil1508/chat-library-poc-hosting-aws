package com.example.chat_poc.util

import platform.Foundation.NSLog

actual object ChatLibraryLog {
    private const val PREFIX = "ChatPoc"

    actual fun d(tag: String, message: String) {
        NSLog("$PREFIX [$tag] D: $message")
    }

    actual fun w(tag: String, message: String) {
        NSLog("$PREFIX [$tag] W: $message")
    }

    actual fun e(tag: String, message: String) {
        NSLog("$PREFIX [$tag] E: $message")
    }
}
