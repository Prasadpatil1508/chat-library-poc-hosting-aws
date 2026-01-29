package com.example.chat_poc.util

import android.util.Log

actual object ChatLibraryLog {
    private const val PREFIX = "ChatPoc"

    actual fun d(tag: String, message: String) {
        Log.d("$PREFIX:$tag", message)
    }

    actual fun w(tag: String, message: String) {
        Log.w("$PREFIX:$tag", message)
    }

    actual fun e(tag: String, message: String) {
        Log.e("$PREFIX:$tag", message)
    }
}
