package com.example.chat_poc

import com.example.chat_poc.config.LibraryConnectConfig
import com.example.chat_poc.connect.ChatMessage
import com.example.chat_poc.connect.ConnectChatSession
import com.example.chat_poc.connect.MessageDirection
import com.example.chat_poc.connect.createConnectChatSessionOrNull
import com.example.chat_poc.util.ChatLibraryLog
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

/**
 * Shared bottom sheet content (commonMain).
 *
 * Displays [config] (title + messages from host or defaults).
 * Invokes [callbacks] when the user taps the action button or when the library sends data.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatBottomSheetContent(
    config: ChatLibraryConfig,
    callbacks: ChatLibraryCallbacks?,
    onDismiss: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    val hasConnectConfig = LibraryConnectConfig.get()?.isValid() == true
    var isConnecting by remember { mutableStateOf(false) }
    var isConnected by remember { mutableStateOf(false) }
    var connectError by remember { mutableStateOf<String?>(null) }
    var chatSession by remember { mutableStateOf<ConnectChatSession?>(null) }
    val chatMessages = remember { mutableStateListOf<ChatMessage>() }
    var sendText by remember { mutableStateOf("") }

    DisposableEffect(Unit) {
        onDispose {
            scope.launch {
                chatSession?.disconnect()
            }
        }
    }

    ChatLibraryLog.d("BottomSheet", "Content composing: title=${config.displayTitle}, hasConnectConfig=$hasConnectConfig")
    ModalBottomSheet(
        onDismissRequest = {
            ChatLibraryLog.d("BottomSheet", "Dismiss requested")
            onDismiss()
        },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
        ) {
            Text(
                text = config.displayTitle,
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Text(
                text = "Drag up to expand. Data from host is shown below.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            Text(
                text = "Messages:",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            config.displayMessages.forEach { message ->
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp, horizontal = 4.dp)
                )
            }

            if (hasConnectConfig && !isConnected) {
                Button(
                    onClick = {
                        if (isConnecting) return@Button
                        ChatLibraryLog.d("BottomSheet", "Fetch Connect token tapped")
                        isConnecting = true
                        connectError = null
                        scope.launch {
                            val details = fetchConnectChatDetails().getOrElse {
                                connectError = it.message
                                isConnecting = false
                                callbacks?.onDataToHost("error:${it.message}")
                                return@launch
                            }
                            callbacks?.onDataToHost("token:${details.participantToken}")
                            val session = createConnectChatSessionOrNull()
                            if (session == null) {
                                connectError = "Connect config not set (check local.properties: API_GATEWAY, REGION, etc.)"
                                isConnecting = false
                                return@launch
                            }
                            session.onConnectionEstablished = {
                                isConnected = true
                                isConnecting = false
                                callbacks?.onDataToHost("connected")
                            }
                            session.onConnectionBroken = {
                                isConnected = false
                                isConnecting = false
                                connectError = it?.message
                            }
                            session.onMessageReceived = { msg ->
                                chatMessages.add(msg)
                            }
                            chatSession = session
                            session.connect(details)
                                .onFailure {
                                    connectError = it.message
                                    isConnecting = false
                                }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isConnecting
                ) {
                    Text(
                        if (isConnecting) "Fetching & connecting…"
                        else "Fetch Connect token & connect to chat"
                    )
                }
                connectError?.let { err ->
                    Text(
                        text = "Error: $err",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            if (isConnected && chatSession != null) {
                Text(
                    text = "Connected. Messages:",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                LazyColumn(
                    modifier = Modifier.height(200.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(chatMessages) { msg ->
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = msg.displayName ?: msg.participantId ?: "?",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            ChatMessageContent(
                                msg = msg,
                                modifier = Modifier.fillMaxWidth(),
                                onQuickReplyClick = { value ->
                                    scope.launch {
                                        chatSession?.sendMessage(value)?.onSuccess {
                                            chatMessages.add(
                                                ChatMessage(
                                                    id = "qr",
                                                    text = value,
                                                    participantId = null,
                                                    displayName = "You",
                                                    timestamp = "",
                                                    direction = MessageDirection.OUTGOING,
                                                )
                                            )
                                        }
                                    }
                                },
                            )
                        }
                    }
                }
                OutlinedTextField(
                    value = sendText,
                    onValueChange = { sendText = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Type a message") },
                    singleLine = true
                )
                Button(
                    onClick = {
                        val text = sendText.trim()
                        if (text.isEmpty()) return@Button
                        scope.launch {
                            chatSession?.sendMessage(text)?.onSuccess {
                                chatMessages.add(ChatMessage(id = "local", text = text, participantId = null, displayName = "You", timestamp = "", direction = MessageDirection.OUTGOING))
                                sendText = ""
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Send")
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            Button(
                onClick = {
                    ChatLibraryLog.d("BottomSheet", "Action (notify host) tapped")
                    callbacks?.onActionButtonClicked()
                    callbacks?.onDataToHost("action_button_clicked")
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Action (notify host)")
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
