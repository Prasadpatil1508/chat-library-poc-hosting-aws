# iOS – Chat Library Phase 1

This folder describes how to add the Chat Library (ChatSDK XCFramework) to a Swift or SwiftUI app and show the bottom sheet when the user taps a button.

**Distribution:** Both **Android** and **iOS** are published to the **same AWS CodeArtifact repository**; Android uses the Maven toolchain and iOS uses the Swift registry. Only the host app’s login (Gradle vs Swift) differs.

---

## Option A: Add the library from AWS CodeArtifact (Swift registry)

The library is published to AWS CodeArtifact’s **Swift** registry under the same repository name used for Android (e.g. `chat-sdk-repo-dev`). Use the Swift toolchain to consume it.

### Step 1: Configure Swift with CodeArtifact (required once per machine; token lasts ~12 hours)

Per [AWS Configure Swift](https://docs.aws.amazon.com/codeartifact/latest/ug/configure-swift.html): the login command must run from a directory that contains **Package.swift**. It stores the token (macOS: Keychain) and writes the registry URL to `.swiftpm/configuration/registries.json`. In a terminal (AWS CLI configured):

```bash
aws codeartifact login --tool swift \
  --domain YOUR_DOMAIN \
  --domain-owner YOUR_ACCOUNT_ID \
  --repository YOUR_REPO \
  --region YOUR_REGION
```

**Important:** Run this from a directory that contains a **Package.swift**. Otherwise Swift returns: *"Could not find Package.swift in this directory or any of its parent directories."*

**If your host app has no Package.swift** (e.g. it’s a plain Xcode project), add the following so you can run the login from the host app root and Xcode can resolve the registry from that directory:

1. **In your host app root** (same folder as your `.xcodeproj`), create **Package.swift** at that root—**not** inside a subfolder. If you add these files in Xcode, do **not** add them to your app target’s “Compile Sources” (uncheck Target Membership for Package.swift and RegistryHelper), or Xcode will try to compile the manifest and show “No such module 'PackageDescription'”.

**Critical:** The **first line** of `Package.swift` must be exactly `// swift-tools-version: 5.9` with **no blank lines or other text above it**. Otherwise Xcode reports: *"the manifest is backward-incompatible with Swift < 6.0 because the tools-version was specified in a subsequent line"*. Open the file and delete anything before that line.

```swift
// swift-tools-version: 5.9
import PackageDescription

let package = Package(
    name: "HostAppRegistryHelper",
    platforms: [.iOS(.v14)],
    products: [],
    dependencies: [
        .package(id: "company.chat-sdk", from: "1.0.0"),
    ],
    targets: [
        .target(name: "RegistryHelper", path: "RegistryHelper"),
    ]
)
```

**Why add `dependencies`?** Per [AWS Consuming Swift packages](https://docs.aws.amazon.com/codeartifact/latest/ug/swift-publish-consume.html), the package identifier (`company.chat-sdk`) and version range can be declared in Package.swift. That lets you run **`swift package resolve`** from the host app root to fetch ChatSDK from CodeArtifact and verify the registry + credentials work. Your app target is still in the `.xcodeproj`; you add the ChatSDK product to the app via Xcode (Step 2 below). Use a version range that matches what you published (e.g. `from: "1.0.0"` or `from: "1.0.19"`).

2. At the **same root** (next to Package.swift), create a folder **RegistryHelper** (same directory as Package.swift) and inside it add **RegistryHelper.swift** with a single line (e.g. `import Foundation`). This satisfies SPM’s requirement for a source directory; your app still builds from the `.xcodeproj` only. If you see *invalid custom path 'RegistryHelper'*, the folder is missing or in the wrong place—it must be next to Package.swift and contain at least one .swift file.

3. **From the host app root** (the directory that now contains Package.swift and RegistryHelper/), run the login (replace with your domain/repo/region):

```bash
cd /path/to/your/ios-host-app
aws codeartifact login --tool swift \
  --domain YOUR_DOMAIN \
  --domain-owner YOUR_ACCOUNT_ID \
  --repository YOUR_REPO \
  --region YOUR_REGION
```

4. **Keep them out of the app target:** In Xcode, select **Package.swift** and the **RegistryHelper** folder/file. In the File inspector (right panel), under **Target Membership**, ensure your app target is **unchecked** for both. They must exist on disk for the login command; the app must not compile them.

   **If you see “No such module 'PackageDescription'”:** Xcode is compiling `Package.swift` as app source. Select **Package.swift** in the Project Navigator → **File** inspector (right panel) → **Target Membership** → **uncheck** your app target. Do the same for **RegistryHelper** and **RegistryHelper.swift**. Then open your **app target** → **Build Phases** → **Compile Sources** and remove **Package.swift** and **RegistryHelper.swift** if they are listed (select and click **−**).

5. Re-run the login every ~12 hours (token expiry) from the same directory before resolving or updating packages in Xcode.

**Confirm configuration** (per AWS): run `cat .swiftpm/configuration/registries.json` in the same directory; you should see your registry URL under `registries`. Use the same domain, account ID, repository name, and region as for Android.

### Step 2: Add the package in Xcode (per AWS “Consuming in Xcode”)

Per [AWS Consuming Swift packages](https://docs.aws.amazon.com/codeartifact/latest/ug/swift-publish-consume.html): *“Your search must be in the form package_scope.package_name”*.

1. Open your app in Xcode (open the `.xcodeproj` from the same directory where you ran the login).
2. **File** → **Add Package Dependencies…** (or **Add Packages…**).
3. In the search bar, enter **`company.chat-sdk`** (scope: `company`, package: `chat-sdk`).
4. When the package appears, choose it and **Add Package**.
5. Select the **ChatSDK** product and add it to your app target; finish with **Add Package**.

### Step 3: Use the library in your UI

See **Step 2** under Option B below (same code: `ChatPoc_iosKt.createBottomSheetViewController(config:callbacks:)` and `defaultChatLibraryConfig()`).

### If Xcode doesn’t resolve the package (login dialog or “could not be accessed”)

Xcode uses **Keychain** for registry credentials. Use Swift PM’s login with the **registry URL + `login`** and **`--token`** so credentials are stored correctly (AWS docs: append `login` to the repo URL for this step).

1. **From your host app root** (directory that contains `Package.swift`), run:

   ```bash
   cd /path/to/your/ios-host-app

   export CODEARTIFACT_AUTH_TOKEN=$(aws codeartifact get-authorization-token \
     --domain YOUR_DOMAIN \
     --domain-owner YOUR_ACCOUNT_ID \
     --region YOUR_REGION \
     --query authorizationToken --output text)

   export CODEARTIFACT_REPO=$(aws codeartifact get-repository-endpoint \
     --domain YOUR_DOMAIN \
     --domain-owner YOUR_ACCOUNT_ID \
     --repository YOUR_REPO \
     --format swift \
     --query repositoryEndpoint --output text)
   ```

2. **Store credentials in Keychain** (URL must end with `login`):

   ```bash
   swift package-registry login ${CODEARTIFACT_REPO}login --token ${CODEARTIFACT_AUTH_TOKEN}
   ```

3. **Set the registry for this project** (so packages resolve from CodeArtifact):

   ```bash
   swift package-registry set ${CODEARTIFACT_REPO}
   ```

4. **Quit Xcode**, reopen the project from this directory, then **File** → **Add Package Dependencies…** and add by **package identifier** `company.chat-sdk` (or paste the full package URL). When prompted for credentials, use **User Name:** `aws`, **Password:** a fresh token; when asked for “login keychain password”, enter your **Mac user password** so Xcode can save the credential.

5. Use **Xcode 15 or later**; earlier versions had issues with registry auth from Keychain.

**References:** [Configure Swift with CodeArtifact](https://docs.aws.amazon.com/codeartifact/latest/ug/configure-swift.html), [Consuming and publishing Swift packages](https://docs.aws.amazon.com/codeartifact/latest/ug/swift-publish-consume.html), [Swift troubleshooting](https://docs.aws.amazon.com/codeartifact/latest/ug/swift-troubleshooting.html).

If it still fails after the steps above, you can fall back to adding the package from a **downloaded archive** (download the `.zip` for the package version from the registry with `curl -u "aws:$TOKEN"` and **Add Local…** in Xcode); the exact curl URL is `{REGISTRY_BASE}/company/chat-sdk/{VERSION}.zip` with header `Accept: application/vnd.swift.registry.v1+zip`. The unzipped folder must contain both **Package.swift** and **ChatSDK.xcframework**. If you see *"does not contain a binary artifact"*, the archive was published without the xcframework—use the xcframework from a GitHub Release (see Option B) or wait for a republish; the publish workflow has been updated to include the xcframework in the archive.

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
