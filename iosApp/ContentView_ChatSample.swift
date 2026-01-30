//
//  ContentView_ChatSample.swift
//  POCChatApp – sample ContentView using ChatSDK (mirrors Android usage)
//
//  Add ChatSDK.xcframework to your app target, then replace your ContentView with this
//  (or copy the chat-related parts into your own ContentView).
//

import SwiftUI
import ChatSDK

struct ContentView: View {
    @State private var showChat = false
    @State private var dataFromLib: String = ""
    @State private var callbackCount: Int = 0

    var body: some View {
        VStack(spacing: 20) {
            Text("Chat Library Demo")
                .font(.title)

            Button(action: openChat) {
                Text("Open Chat")
            }
            .buttonStyle(.borderedProminent)

            if !dataFromLib.isEmpty {
                Text("Last data from library: \(dataFromLib)")
                    .font(.caption)
                    .lineLimit(2)
            }
            if callbackCount > 0 {
                Text("Callbacks received: \(callbackCount)")
                    .font(.caption)
            }
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
        .sheet(isPresented: $showChat) {
            ChatSheetView(
                onDismissRequested: { showChat = false },
                onActionButtonClicked: {
                    // Handle action button tap (e.g. analytics, navigation)
                },
                onDataToHost: { data in
                    dataFromLib = data
                    callbackCount += 1
                }
            )
        }
    }

    private func openChat() {
        showChat = true
    }
}

/// Wraps the ChatSDK bottom sheet in a SwiftUI sheet.
struct ChatSheetView: UIViewControllerRepresentable {
    var onDismissRequested: () -> Void
    var onActionButtonClicked: (() -> Void)?
    var onDataToHost: ((String) -> Void)?

    func makeUIViewController(context: Context) -> UIViewController {
        let vc = ChatPoc_iosKt.createBottomSheetViewController(
            title: "My Chat",
            messages: ["Hello", "From host"],
            authToken: "Bearer xxx",
            onActionButtonClicked: onActionButtonClicked,
            onDataToHost: onDataToHost
        )
        ChatPoc_iosKt.setBottomSheetDismissHandler {
            onDismissRequested()
        }
        return vc
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}

#Preview {
    ContentView()
}
