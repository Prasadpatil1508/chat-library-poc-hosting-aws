# iOS – Chat Library Phase 1

This folder describes how to add the Chat Library (ChatSDK XCFramework) to a Swift or SwiftUI app and show the bottom sheet when the user taps a button.

## Build the framework

From the project root:

```bash
./gradlew :shared:assembleReleaseXCFramework
```

The XCFramework is produced under `shared/build/XCFrameworks/release/` (e.g. `ChatSDK.xcframework`).

## Add the framework to your Xcode project

1. In Xcode, select your app target → **General** → **Frameworks, Libraries, and Embedded Content**.
2. Click **+** → **Add Other…** → **Add Files…** and select `ChatSDK.xcframework`.
3. Set **Embed** to **Embed & Sign** (or **Do Not Embed** if you link it differently).

## Requirements

- Your app must link and embed the `ChatSDK.xcframework` (see above).
- Present the view controller returned by the library as a modal or sheet.

## Usage

Import the framework and call `createBottomSheetViewController()` when the user taps your button. Present that view controller modally (e.g. as a sheet).

The Kotlin export is generated from the package and file name. Use the generated class `Com_example_chat_pocChatPoc_iosKt` and its function `createBottomSheetViewController()`. Use autocomplete or inspect the framework headers if the name differs in your build.

### SwiftUI

```swift
import SwiftUI
import ChatSDK

struct ContentView: View {
    @State private var showChat = false

    var body: some View {
        Button("Open Chat Library") {
            showChat = true
        }
        .sheet(isPresented: $showChat) {
            ChatSheetView()
        }
    }
}

struct ChatSheetView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        Com_example_chat_pocChatPoc_iosKt.createBottomSheetViewController()
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}
```

### UIKit

When using `present(_:animated:)`, set the dismiss handler so that when the user dismisses the bottom sheet from inside (drag handle / gesture), the view controller is closed:

```swift
import UIKit
import ChatSDK

class ViewController: UIViewController {
    @IBAction func openChatTapped(_ sender: Any) {
        let vc = Com_example_chat_pocChatPoc_iosKt.createBottomSheetViewController()
        Com_example_chat_pocChatPoc_iosKt.setBottomSheetDismissHandler { [weak vc] in
            vc?.dismiss(animated: true)
        }
        vc.modalPresentationStyle = .pageSheet
        present(vc, animated: true)
    }
}
```

With SwiftUI `.sheet`, the system handles dismiss when the user swipes; you don't need to call `setBottomSheetDismissHandler` unless you want the Compose sheet's internal dismiss to also close the presented VC.

## Phase 1 behavior

- Tapping **Open Chat Library** (or your equivalent button) presents a bottom sheet.
- The sheet displays the text: **"Hello from Chat Library"**.
- The user can dismiss by swiping down or using the sheet’s drag handle.
