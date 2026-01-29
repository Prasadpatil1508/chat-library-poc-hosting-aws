package com.example.chat_poc.connect

import com.example.chat_poc.config.ConnectConfig
import com.example.chat_poc.config.LibraryConnectConfig
import com.example.chat_poc.util.ChatLibraryLog

/**
 * Creates a [ConnectChatSession] for AWS Connect chat.
 * Uses [ConnectChatDetails] (ContactId, ParticipantId, ParticipantToken) from start-chat and
 * [ConnectConfig.region] to call AWS Participant Service directly; no separate connection URL.
 *
 * @param config Connect config with valid region (from local.properties: API_GATEWAY, REGION, etc.).
 * @return Session instance; call [ConnectChatSession.connect] with [ConnectChatDetails] from [fetchConnectChatDetails].
 */
fun createConnectChatSession(config: ConnectConfig): ConnectChatSession {
    require(config.isValid()) {
        "ConnectConfig (apiGatewayUrl, region, etc.) is required. Add API_GATEWAY, CONTACT_FLOW_ID, INSTANCE_ID, REGION to local.properties."
    }
    ChatLibraryLog.d("ConnectChat", "createConnectChatSession (AWS Participant Service)")
    return ConnectSessionKtor(config)
}

/** Creates a session using library config from local.properties. Returns null if config not set. */
fun createConnectChatSessionOrNull(): ConnectChatSession? {
    val config = LibraryConnectConfig.get() ?: return null
    return if (config.isValid()) createConnectChatSession(config) else null
}
