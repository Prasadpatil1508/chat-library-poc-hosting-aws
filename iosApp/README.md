# iOS – Chat Library Phase 1

This folder describes how to add the Chat Library (ChatSDK XCFramework) to a Swift or SwiftUI app and show the bottom sheet when the user taps a button.

**Distribution:** Both **Android** and **iOS** are published to the **same AWS CodeArtifact repository**; Android uses the Maven toolchain and iOS uses the Swift registry. Only the host app’s login (Gradle vs Swift) differs.

---

## Option A: Add the library from AWS CodeArtifact (Swift registry, same repo as Android)

The library is published to AWS CodeArtifact’s **Swift** registry under the same repository name used for Android (e.g. `chat-sdk-repo-dev`). Use the Swift toolchain to consume it.

### Step 1: Configure Swift with CodeArtifact (one-time per machine / 12h token)

In a terminal (with AWS CLI configured):

```bash
aws codeartifact login --tool swift \
  --domain YOUR_DOMAIN \
  --domain-owner YOUR_ACCOUNT_ID \
  --repository YOUR_REPO \
  --region YOUR_REGION
```

Use the **same** domain, account ID, repository name, and region as for Android. This configures the Swift Package Manager to use your CodeArtifact Swift registry.

### Step 2: Add the package in Xcode

1. **File** → **Add Package Dependencies…**
2. In the search bar, enter the package identifier: **`company.chat-sdk`** (scope: `company`, package: `chat-sdk`).
3. Select the version you want (e.g. `1.0.0`) and add the **ChatSDK** product to your app target.

The package is resolved from the CodeArtifact Swift registry. No GitHub repo URL needed.

### Step 3: Use the library in your UI

See **Step 2** under Option B below (same code: `ChatPoc_iosKt.createBottomSheetViewController(config:callbacks:)` and `defaultChatLibraryConfig()`).

---

## Option B: Add the library from GitHub Release (legacy)

If the library also publishes an XCFramework zip to GitHub Releases, you can add the package by **repository URL** instead of the CodeArtifact Swift registry.

### Step 1: Add the package in Xcode

1. Open your **iOS app** project in Xcode.
2. **File** → **Add Package Dependencies…**
3. In the search field, paste the **library repo URL**:
   ```
   https://github.com/Prasadpatil1508/chat-library-poc-hosting-aws
   ```
   (Replace with your org/repo if different.)
4. Click **Add Package**.
5. Ensure **ChatSDK** is selected and added to your **app target**. Click **Add Package** again.

Xcode will fetch the `Package.swift` from the repo and resolve the binary (from the registry or release URL, depending on how the library is published).

### Step 2: Use the library in your UI

**SwiftUI (recommended):**

1. In the view where you want the “Open Chat” button (e.g. `ContentView.swift`), add:

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
        ChatPoc_iosKt.createBottomSheetViewController(config: ChatPoc_iosKt.defaultChatLibraryConfig(), callbacks: nil)
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}
```

2. Build and run (⌘R). Tap the button; the chat bottom sheet should open.

**UIKit:**

```swift
import UIKit
import ChatSDK

class ViewController: UIViewController {
    @IBAction func openChatTapped(_ sender: Any) {
        let vc = ChatPoc_iosKt.createBottomSheetViewController(config: ChatPoc_iosKt.defaultChatLibraryConfig(), callbacks: nil)
        ChatPoc_iosKt.setBottomSheetDismissHandler { [weak vc] in
            vc?.dismiss(animated: true)
        }
        vc.modalPresentationStyle = .pageSheet
        present(vc, animated: true)
    }
}
```

### Step 3: Requirements

- **iOS 14+** (the library’s `Package.swift` declares `.iOS(.v14)`).
- No AWS CLI, no token, no `gradle.properties` – the binary comes from the GitHub Release URL in `Package.swift`.

### If something goes wrong

- **“No such module 'ChatSDK'”** – Confirm the package was added to your **app target** (Step 1.5). In the project navigator, select your app target → **General** → **Frameworks, Libraries, and Embedded Content** → **ChatSDK** should be listed.
- **“Checksum mismatch”** – The library’s `Package.swift` points at a release (e.g. `v1.0.18`). If that release was re-uploaded or the zip changed, the checksum in `Package.swift` must be updated and the library repo republished. As an app developer, you just use the latest version; if the maintainer updates the tag/checksum, try **File** → **Packages** → **Reset Package Caches** and **Update to Latest Package Versions**.
- **“Unable to find a specification for ...”** – You might have entered the wrong repo URL. Use the exact GitHub repo URL (e.g. `https://github.com/Prasadpatil1508/chat-library-poc-hosting-aws`).

---

## Add the framework from GitHub (other options)

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

Import the framework and call `createBottomSheetViewController(config:callbacks:)` when the user taps your button (use `ChatPoc_iosKt.defaultChatLibraryConfig()` and `nil` for callbacks if you don’t need custom config). Present that view controller modally (e.g. as a sheet).

Use the class `ChatPoc_iosKt` and its function `createBottomSheetViewController(config:callbacks:)`. For default config, use `ChatPoc_iosKt.defaultChatLibraryConfig()` and pass `nil` for callbacks. (The framework exports these from the Kotlin `ChatPoc.ios.kt` file.)

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
        ChatPoc_iosKt.createBottomSheetViewController(config: ChatPoc_iosKt.defaultChatLibraryConfig(), callbacks: nil)
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
        let vc = ChatPoc_iosKt.createBottomSheetViewController(config: ChatPoc_iosKt.defaultChatLibraryConfig(), callbacks: nil)
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
