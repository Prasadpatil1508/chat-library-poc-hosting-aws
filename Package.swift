// swift-tools-version: 5.9
// Swift Package Manager: add this repo as a package dependency to get the ChatSDK XCFramework.
// Binary URL pattern: https://github.com/YOUR_ORG/YOUR_REPO/releases/download/${TAG}/ChatSDK.xcframework.zip
// After each release: update url tag and checksum (swift package compute-checksum ChatSDK.xcframework.zip).
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
            url: "https://github.com/Prasadpatil1508/chat-library-poc-hosting-aws/releases/download/v1.0.18/ChatSDK.xcframework.zip",
            checksum: "029bea290e9e20df6fdc760b5878163200fd24db344661a0d61137904c991b99"
        ),
    ]
)
