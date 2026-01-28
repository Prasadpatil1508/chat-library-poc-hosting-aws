package com.example.chat_poc

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.window.ComposeUIViewController
import platform.UIKit.UIViewController

private var bottomSheetDismissHandler: (() -> Unit)? = null

/**
 * Swift-friendly API bridge
 */
object ChatSDK {

    fun setBottomSheetDismissHandler(handler: () -> Unit) {
        bottomSheetDismissHandler = handler
    }

    fun createBottomSheetViewController(): UIViewController =
        ComposeUIViewController {
            MaterialTheme {
                ChatBottomSheetContent(
                    onDismiss = { bottomSheetDismissHandler?.invoke() }
                )
            }
        }
}
