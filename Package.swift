// swift-tools-version: 5.9
// Swift package for ChatSDK XCFramework. Published to AWS CodeArtifact Swift registry (same repo as Android Maven).
// Local: binary is at ./ChatSDK.xcframework. CI copies the built XCFramework to repo root before publishing.
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
            path: "./ChatSDK.xcframework"
        ),
    ]
)
