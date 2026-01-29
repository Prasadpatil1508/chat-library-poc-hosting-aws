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

### Config & callbacks (host ↔ library)

- **Host → library:** Pass [ChatLibraryConfig] (e.g. `authToken`, `displayTitle`, `displayMessages`) when showing the sheet. **Connect/start-chat config is inside the library** (from the library’s [local.properties](local.properties) at build time); see [local.properties.example](local.properties.example) and [ENV.md](ENV.md).
- **Library → host:** Implement [ChatLibraryCallbacks] (`onActionButtonClicked`, `onDataToHost`) to handle button clicks and receive data (e.g. start-chat token or errors).
- **Phase 1 – start-chat API:** The library reads API_GATEWAY, CONTACT_FLOW_ID, INSTANCE_ID, REGION from **its** `local.properties` and shows a "Fetch Connect token" button; it calls the start-chat API and returns the token via `onDataToHost("token:...")`.
- **Phase 2 – AWS Connect chat:** The library uses **ContactId, ParticipantId, ParticipantToken** from the start-chat response and **REGION** from config to call the **AWS Connect Participant Service** directly (no separate connection URL). One tap on "Fetch Connect token & connect to chat" fetches the token and connects to AWS Connect; see [ENV.md](ENV.md) and [amazon-connect-chat-ui-examples/mobileChatExamples](https://github.com/amazon-connect/amazon-connect-chat-ui-examples/tree/master/mobileChatExamples).
- All logic lives in **commonMain** (config, api, model, domain, connect, UI); Android/iOS only provide the HTTP/WebSocket engine and UI host.

### Requirements

- **Android**: `ComponentActivity` (e.g. from Activity Compose). The host app does not need to be fully Compose-based.
- **iOS**: Present the view controller from `createBottomSheetViewController()` as a modal or sheet.

## Project structure

- `shared/` – KMP library (common + android + ios).
- `androidApp/README.md` – Instructions for adding the library to an Android app.
- `iosApp/README.md` – Instructions for adding the library to an iOS app.

## Phase 2 – Connect chat (SOLID, KMP)

- **Abstractions:** [ConnectChatDetails] (participantToken, contactId, participantId), [ConnectChatSession] (connect, disconnect, sendMessage, onConnectionEstablished, onMessageReceived, etc.). Follows the [Amazon Connect mobile examples](https://github.com/amazon-connect/amazon-connect-chat-ui-examples/tree/master/mobileChatExamples).
- **Direct AWS:** [AwsParticipantConnectionApi] calls **CreateParticipantConnection** at `participant-connect.{region}.amazonaws.com/participant/connection` with header `X-Amz-Bearer: participantToken` (no SigV4). [ConnectSessionKtor] uses that plus the AWS **SendMessage** endpoint (`/participant/message` with connection token) and Ktor WebSocket for receiving.
- **Public API:** `fetchConnectChatDetails()` → [ConnectChatDetails]; `createConnectChatSession(config)` or `createConnectChatSessionOrNull()` → [ConnectChatSession]; then `session.connect(details)`.
- **UI:** "Fetch Connect token & connect to chat" fetches token and connects when config (API_GATEWAY, REGION, etc.) is set; after connect, messages list and send box.

## Tech stack

- Kotlin 2.0, Compose Multiplatform 1.6.10, Material3.
- Android: Jetpack Compose (via `ComposeView` in a `Dialog` for the library entry point).
- iOS: `ComposeUIViewController` + XCFramework for Swift/UIKit/SwiftUI.
