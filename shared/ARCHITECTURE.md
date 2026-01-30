# Chat Library Architecture

This document maps our **Kotlin Multiplatform (KMP) shared library** structure to the [amazon-connect-chat-ui-examples](https://github.com/amazon-connect/amazon-connect-chat-ui-examples) Android chat example and clarifies **AWS SDK vs Ktor**.

## Structure mapping (reference → our project)

| Reference (Android app) | Our shared module |
|------------------------|-------------------|
| **di/** (Hilt, SharedPreferences) | No DI in shared; host provides context. Storage is expect/actual (`storage/`). |
| **config/** (Config.kt) | `config/` — ConnectConfig, LibraryConnectConfig |
| **models/** | `model/` — StartChatRequest, StartChatResponse; `connect/model/` — ChatItem, BotPayloadModels, ConnectChatDetails, ChatMessage |
| **network/** (Retrofit ApiInterface, ApiService) | `api/` — StartChatApi, KtorStartChatApi (HTTP for start-chat) |
| **repository/** (ChatRepository, WebSocketManager) | `connect/api/` — ParticipantConnectionApi, AwsParticipantConnectionApi; `connect/parser/` — ConnectWebSocketParser; `connect/session/` — ConnectSessionKtor, ConnectChat |
| **domain/** (use cases) | `domain/` — FetchConnectTokenUseCase |
| **ui/theme/** (Color, Theme, Type) | `ui/theme/` — Color, Theme, Type |
| **utils/** (CommonUtils, ContentType) | `util/` — ChatLibraryLog, UrlOpener; `ui/MarkdownText.kt` (reference-style markdown) |
| **viewmodel/** (ChatViewModel, LiveData) | No ViewModel in shared; state in `ChatBottomSheetContent` (Compose) |
| **views/** (ChatComponents, QuickReplyContentView, …) | `ui/views/` — ChatMessageContent, FlightStatusContent, ChatBottomSheet |
| **SharedPreferences** (contactID, participantToken) | `storage/` — ChatSessionStorage (expect/actual: Android SharedPreferences, iOS UserDefaults) |

## Do we need the AWS SDK?

**No.** The reference Android app uses:

- `com.amazonaws:aws-android-sdk-connectparticipant` for CreateParticipantConnection, SendMessage, GetTranscript, DisconnectParticipant.

Our library uses **Ktor** in `commonMain` to call the **same AWS HTTP/WebSocket APIs**:

- **Start-chat**: HTTP POST to your API Gateway (which calls StartChatContact); we use `api/KtorStartChatApi`.
- **CreateParticipantConnection**: HTTP POST to `participant.connect.{region}.amazonaws.com/participant/connection` with `X-Amz-Bearer: participantToken` — see `connect/api/AwsParticipantConnectionApi.kt`.
- **WebSocket**: Connect to the URL from CreateParticipantConnection; subscribe to `aws/chat`; parse messages — see `connect/session/ConnectSessionKtor.kt` and `connect/parser/ConnectWebSocketParser.kt`.
- **SendMessage**: HTTP POST to `participant.connect.{region}.amazonaws.com/participant/message` with connection token.

So we achieve the same behavior **without** the AWS SDK, which keeps:

- One codebase for **Android and iOS** (the SDK is Android-only).
- No extra SDK size or version coupling in the shared module.

If a host app prefers the **official AWS SDK** on Android, it can use the SDK for start-chat and participant connection and still use our UI (`ui/views/`, `ui/theme/`) and models; our `connect/` layer would need to be replaced or bypassed by the host.

## Package layout (shared/src/commonMain/.../chat_poc)

```
api/           # Network: start-chat HTTP (StartChatApi, KtorStartChatApi)
config/        # ConnectConfig, LibraryConnectConfig
connect/       # AWS Connect chat (subfolders by use)
  api/         # Participant connection HTTP (ParticipantConnectionApi, AwsParticipantConnectionApi)
  model/       # DTOs (ConnectChatDetails, ConnectionDetails, ChatItem, BotPayloadModels, ChatMessage)
  parser/      # WebSocket parsing (AwsSocketEnvelope, AwsChatPayload, ConnectWebSocketParser)
  session/     # Session abstraction + Ktor impl (ConnectChatSession, ConnectSessionKtor, ConnectChat)
domain/        # Use cases (FetchConnectTokenUseCase)
model/         # DTOs (StartChatRequest, StartChatResponse)
storage/       # Session persistence (expect/actual)
ui/
  theme/       # Color, Theme, Type
  views/       # ChatBottomSheet, ChatMessageContent, FlightStatusContent
  MarkdownText.kt
markdown/      # MarkdownRenderer (expect/actual)
util/          # ChatLibraryLog, UrlOpener
# Root: ChatPoc, ChatLibraryConfig, ChatLibraryCallbacks, ConnectToken
```
