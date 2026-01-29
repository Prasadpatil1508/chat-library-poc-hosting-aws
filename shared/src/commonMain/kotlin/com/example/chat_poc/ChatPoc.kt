package com.example.chat_poc

/**
 * Entry point for the Chat Library (commonMain).
 *
 * - **Android**: Call [showBottomSheet] with a [androidx.activity.ComponentActivity],
 *   optional [ChatLibraryConfig], and optional [ChatLibraryCallbacks].
 *
 * - **iOS**: Call [createBottomSheetViewController] (from platform bridge) with optional
 *   config and callbacks; present the returned UIViewController modally.
 *
 * Config: pass data into the library (e.g. auth token, title, messages).
 * Callbacks: override in the host to handle button clicks and data from the library.
 */
object ChatPoc
