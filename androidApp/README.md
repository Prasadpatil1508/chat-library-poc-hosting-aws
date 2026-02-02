# Android – Chat Library Phase 1

This folder describes how to add the Chat Library to an Android app and show the bottom sheet when the user taps a button.

## Test locally (Maven Local) — quickest way

Publish the library to your local Maven repository (`~/.m2/repository`) and add it to your Android app.

### Step 1: Publish to Maven Local

From the **library repo root** (`chat-library-poc/`):

```bash
./gradlew :shared:publishToMavenLocal
```

This publishes all publications (including `shared-android`) to `~/.m2/repository/com/example/chat_poc/shared/...`.

### Step 2: Add to your Android app

In your **Android app project**:

1. **Add Maven Local repository** (in `settings.gradle.kts` or root `build.gradle.kts`):

```kotlin
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        mavenLocal()  // ← Add this
    }
}
```

Or if using repositories in root `build.gradle.kts`:

```kotlin
repositories {
    google()
    mavenCentral()
    mavenLocal()  // ← Add this
}
```

2. **Add the dependency** (in your app module's `build.gradle.kts`):

```kotlin
dependencies {
    implementation("com.example.chat_poc:shared:1.0.0")
}
```

Use the version from the library's `build.gradle.kts` (default is `1.0.0`, or set via `-PLIB_VERSION=...` when publishing).

3. **Sync and use** — see [Usage](#usage) below.

---

## Add the library from GitHub (for production)

If the library is published to **GitHub Packages** from this repo, add it as a Maven dependency.

### 1. Add the GitHub Packages repository

In your app’s **root** `settings.gradle.kts` (or where `dependencyResolutionManagement` is defined), add the GitHub Packages Maven URL. Replace `YOUR_GITHUB_OWNER` with the GitHub user or org that hosts the repo (e.g. `getitrent`):

```kotlin
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven {
            url = uri("https://maven.pkg.github.com/YOUR_GITHUB_OWNER/chat-library-poc")
            credentials {
                username = providers.gradleProperty("gpr.user").getOrElse(System.getenv("GITHUB_ACTOR") ?: "")
                password = providers.gradleProperty("gpr.token").getOrElse(System.getenv("GITHUB_TOKEN") ?: "")
            }
        }
    }
}
```

If you use `build.gradle.kts` at root and repositories are declared there:

```kotlin
repositories {
    google()
    mavenCentral()
    maven {
        url = uri("https://maven.pkg.github.com/YOUR_GITHUB_OWNER/chat-library-poc")
        credentials {
            username = project.findProperty("gpr.user")?.toString() ?: System.getenv("GITHUB_ACTOR") ?: ""
            password = project.findProperty("gpr.token")?.toString() ?: System.getenv("GITHUB_TOKEN") ?: ""
        }
    }
}
```

### 2. Add the dependency

In your **app** module’s `build.gradle.kts`:

```kotlin
dependencies {
    implementation("com.example.chat_poc:shared:1.0.0")
}
```

Use the version that was published (e.g. `1.0.0` or the tag without `v`, like `v1.0.0` → `1.0.0`).

### 3. Provide credentials

GitHub Packages needs a token with `read:packages` (and `write:packages` for publishing).

**Local development:** add to `~/.gradle/gradle.properties`:

```properties
gpr.user=YOUR_GITHUB_USERNAME
gpr.token=YOUR_GITHUB_PERSONAL_ACCESS_TOKEN
```

**CI:** set env vars `GITHUB_ACTOR` and `GITHUB_TOKEN` (or `gpr.user` / `gpr.token`).

---

## Add the library from AWS CodeArtifact (step-by-step)

If the library is published to **AWS CodeArtifact** (Maven), add it to your Android app as follows.

### Step 1: Get your CodeArtifact Maven URL

The URL format is:

`https://YOUR_DOMAIN-YOUR_ACCOUNT_ID.d.codeartifact.YOUR_REGION.amazonaws.com/maven/YOUR_REPO/`

Example: `https://my-domain-123456789012.d.codeartifact.us-east-1.amazonaws.com/maven/my-repo/`

You can copy this from **AWS Console → CodeArtifact → your domain → your Maven repository → View connection instructions → Maven**.

### Step 2: Add the CodeArtifact repository in your app

In your app’s **root** `settings.gradle.kts` (or where `dependencyResolutionManagement` is defined), add the CodeArtifact Maven repo. Replace the URL with your actual CodeArtifact Maven URL from Step 1:

```kotlin
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven {
            name = "AWSCodeArtifact"
            url = uri("https://YOUR_DOMAIN-YOUR_ACCOUNT_ID.d.codeartifact.YOUR_REGION.amazonaws.com/maven/YOUR_REPO/")
            credentials {
                username = "aws"
                password = providers.gradleProperty("codeartifact.token").getOrElse(System.getenv("CODEARTIFACT_AUTH_TOKEN") ?: "")
            }
        }
    }
}
```

If your project declares repositories in the **root** `build.gradle.kts` instead:

```kotlin
maven {
    name = "AWSCodeArtifact"
    url = uri("https://YOUR_DOMAIN-YOUR_ACCOUNT_ID.d.codeartifact.YOUR_REGION.amazonaws.com/maven/YOUR_REPO/")
    credentials {
        username = "aws"
        password = project.findProperty("codeartifact.token")?.toString() ?: System.getenv("CODEARTIFACT_AUTH_TOKEN") ?: ""
    }
}
```

### Step 3: Add the dependency

In your **app** module’s `build.gradle.kts` (e.g. `app/build.gradle.kts`):

```kotlin
dependencies {
    implementation("com.example.chat_poc:shared:1.0.1")
}
```

Use the version you published (e.g. `1.0.1` for release tag `v1.0.1`).

### Step 4: Provide the CodeArtifact auth token (local development)

CodeArtifact requires an auth token (valid ~12 hours). Run once per session (or add to your shell profile):

```bash
export CODEARTIFACT_AUTH_TOKEN=$(aws codeartifact get-authorization-token \
  --domain YOUR_DOMAIN \
  --domain-owner YOUR_ACCOUNT_ID \
  --query authorizationToken --output text)
```

Replace `YOUR_DOMAIN` and `YOUR_ACCOUNT_ID` with your CodeArtifact domain name and AWS account ID. Then run your app from the same terminal (e.g. `./gradlew :app:installDebug` or Android Studio Run).

**Alternative:** put the token in `~/.gradle/gradle.properties` (do not commit this file):

```properties
codeartifact.token=YOUR_TOKEN_HERE
```

Get the token with the same `aws codeartifact get-authorization-token` command and paste the output. Refresh the token when it expires (~12 hours).

### Step 5: Align Kotlin and Compose (required)

The library is built with **Kotlin 2.3.0** and **Compose 1.10**. Your app must match (see [LOCAL_TESTING.md](../LOCAL_TESTING.md) for details):

- **Kotlin:** `2.3.0` in your version catalog or root `build.gradle.kts`.
- **Compose BOM:** `2025.01.00` or newer (so Compose 1.10 runtime is used).

Sync Gradle and rebuild.

### Step 6: Use the library in your Activity

Your Activity must extend **`ComponentActivity`** (e.g. `AppCompatActivity`). When the user taps a button, call:

```kotlin
import com.example.chat_poc.ChatPoc
import com.example.chat_poc.showBottomSheet

// In your Activity (e.g. in a button click listener):
ChatPoc.showBottomSheet(this)
```

**Compose example:**

```kotlin
Button(onClick = { ChatPoc.showBottomSheet(this@MainActivity) }) {
    Text("Open Chat")
}
```

**View/XML example:**

```kotlin
findViewById<View>(R.id.button_open_chat).setOnClickListener {
    ChatPoc.showBottomSheet(this)
}
```

That’s it. Build and run; tapping the button should open the chat bottom sheet.

---

## Add the library locally (source or project)

### Option A: From the same repo or a local clone

1. In your **app** module’s `build.gradle.kts`:

```kotlin
dependencies {
    implementation(project(":shared"))
}
```

2. In the project that contains your app, **settings.gradle.kts** must include the shared module. If the library repo is next to your app repo:

```kotlin
include(":shared")
project(":shared").projectDir = file("../chat-library-poc/shared")
```

### Option B: From Maven Local (see "Test locally" section above)

Already covered in the "Test locally" section at the top of this file.

## Requirements

- Your **Activity** must be (or extend) **`ComponentActivity`** (e.g. `AppCompatActivity` or `ComponentActivity`). Activity Compose uses this by default.
- The library uses Jetpack Compose; your project does not need to be fully Compose-based.

## Usage

Import the library API and call `ChatPoc.showBottomSheet(activity)` when the user taps your button.

### Compose UI

```kotlin
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.chat_poc.ChatPoc
import com.example.chat_poc.showBottomSheet

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Button(onClick = { ChatPoc.showBottomSheet(this@MainActivity) }) {
                        Text("Open Chat Library")
                    }
                }
            }
        }
    }
}
```

### View / XML UI

```kotlin
import android.os.Bundle
import androidx.activity.ComponentActivity
import com.example.chat_poc.ChatPoc
import com.example.chat_poc.showBottomSheet

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        findViewById<View>(R.id.button_open_chat).setOnClickListener {
            ChatPoc.showBottomSheet(this)
        }
    }
}
```

Use the **extension** `showBottomSheet` on `ChatPoc` and pass your `ComponentActivity`:

```kotlin
import com.example.chat_poc.ChatPoc
import com.example.chat_poc.showBottomSheet

// When the user taps your button:
ChatPoc.showBottomSheet(activity)
```

## Markdown rendering (bot messages)

The library renders bot Markdown messages (headings, lists, tables, bold/italic, links) using **com.mikepenz:multiplatform-markdown-renderer** with Material 3, on both Android and iOS (Compose Multiplatform). The shared module exposes `ChatMessage.isMarkdown`; `ChatMessageContent` uses the same markdown renderer when `isMarkdown` is true. No extra setup is required when using the built-in bottom sheet.

## Phase 1 behavior

- Tapping **Open Chat Library** (or your equivalent button) shows a bottom sheet.
- The sheet displays the text: **"Hello from Chat Library"**.
- The user can dismiss by dragging down or tapping outside.
