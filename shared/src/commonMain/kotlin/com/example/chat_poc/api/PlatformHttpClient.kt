package com.example.chat_poc.api

import io.ktor.client.HttpClient

/**
 * Platform-provided [HttpClient] (OkHttp on Android, Darwin on iOS).
 * Implement in androidMain/iosMain so no engine is hardcoded in commonMain.
 */
expect fun createHttpClient(): HttpClient
