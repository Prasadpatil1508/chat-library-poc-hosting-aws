// Swift Package Manager: add this repo as a package dependency to get the ChatSDK XCFramework.
//
// After you publish a GitHub Release with ChatSDK.xcframework.zip:
// 1. Set url to: https://github.com/YOUR_GITHUB_OWNER/chat-library-poc/releases/download/v1.0.0/ChatSDK.xcframework.zip
// 2. Run: swift package compute-checksum ChatSDK.xcframework.zip   (after downloading the zip)
// 3. Replace the checksum below.
//
// swift-tools-version: 5.9
import PackageDescription

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
            checksum: "REPLACE_AFTER_FIRST_RELEASE"
        ),
    ]
)
