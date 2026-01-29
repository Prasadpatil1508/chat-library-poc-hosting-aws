package com.example.chat_poc

/**
 * Callbacks from the Chat Library to the host app (library → host).
 *
 * Implement in the host app and pass when showing the bottom sheet.
 * Override only the methods you need; defaults are no-op.
 *
 * Use [onActionButtonClicked] when the user taps the primary action button.
 * Use [onDataToHost] when the library needs to send data back (e.g. selected item, form result).
 */
interface ChatLibraryCallbacks {
    /**
     * Called when the user taps the action button in the bottom sheet.
     * Host can perform navigation, analytics, or any custom logic.
     */
    fun onActionButtonClicked() {}

    /**
     * Called when the library sends data to the host (e.g. user selection, payload).
     * Host can update UI, persist data, or call APIs.
     */
    fun onDataToHost(data: String) {}
}
