# Local Testing Guide

Quick guide to test the library in your **Android** or **iOS** app without publishing to GitHub.  
Android and iOS setups are independent; doing one does not affect the other.

---

## Consumer requirements (Android / host app)

The library is built with **Kotlin 2.3.0** and uses **Compose** (including the mikepenz markdown renderer, which targets Compose 1.10). Your **host app** must align:

1. **Kotlin 2.3.0** – Set `kotlin = "2.3.0"` in your version catalog (or equivalent). Otherwise you may see "source must not be null" or "Module was compiled with an incompatible version of Kotlin."
2. **Compose BOM 2025.01.00 or newer** – Use `compose-bom:2025.01.00` (or a later BOM that includes Compose 1.10 runtime). Older BOMs (e.g. 2024.09.00) ship an older Compose runtime and can cause `NoSuchMethodError` (e.g. `Updater.init-impl`) when the library’s markdown composables run.

Sync Gradle and rebuild after changing versions.

---

## Android

### 1. Publish to Maven Local

From the **library repo** (`chat-library-poc/`):

```bash
./gradlew :shared:publishToMavenLocal
```

This publishes to `~/.m2/repository/com/example/chat_poc/shared/...`.

**Optional:** Set a specific version:
```bash
./gradlew :shared:publishToMavenLocal -PLIB_VERSION=1.0.0
```

### 2. Add to Your Android App

**Add Maven Local repository**

In your app's **`settings.gradle.kts`** (or root `build.gradle.kts`):

```kotlin
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        mavenLocal()  // ← Add this
    }
}
```

**Add the dependency**

In your **app module's `build.gradle.kts`**:

```kotlin
dependencies {
    implementation("com.example.chat_poc:shared:1.0.0")
}
```

Use the same version you published (default is `1.0.0`).

**Use the library**

```kotlin
import androidx.activity.ComponentActivity
import com.example.chat_poc.ChatPoc
import com.example.chat_poc.showBottomSheet

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // ... your UI setup ...
        
        // When user taps a button:
        button.setOnClickListener {
            ChatPoc.showBottomSheet(this)
        }
    }
}
```

### 3. Verify it's working

1. **Sync Gradle** in Android Studio
2. The dependency should resolve (no red errors)
3. Run your app and tap the button
4. You should see a bottom sheet with **"Hello from Chat Library"**

### 4. Troubleshooting (Android)

**"Could not find com.example.chat_poc:shared:1.0.0":**
- Make sure you ran `publishToMavenLocal` from the library repo
- Check the version matches (library's `build.gradle.kts` default or `-PLIB_VERSION`)
- Ensure `mavenLocal()` is in your app's repositories

**"shared-android (AAR) missing" or "--- com.example.chat_poc:shared:1.0.0 FAILED":**
- The library should publish `shared-android` automatically
- Try: `./gradlew :shared:clean :shared:publishToMavenLocal`
- Check `~/.m2/repository/com/example/chat_poc/shared-android/1.0.0/` exists
- Verify the build output shows: `✓ Attached Android release component to 'android' publication`
- If you see an error about components, check that `android.publishing.singleVariant("release")` is set in `shared/build.gradle.kts`
- Make sure you're building the Android variant: `./gradlew :shared:assembleRelease` before publishing

**After updating the library:**
- Re-run `publishToMavenLocal` in the library repo
- In your app: **File → Sync Project with Gradle Files**

---

## iOS

Test the library in your iOS app by building the XCFramework locally and adding it to your Xcode project. This does not affect Android.

### 1. Build the XCFramework (on a Mac)

From the **library repo** (`chat-library-poc/`):

```bash
./gradlew :shared:assembleReleaseXCFramework
```

The XCFramework is created at:

```
shared/build/XCFrameworks/release/ChatSDK.xcframework
```

### 2. Add the framework to your iOS app

1. Open your app in **Xcode**.
2. Select your **app target** → **General** → **Frameworks, Libraries, and Embedded Content**.
3. Click **+** → **Add Other…** → **Add Files…**.
4. Navigate to `chat-library-poc/shared/build/XCFrameworks/release/` and select **ChatSDK.xcframework**.
5. Set **Embed** to **Embed & Sign**.

### 3. Use the library

Import `ChatSDK` and call `createBottomSheetViewController()`. Present that view controller as a sheet.

**SwiftUI example:**

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
        Com_example_chat_pocChatPoc_iosKt.createBottomSheetViewController()
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}
```

**UIKit example:**

```swift
import UIKit
import ChatSDK

class ViewController: UIViewController {
    @IBAction func openChatTapped(_ sender: Any) {
        let vc = Com_example_chat_pocChatPoc_iosKt.createBottomSheetViewController()
        Com_example_chat_pocChatPoc_iosKt.setBottomSheetDismissHandler { [weak vc] in
            vc?.dismiss(animated: true)
        }
        vc.modalPresentationStyle = .pageSheet
        present(vc, animated: true)
    }
}
```

### 4. Verify it's working

1. Build and run your iOS app (Simulator or device).
2. Tap the button that presents the chat sheet.
3. You should see a bottom sheet with **"Hello from Chat Library"** and sample messages. You can drag the sheet to expand and scroll.

### 5. Troubleshooting (iOS)

**"No such module 'ChatSDK'"**
- Ensure `ChatSDK.xcframework` is added under **Frameworks, Libraries, and Embedded Content** with **Embed & Sign**.
- Clean build folder (Xcode: **Product** → **Clean Build Folder**) and build again.

**Simulator vs device**
- The XCFramework includes `ios-arm64`, `ios-x86_64`, and `ios-arm64-simulator`. Use the same Xcode destination (Simulator or a real device) you used when building; the framework supports both.

**After updating the library**
- Re-run `./gradlew :shared:assembleReleaseXCFramework` in the library repo.
- In Xcode, ensure the app still points at the same `ChatSDK.xcframework` path (or re-add it if you moved the framework).
