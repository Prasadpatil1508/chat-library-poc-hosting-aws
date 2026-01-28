# Android – Chat Library Phase 1

This folder describes how to add the Chat Library to an Android app and show the bottom sheet when the user taps a button.

## Add the library to your Android project

### Option A: Local dependency (same repo or nearby)

If your app lives in the same repo or you have the `chat-library-poc` project on disk:

1. In your **app** module’s `build.gradle.kts`, add the `:shared` project:

```kotlin
dependencies {
    implementation(project(":shared"))
    // If your app is in a different repo, point to the shared path, e.g.:
    // implementation(project(":shared"))  // when shared is included in settings.gradle.kts
}
```

2. In `settings.gradle.kts` (root of the project that contains your app), include the shared module. For example, if the library is in a sibling directory:

```kotlin
include(":shared")
project(":shared").projectDir = file("../path/to/chat-library-poc/shared")
```

### Option B: Published artifact

If the library is published to a Maven repository (e.g. via your CI or local `publishToMavenLocal`):

```kotlin
repositories {
    mavenLocal()   // or your Maven URL
}

dependencies {
    implementation("com.example.chat_poc:shared:1.0.0")  // use your group/artifact/version
}
```

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

## Phase 1 behavior

- Tapping **Open Chat Library** (or your equivalent button) shows a bottom sheet.
- The sheet displays the text: **"Hello from Chat Library"**.
- The user can dismiss by dragging down or tapping outside.
