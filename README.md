# Chat Library POC

Kotlin Multiplatform (KMP) library that shows a bottom sheet with simple text when invoked from an Android or Swift (iOS) app.

## Phase 1

- **Library**: `:shared` – Compose Multiplatform UI (Material3 `ModalBottomSheet`) + platform entry points.
- **Android**: `ChatPoc.showBottomSheet(activity)` – shows the bottom sheet over the given `ComponentActivity`.
- **iOS**: `createBottomSheetViewController()` – returns a `UIViewController` that you present modally (e.g. as a sheet).

### Android integration

See **androidApp/README.md** for how to add the library to an Android app (dependency setup, usage, and examples).

### iOS integration

See **iosApp/README.md** for how to add the library to an iOS app (build the XCFramework, add it in Xcode, and SwiftUI/UIKit usage).

### Requirements

- **Android**: `ComponentActivity` (e.g. from Activity Compose). The host app does not need to be fully Compose-based.
- **iOS**: Present the view controller from `createBottomSheetViewController()` as a modal or sheet.

## Project structure

- `shared/` – KMP library (common + android + ios).
- `androidApp/README.md` – Instructions for adding the library to an Android app.
- `iosApp/README.md` – Instructions for adding the library to an iOS app.

## Tech stack

- Kotlin 2.0, Compose Multiplatform 1.6.10, Material3.
- Android: Jetpack Compose (via `ComposeView` in a `Dialog` for the library entry point).
- iOS: `ComposeUIViewController` + XCFramework for Swift/UIKit/SwiftUI.
