package com.example.chat_poc

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.window.ComposeUIViewController
import platform.UIKit.UIViewController

/**
 * Optional dismiss callback, set by the host app before presenting.
 * When the user dismisses the bottom sheet (swipe/tap), we invoke this so the host can call
 * `presentedViewController.dismiss(animated: true)`.
 *
 * Set from Swift before presenting, e.g.:
 *   let vc = Com_example_chat_pocChatPoc_iosKt.createBottomSheetViewController()
 *   Com_example_chat_pocChatPoc_iosKt.setBottomSheetDismissHandler { vc.dismiss(animated: true) }
 *   present(vc, animated: true)
 */
private var bottomSheetDismissHandler: (() -> Unit)? = null

fun setBottomSheetDismissHandler(handler: () -> Unit) {
    bottomSheetDismissHandler = handler
}

/**
 * Returns a UIViewController that displays the Chat Library bottom sheet with simple text.
 * Presents the same Phase 1 content as Android (MaterialTheme + ChatBottomSheetContent).
 *
 * **Usage from Swift:**
 * 1. Create: `let vc = Com_example_chat_pocChatPoc_iosKt.createBottomSheetViewController()`
 * 2. Set dismiss handler: `Com_example_chat_pocChatPoc_iosKt.setBottomSheetDismissHandler { vc.dismiss(animated: true) }`
 * 3. Present: `present(vc, animated: true)` or use `.sheet { ... }` with a wrapper that uses this VC.
 *
 * If you don't set the dismiss handler, the sheet's onDismiss will no-op; the user can still
 * dismiss via the sheet's drag handle or by sliding down (when presented as a sheet).
 */
fun createBottomSheetViewController(): UIViewController =
    ComposeUIViewController {
        MaterialTheme {
            ChatBottomSheetContent(
                onDismiss = { bottomSheetDismissHandler?.invoke() }
            )
        }
    }
