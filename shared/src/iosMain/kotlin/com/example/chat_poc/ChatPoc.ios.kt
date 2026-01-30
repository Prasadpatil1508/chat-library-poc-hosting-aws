@file:OptIn(kotlin.experimental.ExperimentalNativeApi::class)
package com.example.chat_poc

import kotlin.experimental.ExperimentalNativeApi
import kotlin.native.setUnhandledExceptionHook
import kotlin.native.terminateWithUnhandledException
import androidx.compose.runtime.Composable
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.window.ComposeUIViewController
import com.example.chat_poc.ui.views.ChatBottomSheetContent
import com.example.chat_poc.util.ChatLibraryLog
import platform.UIKit.UIViewController
import platform.Foundation.NSLog

private var bottomSheetDismissHandler: (() -> Unit)? = null

/** Install once so uncaught Kotlin exceptions (e.g. require/check) are logged before abort. */
@OptIn(ExperimentalNativeApi::class)
private fun installExceptionLogger() {
    setUnhandledExceptionHook { throwable ->
        val msg = throwable.message ?: "no message"
        val stack = throwable.getStackTrace().joinToString("\n") { "  at $it" }
        NSLog("ChatPoc [FATAL] Unhandled Kotlin exception: $msg")
        NSLog("ChatPoc [FATAL] Stack trace:\n$stack")
        terminateWithUnhandledException(throwable)
    }
}

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
    installExceptionLogger()
    ChatLibraryLog.d("iOS", "[1/5] createBottomSheetViewController called (config overload)")
    val msgCount = runCatching { config.displayMessages.size }.getOrElse { -1 }
    ChatLibraryLog.d("iOS", "[2/5] config: title=${config.displayTitle}, messagesCount=$msgCount, hasCallbacks=${callbacks != null}")
    ChatLibraryLog.d("iOS", "[3/5] creating ComposeUIViewController...")
    return ComposeUIViewController(configure = { enforceStrictPlistSanityCheck = false }) {
        ChatSheetComposable(config, callbacks)
    }
}

@Composable
private fun ChatSheetComposable(
    config: ChatLibraryConfig,
    callbacks: ChatLibraryCallbacks?,
) {
    ChatLibraryLog.d("iOS", "[4/5] inside Compose content block, setting MaterialTheme...")
    MaterialTheme(colorScheme = lightColorScheme()) {
        ChatLibraryLog.d("iOS", "[5/5] composing ChatBottomSheetContent...")
        ChatBottomSheetContent(
            config = config,
            callbacks = callbacks,
            onDismiss = { bottomSheetDismissHandler?.invoke() }
        )
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
    ChatLibraryLog.d("iOS", "[Swift overload] called: title=$title, authToken.length=${authToken.length}, messages param NOT touched (K/N interop risk)")
    // Do not touch [messages] from Swift: K/N list interop can trigger kotlin#error() on access.
    val config = ChatLibraryConfig(
        authToken = authToken,
        displayTitle = title.ifBlank { "Hello from Chat Library" },
        displayMessages = ChatLibraryConfig().displayMessages
    )
    val callbacks = object : ChatLibraryCallbacks {
        override fun onActionButtonClicked() = onActionButtonClicked?.invoke() ?: Unit
        override fun onDataToHost(data: String) = onDataToHost?.invoke(data) ?: Unit
    }
    ChatLibraryLog.d("iOS", "[Swift overload] delegating to config overload...")
    return createBottomSheetViewController(config, callbacks)
}
