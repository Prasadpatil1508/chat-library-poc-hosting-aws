package com.example.chat_poc.api

import com.example.chat_poc.config.ConnectConfig
import com.example.chat_poc.model.StartChatResponse

/**
 * Port for fetching the start-chat token (DIP: depend on abstraction).
 */
interface StartChatApi {
    suspend fun fetchToken(config: ConnectConfig): Result<StartChatResponse>
}
