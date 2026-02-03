// Swift Package Manager: add this repo as a package dependency to get the ChatSDK XCFramework.
//
// Binary URL pattern: https://github.com/YOUR_ORG/YOUR_REPO/releases/download/${TAG}/ChatSDK.xcframework.zip
// After each release:
// 1. Update url with the tag (e.g. v1.0.0).
// 2. Run: swift package compute-checksum ChatSDK.xcframework.zip   (after downloading the zip from the release)
// 3. Update the checksum below.
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
            url: "https://github.com/Prasadpatil1508/chat-library-poc-hosting-aws/releases/download/v1.0.1/ChatSDK.xcframework.zip",
            checksum: "b20ee2eb01d581e660e5db24d3fb4431f616d75c2bed2844cc1eb13999f2b35a"
        ),
    ]
)
