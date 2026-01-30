package com.example.chat_poc.storage

/**
 * Persistent storage for chat session data from the start-chat REST API.
 * Aligns with [amazon-connect-chat-ui-examples](https://github.com/amazon-connect/amazon-connect-chat-ui-examples)
 * (Android: SharedPreferences "ConnectChat"; iOS: UserDefaults).
 *
 * Use after start-chat to persist participant token and contact ID for resume/reconnect.
 * Android actual requires [setContext] to be called before use (e.g. from ChatBottomSheetDialog).
 *
 * **To clear stored data** (participant token + contact ID) so the user starts a completely fresh chat
 * with no "Resume chat" option, call [clear] from your UI (e.g. a "Clear session" / "Start fresh" button)
 * or on logout. Example: `ChatSessionStorage.clear()`
 */
expect object ChatSessionStorage {

    fun getParticipantToken(): String?
    fun setParticipantToken(value: String?)

    fun getContactId(): String?
    fun setContactId(value: String?)

    /** Clears all stored session data (participant token and contact ID). Call to start fresh with no resume. */
    fun clear()
}
