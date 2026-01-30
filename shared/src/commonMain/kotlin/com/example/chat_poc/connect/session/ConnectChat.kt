package com.example.chat_poc.connect.session

import com.example.chat_poc.config.ConnectConfig
import com.example.chat_poc.config.LibraryConnectConfig
import com.example.chat_poc.util.ChatLibraryLog

/**
 * Creates a [ConnectChatSession] for AWS Connect chat.
 * Uses [ConnectChatDetails] from start-chat and [ConnectConfig.region].
 *
 * @param config Connect config with valid region.
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
