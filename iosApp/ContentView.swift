//
//  ContentView.swift
//  ChatPOCios
//
//  Created by Prasad Hindurao Patil on 04/02/26.
//

import SwiftUI
import ChatSDK

struct ContentView: View {
    @State private var showChat = false

    var body: some View {
        NavigationStack {
            VStack(spacing: 24) {
                Text("Chat Library Demo")
                    .font(.title)

                Button(action: { showChat = true }) {
                    Label("Open Chat", systemImage: "bubble.left.and.bubble.right")
                }
                .buttonStyle(.borderedProminent)
            }
            .frame(maxWidth: .infinity, maxHeight: .infinity)
            .sheet(isPresented: $showChat) {
                ChatSheetView(onDismissRequested: { showChat = false })
            }
        }
    }
}

/// Wraps the ChatSDK bottom sheet in a SwiftUI sheet.
struct ChatSheetView: UIViewControllerRepresentable {
    var onDismissRequested: () -> Void

    func makeUIViewController(context: Context) -> UIViewController {
        let vc = ChatPoc_iosKt.createBottomSheetViewController(
            title: "Chat",
            messages: [],
            authToken: "",
            onActionButtonClicked: nil,
            onDataToHost: nil
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
