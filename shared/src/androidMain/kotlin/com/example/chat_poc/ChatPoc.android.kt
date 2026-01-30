package com.example.chat_poc

import android.content.Context
import androidx.activity.ComponentActivity
import androidx.activity.ComponentDialog
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.platform.ComposeView
import com.example.chat_poc.storage.ChatSessionStorage
import com.example.chat_poc.ui.theme.ChatPocTheme
import com.example.chat_poc.ui.views.ChatBottomSheetContent
import com.example.chat_poc.util.ChatLibraryLog
import com.example.chat_poc.util.UrlOpener

/**
 * Android bridge: shows the chat bottom sheet with [config] and [callbacks].
 * Logic lives in commonMain; this only wires Compose to the Activity.
 */
class ChatBottomSheetDialog(
    context: Context,
    private val config: ChatLibraryConfig,
    private val callbacks: ChatLibraryCallbacks?,
    private val onDismiss: () -> Unit
) : ComponentDialog(context) {

    override fun onStart() {
        super.onStart()
        UrlOpener.setContext(context)
        ChatSessionStorage.setContext(context)
        ChatLibraryLog.d("Android", "Bottom sheet dialog onStart: title=${config.displayTitle}, hasCallbacks=${callbacks != null}")
        window?.setLayout(
            android.view.ViewGroup.LayoutParams.MATCH_PARENT,
            android.view.ViewGroup.LayoutParams.MATCH_PARENT
        )
        val composeView = ComposeView(context).apply {
            setContent {
                ChatPocTheme(darkTheme = isSystemInDarkTheme()) {
                    ChatBottomSheetContent(
                        config = config,
                        callbacks = callbacks,
                        onDismiss = {
                            dismiss()
                            onDismiss()
                        }
                    )
                }
            }
        }
        setContentView(composeView)
    }

    override fun onStop() {
        UrlOpener.setContext(null)
        ChatSessionStorage.setContext(null)
        super.onStop()
    }
}

/**
 * Shows the Chat Library bottom sheet. Pass [config] to send data into the library,
 * [callbacks] to receive button clicks and data from the library.
 */
fun ChatPoc.showBottomSheet(
    activity: ComponentActivity,
    config: ChatLibraryConfig = ChatLibraryConfig(),
    callbacks: ChatLibraryCallbacks? = null
) {
    ChatLibraryLog.d("Android", "showBottomSheet called: title=${config.displayTitle}, messages=${config.displayMessages.size}")
    ChatBottomSheetDialog(
        context = activity,
        config = config,
        callbacks = callbacks,
        onDismiss = {}
    ).show()
}
