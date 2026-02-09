# Publishing the Chat Library

This document describes how to publish the library so Android and iOS apps can add it directly from GitHub (without cloning the repo).

---

## Overview

| Platform | Publish to | Consume via |
|----------|------------|--------------|
| **Android** | GitHub Packages (Maven) | Gradle dependency + repository URL |
| **iOS** | GitHub Releases (XCFramework zip) | Swift Package Manager or manual XCFramework |

---

## Where to find the published library

After you publish, here’s where it appears and how consumers use it:

### Android (GitHub Packages)

- **In the GitHub UI:** Your repo → **Packages** (right-hand side, or `https://github.com/orgs/YOUR_ORG/packages` / `https://github.com/YOUR_USER?tab=packages`). The package name is the **repository name** (e.g. `chat-library-poc-hosting-aws`).
- **Maven URL consumers use:**  
  `https://maven.pkg.github.com/YOUR_GITHUB_OWNER/chat-library-poc-hosting-aws`
- **Dependency coordinates:**  
  `com.example.chat_poc:shared:VERSION`  
  (e.g. `com.example.chat_poc:shared:1.0.0`).

### iOS (GitHub Releases)

- **In the GitHub UI:** Your repo → **Releases** → choose a release (e.g. `v1.0.0`) → **Assets**. The XCFramework zip (e.g. `ChatSDK.xcframework.zip`) is listed there.
- **Direct download URL (for docs or SPM):**  
  `https://github.com/YOUR_GITHUB_OWNER/chat-library-poc-hosting-aws/releases/download/v1.0.0/ChatSDK.xcframework.zip`  
  (replace `v1.0.0` with the release tag).

### Quick links (replace `YOUR_GITHUB_OWNER`)

| What | URL |
|------|-----|
| **Android – Maven repo** | `https://maven.pkg.github.com/YOUR_GITHUB_OWNER/chat-library-poc-hosting-aws` |
| **Android – view package in GitHub** | Repo page → **Packages** (right sidebar), or **Your profile** → **Packages** |
| **iOS – releases** | `https://github.com/YOUR_GITHUB_OWNER/chat-library-poc-hosting-aws/releases` |
| **iOS – zip for tag v1.0.0** | `https://github.com/YOUR_GITHUB_OWNER/chat-library-poc-hosting-aws/releases/download/v1.0.0/ChatSDK.xcframework.zip` |

---

## 1. Publish Android (GitHub Packages)

### 1.1 One-time setup

1. **GitHub Personal Access Token**
   - GitHub → Settings → Developer settings → Personal access tokens
   - Create a token with scope `write:packages` (and `read:packages` if you want to install from private packages).
   - Optionally add `repo` if you use it in CI.

2. **Repository URL**
   - Replace `YOUR_GITHUB_OWNER` in the publish URL with your GitHub username or org.
   - Default URL in the project: `https://maven.pkg.github.com/YOUR_GITHUB_OWNER/chat-library-poc-hosting-aws`
   - Or set it via property: `-PGITHUB_PACKAGES_URL=https://maven.pkg.github.com/OWNER/REPO`

### 1.2 Publish from your machine

```bash
# From the repo root
./gradlew :shared:publishAllPublicationsToGitHubPackagesRepository \
  -Pgpr.user=YOUR_GITHUB_USERNAME \
  -Pgpr.token=YOUR_GITHUB_TOKEN
```

Or use environment variables (handy in CI):

```bash
export GITHUB_ACTOR=YOUR_GITHUB_USERNAME
export GITHUB_TOKEN=YOUR_GITHUB_TOKEN
./gradlew :shared:publishAllPublicationsToGitHubPackagesRepository
```

Optional: set the library version with:

```bash
./gradlew :shared:publishAllPublicationsToGitHubPackagesRepository -PLIB_VERSION=1.0.0
```

**If you get "409 Conflict":** GitHub Packages does not allow overwriting a version. Publish with a new version, e.g. `-PLIB_VERSION=1.0.1`.

**If you see “Incompatible ABI version” or “KLIB resolver: Could not find … atomicfu …”:**

Stale Kotlin/Native metadata is in the build. Clean and publish again:

```bash
./gradlew clean
./gradlew :shared:publishAllPublicationsToGitHubPackagesRepository -Pgpr.user=… -Pgpr.token=…
```

The project uses Kotlin 2.1.0; if you previously built with another Kotlin version, a full clean removes the old klibs.

### 1.3 Publish via GitHub Actions (on tag push)

The workflow in `.github/workflows/publish.yml` runs on **push** of a tag `v*` (e.g. `v1.0.0`). It publishes Android to GitHub Packages and the iOS XCFramework to a GitHub Release. No extra publish step is needed; push a tag and the workflow runs. The built-in `GITHUB_TOKEN` is used for both GitHub Packages and the release (no AWS or PAT required).

### 1.4 Adding the library in an Android app (from GitHub Packages)

In the **app** (or root) `build.gradle.kts` of your Android project:

1. **Repository**
   - Add the GitHub Packages Maven repo (use the same owner/repo as where you published):

```kotlin
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven {
            url = uri("https://maven.pkg.github.com/YOUR_GITHUB_OWNER/chat-library-poc-hosting-aws")
            credentials {
                username = providers.gradleProperty("gpr.user").getOrElse(System.getenv("GITHUB_ACTOR") ?: "")
                password = providers.gradleProperty("gpr.token").getOrElse(System.getenv("GITHUB_TOKEN") ?: "")
            }
        }
    }
}
```

2. **Dependency**

```kotlin
dependencies {
    implementation("com.example.chat_poc:shared:1.0.0")
}
```

3. **Credentials**
   - For local builds: in `~/.gradle/gradle.properties` add:
     ```
     gpr.user=YOUR_GITHUB_USERNAME
     gpr.token=YOUR_GITHUB_TOKEN
     ```
   - Or pass via env: `GITHUB_ACTOR`, `GITHUB_TOKEN`.

4. **Usage**  
   See [androidApp/README.md](androidApp/README.md) for API usage (`ChatPoc.showBottomSheet(activity)`).

---

## 2. Publish iOS (XCFramework via GitHub Releases)

### 2.1 Build the XCFramework

On a Mac:

```bash
./gradlew :shared:assembleSharedReleaseXCFramework
```

Output: `shared/build/XCFrameworks/release/` (contains the framework, e.g. `ChatSDK.xcframework` or similar).

### 2.2 Publish via GitHub Release

1. **Zip the XCFramework**
   ```bash
   cd shared/build/XCFrameworks/release
   zip -r ChatSDK.xcframework.zip ChatSDK.xcframework
   ```

2. **Create a GitHub Release**
   - Repo → Releases → “Create a new release”
   - Tag: e.g. `v1.0.0`
   - Upload `ChatSDK.xcframework.zip` as an asset
   - Publish release

3. **Stable download URL**
   - Use the “Attached binary” URL, e.g.:
   - `https://github.com/YOUR_GITHUB_OWNER/chat-library-poc-hosting-aws/releases/download/v1.0.0/ChatSDK.xcframework.zip`

### 2.3 Adding the library in an iOS app

**Option A – Swift Package Manager (XCFramework from URL)**

1. In Xcode: File → Add Package Dependencies
2. Enter the repo URL: `https://github.com/YOUR_GITHUB_OWNER/chat-library-poc-hosting-aws`
3. If the repo has a `Package.swift` that points at the XCFramework zip URL, Xcode will use it. Otherwise use Option B.

**Option B – Manual XCFramework**

1. Download the zip from the release, e.g.  
   `https://github.com/YOUR_GITHUB_OWNER/chat-library-poc-hosting-aws/releases/download/v1.0.0/ChatSDK.xcframework.zip`
2. Unzip and add `ChatSDK.xcframework` to the app target (Frameworks, Libraries, and Embedded Content → Embed & Sign).
3. **Usage**  
   See [iosApp/README.md](iosApp/README.md) for `createBottomSheetViewController()` and `setBottomSheetDismissHandler`.

**Option C – Swift Package with binary target**

If you add a `Package.swift` to the repo that declares a binary target with the XCFramework zip URL, clients can add the package by repo URL and get the framework from the release. Example:

```swift
// Package.swift (at repo root)
let package = Package(
    name: "ChatSDK",
    platforms: [.iOS(.v14)],
    products: [
        .library(name: "ChatSDK", targets: ["ChatSDK"]),
    ],
    targets: [
        .binaryTarget(
            name: "ChatSDK",
            url: "https://github.com/YOUR_GITHUB_OWNER/chat-library-poc-hosting-aws/releases/download/v1.0.0/ChatSDK.xcframework.zip",
            checksum: "…"  // run: swift package compute-checksum ChatSDK.xcframework.zip
        ),
    ]
)
```

Then in Xcode: Add Package Dependencies → `https://github.com/YOUR_GITHUB_OWNER/chat-library-poc-hosting-aws`.

---

## 3. Checklist for “add from GitHub” in apps

- [ ] **Android:** Library published to GitHub Packages; app has the GitHub Packages repo and `implementation("com.example.chat_poc:shared:VERSION")`; credentials set via `gpr.user`/`gpr.token` or `GITHUB_ACTOR`/`GITHUB_TOKEN`.
- [ ] **iOS:** XCFramework built, zipped, and attached to a GitHub Release; app either uses SPM (Package.swift + binary target) or manual XCFramework from the release URL.

Replace `YOUR_GITHUB_OWNER` (and repo name if different) everywhere with your actual GitHub owner and repository.

---

## 4. Testing the hosted publish (from this repo, no local build)

Use this flow to test the **hosted** pipeline: push code → configure secrets → push a tag → workflow publishes **Android** to **GitHub Packages** and creates a **GitHub Release** with the iOS XCFramework. No local Gradle or Xcode build required.

### Step 1: Push your code

From the repo root:

```bash
git add .
git status   # confirm .github/workflows/publish.yml, shared/build.gradle.kts, Package.swift, etc.
git commit -m "Publish workflow: GitHub Packages + GitHub Release"
git push origin main   # or your branch name
```

The tag you push in Step 3 can be on any commit on the remote.

### Step 2: Configure GitHub secrets

In **this repo on GitHub**: **Settings → Secrets and variables → Actions**. The workflow uses the built-in **GITHUB_TOKEN** (no extra secret) for publishing to GitHub Packages and creating the release. For Connect config (start-chat API), add these **repository secrets** if the library needs them at build time:

| Secret | Description |
|--------|-------------|
| `API_GATEWAY` | Full start-chat API URL (see [ENV.md](ENV.md)). |
| `CONTACT_FLOW_ID` | Amazon Connect contact flow ID. |
| `INSTANCE_ID` | Connect instance ID. |
| `REGION` | AWS region (e.g. `ca-central-1`). |

Without these, the published library has no Connect config; host apps still get the chat UI.

### Step 3: Trigger the workflow with a tag

Create and push a tag (e.g. for version `1.0.0`). The workflow runs on **push** of any tag matching `v*`.

```bash
git tag v1.0.0
git push origin v1.0.0
```

### Step 4: Check the hosted result

1. **Actions:** In this repo, open **Actions** → select the **Publish** run for the tag (e.g. `v1.0.0`). Confirm all steps succeed.
2. **GitHub Packages:** In this repo → **Packages** (right sidebar). You should see the Maven package (e.g. `chat-library-poc-hosting-aws-hosting-aws` or the repo name) with version `1.0.0`.
3. **GitHub Release:** In this repo → **Releases**. There should be a release for `v1.0.0` with assets:
   - `ChatSDK.xcframework.zip`
   - `ChatSDK.xcframework.zip.sha256`

That’s the hosted test: **Android** from GitHub Packages, **iOS** from the release zip.

### Step 5: After the first successful run

- **Package.swift:** In this repo, set the tag in the binary URL (e.g. `v1.0.0`) and set `checksum` to the value in the release’s `ChatSDK.xcframework.zip.sha256` (or run `swift package compute-checksum` on the downloaded zip). Commit and push so SPM consumers can use the package.
- **Consumers:** Android apps add the GitHub Packages Maven repo and `implementation("com.example.chat_poc:shared:1.0.0")`; iOS apps add this repo as an SPM dependency or use the release zip URL.

### Optional: local dry-run (no tag, no GitHub)

To publish Android to GitHub Packages from your machine: set `GITHUB_ACTOR` (your GitHub username), `GITHUB_TOKEN` (PAT with `write:packages`), and `GITHUB_REPOSITORY` (e.g. `owner/repo`), then run:

```bash
./gradlew :shared:publishAndroidPublicationToGitHubPackagesRepository -PLIB_VERSION=1.0.0
```

For iOS: `./gradlew :shared:assembleSharedReleaseXCFramework`, then zip the XCFramework and create a release manually or push a tag to trigger the workflow.
