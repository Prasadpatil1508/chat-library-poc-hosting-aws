package com.example.chat_poc

import androidx.compose.ui.window.ComposeUIViewController
import com.example.chat_poc.ui.theme.ChatPocTheme
import com.example.chat_poc.ui.views.ChatBottomSheetContent
import com.example.chat_poc.util.ChatLibraryLog
import platform.UIKit.UIViewController

private var bottomSheetDismissHandler: (() -> Unit)? = null

/**
 * Set from Swift before presenting so that when the user dismisses the sheet,
 * the host can call vc.dismiss(animated: true).
 */
fun setBottomSheetDismissHandler(handler: () -> Unit) {
    ChatLibraryLog.d("iOS", "setBottomSheetDismissHandler set")
    bottomSheetDismissHandler = handler
}

/**
 * iOS bridge: returns a UIViewController that shows the chat bottom sheet.
 * Pass [config] to send data into the library, [callbacks] to receive events.
 * Logic lives in commonMain; this only wires Compose to UIKit.
 */
fun createBottomSheetViewController(
    config: ChatLibraryConfig = ChatLibraryConfig(),
    callbacks: ChatLibraryCallbacks? = null
): UIViewController {
    ChatLibraryLog.d("iOS", "createBottomSheetViewController: title=${config.displayTitle}, messages=${config.displayMessages.size}, hasCallbacks=${callbacks != null}")
    return ComposeUIViewController {
        ChatPocTheme(darkTheme = false) {
            ChatBottomSheetContent(
                config = config,
                callbacks = callbacks,
                onDismiss = { bottomSheetDismissHandler?.invoke() }
            )
        }
    }
}

/**
 * Swift-friendly overload: pass primitives and closures. Builds [ChatLibraryConfig]
 * and a [ChatLibraryCallbacks] implementation that delegates to the closures.
 */
fun createBottomSheetViewController(
    title: String,
    messages: List<String>,
    authToken: String,
    onActionButtonClicked: (() -> Unit)?,
    onDataToHost: ((String) -> Unit)?
): UIViewController {
    val config = ChatLibraryConfig(
        authToken = authToken,
        displayTitle = title.ifBlank { "Hello from Chat Library" },
        displayMessages = messages.ifEmpty { ChatLibraryConfig().displayMessages }
    )
    val callbacks = object : ChatLibraryCallbacks {
        override fun onActionButtonClicked() = onActionButtonClicked?.invoke() ?: Unit
        override fun onDataToHost(data: String) = onDataToHost?.invoke(data) ?: Unit
    }
    return createBottomSheetViewController(config, callbacks)
}
