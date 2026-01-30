//
//  ContentView.swift
//  POCChatApp – Chat library usage only
//

import SwiftUI
import ChatSDK

struct ContentView: View {
    @State private var showChat = false

    var body: some View {
        Button("Open Chat") {
            showChat = true
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
        .sheet(isPresented: $showChat) {
            ChatSheetView(
                onDismissRequested: { showChat = false },
                onActionButtonClicked: nil,
                onDataToHost: nil
            )
        }
    }
}

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
