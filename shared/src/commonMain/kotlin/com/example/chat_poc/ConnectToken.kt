package com.example.chat_poc

import com.example.chat_poc.config.ConnectConfig
import com.example.chat_poc.config.LibraryConnectConfig
import com.example.chat_poc.domain.FetchConnectTokenUseCase
import com.example.chat_poc.util.ChatLibraryLog

/**
 * Fetches the start-chat token using the library's config (from local.properties at build time).
 * Call from the library UI (e.g. "Fetch Connect token" button) or from the host.
 *
 * @return [Result] with the token string on success; failure if config is missing or API fails.
 */
suspend fun fetchConnectToken(): Result<String> {
    ChatLibraryLog.d("ConnectToken", "fetchConnectToken() called (library config)")
    val config = LibraryConnectConfig.get()
    if (config == null) {
        ChatLibraryLog.w("ConnectToken", "Connect config not set (add API_GATEWAY, etc. to local.properties)")
        return Result.failure(IllegalStateException("Connect config not set in library (add API_GATEWAY, etc. to local.properties)"))
    }
    return FetchConnectTokenUseCase().invoke(config).also { result ->
        result.onSuccess { ChatLibraryLog.d("ConnectToken", "fetchConnectToken success") }
        result.onFailure { e -> ChatLibraryLog.e("ConnectToken", "fetchConnectToken failed: ${e.message}") }
    }
}

/** Fetches token with an explicit config (e.g. for tests). */
suspend fun fetchConnectToken(config: ConnectConfig): Result<String> {
    ChatLibraryLog.d("ConnectToken", "fetchConnectToken(config) called (explicit config)")
    return FetchConnectTokenUseCase().invoke(config)
}
