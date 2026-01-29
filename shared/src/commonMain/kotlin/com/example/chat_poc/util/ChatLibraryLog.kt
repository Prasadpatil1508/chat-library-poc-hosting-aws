package com.example.chat_poc.util

/**
 * Multiplatform logging for debugging. Implemented via expect/actual (Android: Log, iOS: NSLog).
 */
expect object ChatLibraryLog {
    fun d(tag: String, message: String)
    fun w(tag: String, message: String)
    fun e(tag: String, message: String)
}
