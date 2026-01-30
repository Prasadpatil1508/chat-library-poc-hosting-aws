package com.example.chat_poc.storage

import android.content.Context

private const val PREFS_NAME = "ConnectChat"
private const val KEY_PARTICIPANT_TOKEN = "participantToken"
private const val KEY_CONTACT_ID = "contactID"

actual object ChatSessionStorage {

    @Volatile
    private var appContext: Context? = null

    /** Set by the host (e.g. ChatBottomSheetDialog) so storage can access SharedPreferences. */
    fun setContext(context: Context?) {
        appContext = context
    }

    private fun prefs(): android.content.SharedPreferences? =
        appContext?.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    actual fun getParticipantToken(): String? =
        prefs()?.getString(KEY_PARTICIPANT_TOKEN, null)

    actual fun setParticipantToken(value: String?) {
        prefs()?.edit()?.putString(KEY_PARTICIPANT_TOKEN, value)?.apply()
    }

    actual fun getContactId(): String? =
        prefs()?.getString(KEY_CONTACT_ID, null)

    actual fun setContactId(value: String?) {
        prefs()?.edit()?.putString(KEY_CONTACT_ID, value)?.apply()
    }

    actual fun clear() {
        prefs()?.edit()?.clear()?.apply()
    }
}
