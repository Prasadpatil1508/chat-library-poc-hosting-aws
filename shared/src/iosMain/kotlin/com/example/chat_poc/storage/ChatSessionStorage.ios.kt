package com.example.chat_poc.storage

import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSUserDefaults

@OptIn(ExperimentalForeignApi::class)
actual object ChatSessionStorage {

    private const val KEY_PARTICIPANT_TOKEN = "participantToken"
    private const val KEY_CONTACT_ID = "contactID"

    private fun defaults() = NSUserDefaults.standardUserDefaults

    actual fun getParticipantToken(): String? =
        defaults().stringForKey(KEY_PARTICIPANT_TOKEN)

    actual fun setParticipantToken(value: String?) {
        if (value != null) defaults().setObject(value, KEY_PARTICIPANT_TOKEN)
        else defaults().removeObjectForKey(KEY_PARTICIPANT_TOKEN)
    }

    actual fun getContactId(): String? =
        defaults().stringForKey(KEY_CONTACT_ID)

    actual fun setContactId(value: String?) {
        if (value != null) defaults().setObject(value, KEY_CONTACT_ID)
        else defaults().removeObjectForKey(KEY_CONTACT_ID)
    }

    actual fun clear() {
        defaults().removeObjectForKey(KEY_PARTICIPANT_TOKEN)
        defaults().removeObjectForKey(KEY_CONTACT_ID)
    }
}
