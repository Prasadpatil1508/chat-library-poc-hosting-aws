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

- **In the GitHub UI:** Your repo → **Packages** (right-hand side, or `https://github.com/orgs/YOUR_ORG/packages` / `https://github.com/YOUR_USER?tab=packages`). The package name is the **repository name** (e.g. `chat-library-poc`).
- **Maven URL consumers use:**  
  `https://maven.pkg.github.com/YOUR_GITHUB_OWNER/chat-library-poc`
- **Dependency coordinates:**  
  `com.example.chat_poc:shared:VERSION`  
  (e.g. `com.example.chat_poc:shared:1.0.0`).

### iOS (GitHub Releases)

- **In the GitHub UI:** Your repo → **Releases** → choose a release (e.g. `v1.0.0`) → **Assets**. The XCFramework zip (e.g. `ChatSDK.xcframework.zip`) is listed there.
- **Direct download URL (for docs or SPM):**  
  `https://github.com/YOUR_GITHUB_OWNER/chat-library-poc/releases/download/v1.0.0/ChatSDK.xcframework.zip`  
  (replace `v1.0.0` with the release tag).

### Quick links (replace `YOUR_GITHUB_OWNER`)

| What | URL |
|------|-----|
| **Android – Maven repo** | `https://maven.pkg.github.com/YOUR_GITHUB_OWNER/chat-library-poc` |
| **Android – view package in GitHub** | Repo page → **Packages** (right sidebar), or **Your profile** → **Packages** |
| **iOS – releases** | `https://github.com/YOUR_GITHUB_OWNER/chat-library-poc/releases` |
| **iOS – zip for tag v1.0.0** | `https://github.com/YOUR_GITHUB_OWNER/chat-library-poc/releases/download/v1.0.0/ChatSDK.xcframework.zip` |

---

## 1. Publish Android (GitHub Packages)

### 1.1 One-time setup

1. **GitHub Personal Access Token**
   - GitHub → Settings → Developer settings → Personal access tokens
   - Create a token with scope `write:packages` (and `read:packages` if you want to install from private packages).
   - Optionally add `repo` if you use it in CI.

2. **Repository URL**
   - Replace `YOUR_GITHUB_OWNER` in the publish URL with your GitHub username or org.
   - Default URL in the project: `https://maven.pkg.github.com/YOUR_GITHUB_OWNER/chat-library-poc`
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

### 1.3 Publish via GitHub Actions (on release)

A workflow can run publish when you create a GitHub Release. Example in `.github/workflows/publish.yml`:

```yaml
name: Publish
on:
  release:
    types: [published]
jobs:
  publish-android:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with:
          distribution: 'temurin'
          java-version: '17'
      - name: Publish to GitHub Packages
        env:
          GITHUB_TOKEN: ${{ secrets.GITHUB_TOKEN }}
        run: |
          ./gradlew :shared:publishAllPublicationsToGitHubPackagesRepository \
            -Pgpr.user=${{ github.repository_owner }} \
            -Pgpr.token=${{ secrets.GITHUB_TOKEN }} \
            -PLIB_VERSION=${GITHUB_REF#refs/tags/}
```

Create a release (e.g. tag `v1.0.0`) and the workflow will publish.

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
            url = uri("https://maven.pkg.github.com/YOUR_GITHUB_OWNER/chat-library-poc")
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
./gradlew :shared:assembleReleaseXCFramework
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
   - `https://github.com/YOUR_GITHUB_OWNER/chat-library-poc/releases/download/v1.0.0/ChatSDK.xcframework.zip`

### 2.3 Adding the library in an iOS app

**Option A – Swift Package Manager (XCFramework from URL)**

1. In Xcode: File → Add Package Dependencies
2. Enter the repo URL: `https://github.com/YOUR_GITHUB_OWNER/chat-library-poc`
3. If the repo has a `Package.swift` that points at the XCFramework zip URL, Xcode will use it. Otherwise use Option B.

**Option B – Manual XCFramework**

1. Download the zip from the release, e.g.  
   `https://github.com/YOUR_GITHUB_OWNER/chat-library-poc/releases/download/v1.0.0/ChatSDK.xcframework.zip`
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
            url: "https://github.com/YOUR_GITHUB_OWNER/chat-library-poc/releases/download/v1.0.0/ChatSDK.xcframework.zip",
            checksum: "…"  // run: swift package compute-checksum ChatSDK.xcframework.zip
        ),
    ]
)
```

Then in Xcode: Add Package Dependencies → `https://github.com/YOUR_GITHUB_OWNER/chat-library-poc`.

---

## 3. Checklist for “add from GitHub” in apps

- [ ] **Android:** Library published to GitHub Packages; app has the GitHub Packages repo and `implementation("com.example.chat_poc:shared:VERSION")`; credentials set via `gpr.user`/`gpr.token` or `GITHUB_ACTOR`/`GITHUB_TOKEN`.
- [ ] **iOS:** XCFramework built, zipped, and attached to a GitHub Release; app either uses SPM (Package.swift + binary target) or manual XCFramework from the release URL.

Replace `YOUR_GITHUB_OWNER` (and repo name if different) everywhere with your actual GitHub owner and repository.
