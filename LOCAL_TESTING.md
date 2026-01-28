# Local Testing Guide

Quick guide to test the library in your Android app without publishing to GitHub.

## Publish to Maven Local

From the **library repo** (`chat-library-poc/`):

```bash
./gradlew :shared:publishToMavenLocal
```

This publishes to `~/.m2/repository/com/example/chat_poc/shared/...`.

**Optional:** Set a specific version:
```bash
./gradlew :shared:publishToMavenLocal -PLIB_VERSION=1.0.0
```

## Add to Your Android App

### 1. Add Maven Local repository

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

### 2. Add the dependency

In your **app module's `build.gradle.kts`**:

```kotlin
dependencies {
    implementation("com.example.chat_poc:shared:1.0.0")
}
```

Use the same version you published (default is `1.0.0`).

### 3. Use the library

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

## Verify it's working

1. **Sync Gradle** in Android Studio
2. The dependency should resolve (no red errors)
3. Run your app and tap the button
4. You should see a bottom sheet with **"Hello from Chat Library"**

## Troubleshooting

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
