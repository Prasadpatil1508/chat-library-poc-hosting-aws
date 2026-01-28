package com.example.chat_poc

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform