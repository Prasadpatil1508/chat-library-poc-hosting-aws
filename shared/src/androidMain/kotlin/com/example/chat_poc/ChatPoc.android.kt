package com.example.chat_poc

import android.content.Context
import androidx.activity.ComponentActivity
import androidx.activity.ComponentDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.platform.ComposeView

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
        window?.setLayout(
            android.view.ViewGroup.LayoutParams.MATCH_PARENT,
            android.view.ViewGroup.LayoutParams.MATCH_PARENT
        )
        val composeView = ComposeView(context).apply {
            setContent {
                MaterialTheme {
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
    ChatBottomSheetDialog(
        context = activity,
        config = config,
        callbacks = callbacks,
        onDismiss = {}
    ).show()
}
