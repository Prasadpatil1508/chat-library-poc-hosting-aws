# Chat Library POC

Kotlin Multiplatform (KMP) library that shows a bottom sheet with simple text when invoked from an Android or Swift (iOS) app.

## Quick start — test locally

**Fastest way to test:** publish to Maven Local and add it to your Android app.

1. **Publish to Maven Local** (from this repo):
   ```bash
   ./gradlew :shared:publishToMavenLocal
   ```

2. **In your Android app:**
   - Add `mavenLocal()` to repositories
   - Add `implementation("com.example.chat_poc:shared:1.0.0")`
   - See [LOCAL_TESTING.md](LOCAL_TESTING.md) for full steps

## Adding the library from GitHub

- **Android:** add the library from **GitHub Packages** (Maven). See [androidApp/README.md](androidApp/README.md).
- **iOS:** add the **XCFramework** from a **GitHub Release** or via **Swift Package Manager** if the repo has a `Package.swift`. See [iosApp/README.md](iosApp/README.md).

To publish the library to GitHub (Packages + Releases), follow [PUBLISHING.md](PUBLISHING.md).

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
