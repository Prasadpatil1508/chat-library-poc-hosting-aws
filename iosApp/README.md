# iOS – Chat Library Phase 1

This folder describes how to add the Chat Library (ChatSDK XCFramework) to a Swift or SwiftUI app and show the bottom sheet when the user taps a button.

## Add the framework from GitHub (recommended)

If the library repo publishes releases with an XCFramework zip (e.g. from [PUBLISHING.md](../PUBLISHING.md)):

### Option A: Download from a GitHub Release

1. Open the latest release: `https://github.com/YOUR_GITHUB_OWNER/chat-library-poc/releases`
2. Download the asset `ChatSDK.xcframework.zip` (or the name used in that release).
3. Unzip it and add `ChatSDK.xcframework` to your app:
   - In Xcode: app target → **General** → **Frameworks, Libraries, and Embedded Content** → **+** → **Add Other…** → **Add Files…** → select `ChatSDK.xcframework`.
   - Set **Embed** to **Embed & Sign**.

Replace `YOUR_GITHUB_OWNER` with the GitHub user or org that hosts the repo.

### Option B: Swift Package Manager (if the repo has Package.swift)

If the library repo contains a `Package.swift` that points at the XCFramework zip URL:

1. In Xcode: **File** → **Add Package Dependencies…**
2. Enter the repo URL: `https://github.com/YOUR_GITHUB_OWNER/chat-library-poc`
3. Add the `ChatSDK` product to your app target.

See [PUBLISHING.md](../PUBLISHING.md) for how the maintainer publishes the XCFramework and (optional) `Package.swift`.

---

## Build and add the framework locally

When you have the library repo on disk and want to build the framework yourself:

### 1. Build the XCFramework

From the library repo root (on a Mac):

```bash
./gradlew :shared:assembleSharedReleaseXCFramework
```

The XCFramework is produced under `shared/build/XCFrameworks/release/` (exact name may vary, e.g. `ChatSDK.xcframework`).

### 2. Add the framework to your Xcode project

1. In Xcode, select your app target → **General** → **Frameworks, Libraries, and Embedded Content**.
2. Click **+** → **Add Other…** → **Add Files…** and select the built `ChatSDK.xcframework`.
3. Set **Embed** to **Embed & Sign** (or **Do Not Embed** if you link it differently).

## Requirements

- Your app must link and embed the `ChatSDK.xcframework` (see above).
- Present the view controller returned by the library as a modal or sheet.

**Optional (high refresh rate):** For best performance on ProMotion iPhones, add to Info.plist: `CADisableMinimumFrameDurationOnPhone` (Boolean, YES). The library disables the strict plist check so the chat sheet works without this; add it only if you want to optimize.

## Usage

Import the framework and call `createBottomSheetViewController()` when the user taps your button. Present that view controller modally (e.g. as a sheet).

Use the class `ChatPoc_iosKt` and its function `createBottomSheetViewController()`. (The framework exports this name from the Kotlin `ChatPoc.ios.kt` file.)

### SwiftUI

```swift
import SwiftUI
import ChatSDK

struct ContentView: View {
    @State private var showChat = false

    var body: some View {
        Button("Open Chat Library") {
            showChat = true
        }
        .sheet(isPresented: $showChat) {
            ChatSheetView()
        }
    }
}

struct ChatSheetView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        ChatPoc_iosKt.createBottomSheetViewController()
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}
```

### UIKit

When using `present(_:animated:)`, set the dismiss handler so that when the user dismisses the bottom sheet from inside (drag handle / gesture), the view controller is closed:

```swift
import UIKit
import ChatSDK

class ViewController: UIViewController {
    @IBAction func openChatTapped(_ sender: Any) {
        let vc = ChatPoc_iosKt.createBottomSheetViewController()
        ChatPoc_iosKt.setBottomSheetDismissHandler { [weak vc] in
            vc?.dismiss(animated: true)
        }
        vc.modalPresentationStyle = .pageSheet
        present(vc, animated: true)
    }
}
```

With SwiftUI `.sheet`, the system handles dismiss when the user swipes; you don't need to call `setBottomSheetDismissHandler` unless you want the Compose sheet's internal dismiss to also close the presented VC.

The Compose chat UI (bottom sheet) renders Markdown on iOS using the same shared markdown renderer as Android.

## Phase 1 behavior

- Tapping **Open Chat Library** (or your equivalent button) presents a bottom sheet.
- The sheet displays the text: **"Hello from Chat Library"**.
- The user can dismiss by swiping down or using the sheet’s drag handle.
