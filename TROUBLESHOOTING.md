# Troubleshooting "Unresolved reference: ChatPoc"

## Step 1: Verify Dependency is Added

In your **app module's `build.gradle.kts`**, make sure you have:

```kotlin
dependencies {
    implementation("com.example.chat_poc:shared:1.0.0")
}
```

## Step 2: Verify Maven Local Repository

In your **`settings.gradle.kts`** (or root `build.gradle.kts`):

```kotlin
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        mavenLocal()  // ← Must be present
    }
}
```

## Step 3: Sync Gradle

- **Android Studio**: Click **File → Sync Project with Gradle Files**
- Or run: `./gradlew build --refresh-dependencies`

## Step 4: Correct Import

In your Kotlin file where you use `ChatPoc`:

```kotlin
import com.example.chat_poc.ChatPoc
import com.example.chat_poc.showBottomSheet  // Extension function

// Then use it:
ChatPoc.showBottomSheet(activity)
```

**OR** import just the object and use the extension:

```kotlin
import com.example.chat_poc.ChatPoc

// Extension function is available if ChatPoc is imported
ChatPoc.showBottomSheet(activity)
```

## Step 5: Verify Library is Published

Check that the library exists in Maven Local:

```bash
ls -la ~/.m2/repository/com/example/chat_poc/shared-android/1.0.0/
```

You should see:
- `shared-android-1.0.0.aar`
- `shared-android-1.0.0.pom`
- `shared-android-1.0.0.module`

## Step 6: Clean and Rebuild

If still not working:

1. **In your Android app project:**
   ```bash
   ./gradlew clean build --refresh-dependencies
   ```

2. **In the library project** (if you made changes):
   ```bash
   cd /path/to/chat-library-poc
   ./gradlew :shared:clean :shared:publishToMavenLocal
   ```

3. **Back in your Android app:**
   - File → Invalidate Caches / Restart
   - Sync Gradle again

## Step 7: Check Build Output

Look for errors in:
- **Build** tab in Android Studio
- **Gradle** sync output
- Check if you see: `--- com.example.chat_poc:shared:1.0.0 FAILED`

If you see "FAILED", the dependency isn't resolving. Check:
- `mavenLocal()` is in repositories
- Version matches what you published
- Library was published successfully

## Common Issues

**"Could not find com.example.chat_poc:shared:1.0.0"**
- `mavenLocal()` is missing from repositories
- Library wasn't published (run `publishToMavenLocal` again)
- Version mismatch

**"Unresolved reference: ChatPoc" (but dependency resolves)**
- Missing import: `import com.example.chat_poc.ChatPoc`
- Need to sync Gradle
- IDE cache issue (Invalidate Caches / Restart)

**"Unresolved reference: showBottomSheet"**
- Missing import: `import com.example.chat_poc.showBottomSheet`
- Or use: `ChatPoc.showBottomSheet(activity)` after importing `ChatPoc`
