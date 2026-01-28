package com.example.chat_poc

import android.content.Context
import androidx.activity.ComponentActivity
import androidx.activity.ComponentDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.platform.ComposeView

/**
 * Production-safe chat dialog.
 * Lifecycle, ViewModelStore, and SavedState are wired automatically.
 */
class ChatBottomSheetDialog(
    context: Context,
    private val onDismiss: () -> Unit
) : ComponentDialog(context) {

    override fun onStart() {
        super.onStart()

        // Full-screen style (same behavior you had before)
        window?.setLayout(
            android.view.ViewGroup.LayoutParams.MATCH_PARENT,
            android.view.ViewGroup.LayoutParams.MATCH_PARENT
        )

        val composeView = ComposeView(context).apply {
            setContent {
                MaterialTheme {
                    ChatBottomSheetContent(
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
 * Shows the Chat Library bottom sheet with simple text.
 * Call this when the user taps your button. Requires [ComponentActivity] (e.g. from Activity Compose).
 */
fun ChatPoc.showBottomSheet(activity: ComponentActivity) {
    ChatBottomSheetDialog(
        context = activity,
        onDismiss = {}
    ).show()
}
