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

**"Some Kotlin runtime libraries has an unsupported binary format":**
- This happens when the **host app** (or a transitive dependency) uses an older Kotlin runtime than the library (the library is built with **Kotlin 2.3.0**).
- **Fix 1:** Ensure the host’s version catalog has `kotlin = "2.3.0"` and all Kotlin plugins use it. Do **not** use “Downgrade all Kotlin runtime libraries” — that would break the library.
- **Fix 2 (if the error persists):** Force Kotlin 2.3.0 everywhere. In the host app’s **root** `build.gradle.kts` (the one next to `settings.gradle.kts`, not inside `app/`), add:
  ```kotlin
  subprojects {
      configurations.all {
          resolutionStrategy {
              force(
                  "org.jetbrains.kotlin:kotlin-stdlib:2.3.0",
                  "org.jetbrains.kotlin:kotlin-stdlib-jdk8:2.3.0",
                  "org.jetbrains.kotlin:kotlin-stdlib-common:2.3.0"
              )
          }
      }
  }
  ```
- Sync Gradle, **Build → Clean Project**, then **Build → Rebuild Project**. If the IDE still shows the error, try **File → Invalidate Caches / Restart**.

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

**Clear caches so the host uses the latest library:**
1. **Host app:** **Build → Clean Project**, then **Build → Rebuild Project**.
2. **Refresh Gradle dependencies:** **File → Sync Project with Gradle Files**, or in terminal (from host app root): `./gradlew --refresh-dependencies`.
3. **Force Gradle to re-resolve the library:** In terminal (from host app root): `./gradlew clean dependencies --refresh-dependencies`.
4. **Android Studio caches:** **File → Invalidate Caches…** → check **Clear file system cache and Local History** (and **Clear downloaded shared indexes** if you want) → **Invalidate and Restart**.
5. **If using Maven Local:** After republishing the library, you can delete the cached copy so the host must re-fetch: remove `~/.m2/repository/com/example/chat_poc/` (or only the `shared/` / `shared-android/` subfolders for the version you use), then sync/rebuild the host.

**"AAPT2 Daemon startup failed" / "Failed to exec spawn helper" (host app):**
- The `aapt2` binary in the Gradle cache can't be executed. On macOS this is often **Gatekeeper quarantine** or a **corrupted/wrong-architecture** cached binary.
- **Important:** Close **Android Studio** and stop the Gradle daemon before clearing the cache, or you'll get "Operation not permitted". In a terminal run: `./gradlew --stop` (from any Gradle project or the host app root), then quit Android Studio.
- **Fix 1 (remove quarantine):** In a terminal run:  
  `xattr -cr ~/.gradle/caches/`  
  Then reopen the host app, **Build → Clean Project**, and run the app again.
- **Fix 2 (force fresh AAPT2):** Delete the transforms cache so Gradle re-downloads AAPT2:  
  `rm -rf ~/.gradle/caches/8.11.1/transforms`  
  (If your Gradle version is different, use that number instead of `8.11.1` — check the path in the error.) Then open the host app and run the build again.
- **Fix 3:** If you get "Operation not permitted" when deleting, make sure Android Studio is fully quit and run `./gradlew --stop`; on macOS you may need to run the `rm` command in **Terminal.app** (not from an IDE) so it has permission to modify the cache.
- **Fix 4:** Ensure the host app uses an AGP/Android SDK that ships an AAPT2 for your Mac (e.g. Apple Silicon). AGP 8.10+ should be fine; if the problem persists, try updating the Android SDK Build-Tools in SDK Manager.

**"Library compiled with newer Kotlin/Native compiler" (e.g. ScreenTime | ios_simulator_arm64):**
- The IDE is using an older Kotlin/Native plugin that can't read the 2.3.0 platform klibs in `~/.konan/kotlin-native-prebuilt-macos-aarch64-2.3.0/`.
- **Do not** downgrade the project to Kotlin 2.0.21 unless you use the "Last resort" option below — the library uses Kotlin 2.3.0 and downgrading would break the build and the host app.
- **Fix 1 (confirm it's IDE-only):** From the **library** repo run: `./gradlew :shared:assembleRelease :shared:assembleSharedReleaseXCFramework`. If that succeeds, the project is fine; the error is only the IDE not being able to read the klibs.
- **Fix 2:** Update **Android Studio** and **Kotlin** plugin (Settings → Plugins → Kotlin → Update). Then **File → Invalidate Caches / Restart**. After a **new** Android Studio install, also try: close the project, delete the project's `.idea` folder and `.gradle` folder (inside the library repo), then **File → Open** the project again so the IDE re-imports with the new Kotlin support.
- **Fix 3:** Dismiss the notification (e.g. "Don't show again" or close it). You can keep working: run and build from Gradle or the Run button; only the IDE's analysis of the Native klibs is limited.
- **Last resort (IDE must read klibs):** If you must get rid of the IDE error and are okay downgrading the **entire** library and **host** to Kotlin 2.0.21: in this repo set `kotlin = "2.0.21"` in `gradle/libs.versions.toml`, update the Compose compiler/Kotlin plugin references, then run `./gradlew clean :shared:publishToMavenLocal`. In the host app set Kotlin to 2.0.21 and re-sync. You will lose 2.3.0 features and must keep library and host on the same Kotlin version.

---

## iOS

Test the library in your iOS app by building the XCFramework locally and adding it to your Xcode project. This does not affect Android.

### 1. Build the XCFramework (on a Mac)

From the **library repo** (`chat-library-poc/`):

```bash
./gradlew :shared:assembleSharedReleaseXCFramework
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
        ChatPoc_iosKt.createBottomSheetViewController()
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
        let vc = ChatPoc_iosKt.createBottomSheetViewController()
        ChatPoc_iosKt.setBottomSheetDismissHandler { [weak vc] in
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
- Re-run `./gradlew :shared:assembleSharedReleaseXCFramework` in the library repo.
- In Xcode, ensure the app still points at the same `ChatSDK.xcframework` path (or re-add it if you moved the framework).
