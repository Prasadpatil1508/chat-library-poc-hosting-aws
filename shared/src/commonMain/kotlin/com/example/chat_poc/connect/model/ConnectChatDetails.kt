package com.example.chat_poc.connect.model

/**
 * Details from StartChatContact used to connect to AWS Connect chat.
 * Pass to [ConnectChatSession.connect].
 *
 * @param participantToken Required; from start-chat API.
 * @param contactId Optional; from start-chat response (ContactId).
 * @param participantId Optional; from start-chat response (ParticipantId).
 */
data class ConnectChatDetails(
    val participantToken: String,
    val contactId: String? = null,
    val participantId: String? = null,
) {
    init {
        require(participantToken.isNotBlank()) { "participantToken is required" }
    }
}
