# Connect config: owned by the library (no host pass-through)

The library **contains** the Connect/start-chat API config. It is **not** passed by the host. Values are read from the **library repo’s** `local.properties` at **build time** and baked into the library.

## Keys in the library’s `local.properties`

Add these to **this repo’s** `local.properties` (see [local.properties.example](local.properties.example)):

| Key | Description |
|-----|-------------|
| `API_GATEWAY` | Full URL of the start-chat API (e.g. `https://xxx.execute-api.region.amazonaws.com/prod/start-chat`). |
| `CONTACT_FLOW_ID` | Amazon Connect contact flow ID. |
| `INSTANCE_ID` | Amazon Connect instance ID. |
| `REGION` | AWS region (e.g. `ca-central-1`). Used to call AWS Participant Service directly (participant-connect.{region}.amazonaws.com) for connect and send; no separate connection URL needed. |

## How it works

1. You add the keys to **the library project’s** `local.properties` (keep your existing `sdk.dir=...`).
2. When you **build the library** (`./gradlew :shared:assembleRelease` or `publishToMavenLocal`), the Gradle task `generateConnectConfig` runs and generates `LibraryConnectConfig.kt` from these values.
3. The library uses that config internally to call the start-chat API when the user taps “Fetch Connect token” in the bottom sheet. The host app does **not** pass any Connect config.

## Do not commit real values

`local.properties` is gitignored. Copy [local.properties.example](local.properties.example) and fill in your values locally. Never commit secrets.

## Host app

The **host app** (Android or iOS) does **not** need to pass Connect config. It only uses the library (e.g. `ChatPoc.showBottomSheet(activity, config, callbacks)` or `createBottomSheetViewController()`). The library makes the start-chat API call using its own config.
