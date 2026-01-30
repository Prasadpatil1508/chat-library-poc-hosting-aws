package com.example.chat_poc.ui.views

import com.example.chat_poc.ChatLibraryCallbacks
import com.example.chat_poc.ChatLibraryConfig
import com.example.chat_poc.config.LibraryConnectConfig
import com.example.chat_poc.connect.model.ChatMessage
import com.example.chat_poc.connect.model.ConnectChatDetails
import com.example.chat_poc.connect.model.MessageDirection
import com.example.chat_poc.connect.session.ConnectChatSession
import com.example.chat_poc.connect.session.createConnectChatSessionOrNull
import com.example.chat_poc.fetchConnectChatDetails
import com.example.chat_poc.storage.ChatSessionStorage
import com.example.chat_poc.ui.ChatBubble
import com.example.chat_poc.ui.ChatUi
import com.example.chat_poc.util.ChatLibraryLog
import com.example.chat_poc.util.UrlOpener
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Shared bottom sheet content (commonMain).
 * Aligns with [amazon-connect-chat-ui-examples](https://github.com/amazon-connect/amazon-connect-chat-ui-examples) views layer.
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
    var pendingIdCounter by remember { mutableStateOf(0) }

    /** When we receive OUTGOING (customer echo), remove the matching pending message so we show only one. */
    fun onMessageReceived(msg: ChatMessage) {
        if (msg.direction == MessageDirection.OUTGOING) {
            val trimmed = msg.text.trim()
            val idx = chatMessages.indexOfLast { it.isPending && it.text.trim() == trimmed }
            if (idx >= 0) chatMessages.removeAt(idx)
        }
        chatMessages.add(msg)
    }

    fun addPendingAndStartTimeout(text: String) {
        val pendingId = "pending-${pendingIdCounter++}"
        chatMessages.add(
            ChatMessage(
                id = pendingId,
                text = text,
                participantId = null,
                displayName = null,
                timestamp = "",
                direction = MessageDirection.OUTGOING,
                isPending = true,
            )
        )
        scope.launch {
            delay(ChatUi.sendEchoTimeoutMs)
            val idx = chatMessages.indexOfFirst { it.id == pendingId && it.isPending }
            if (idx >= 0) {
                chatMessages.removeAt(idx)
                chatMessages.add(
                    ChatMessage(
                        id = "failed-$pendingId",
                        text = "Failed to send",
                        participantId = null,
                        displayName = null,
                        timestamp = "",
                        direction = MessageDirection.OUTGOING,
                        sendFailed = true,
                    )
                )
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            scope.launch {
                chatSession?.disconnect()
                ChatSessionStorage.setParticipantToken(null)
            }
        }
    }

    ChatLibraryLog.d("BottomSheet", "Content composing: title=${config.displayTitle}, hasConnectConfig=$hasConnectConfig")
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = {
            ChatLibraryLog.d("BottomSheet", "Dismiss requested")
            ChatSessionStorage.clear()
            onDismiss()
        },
        sheetState = sheetState,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .padding(24.dp)
        ) {
            Text(
                text = config.displayTitle,
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            if (hasConnectConfig && !isConnected) {
                val storedToken = ChatSessionStorage.getParticipantToken()
                val storedContactId = ChatSessionStorage.getContactId()
                if (storedToken != null && storedToken.isNotBlank()) {
                    Button(
                        onClick = {
                            if (isConnecting) return@Button
                            ChatLibraryLog.d("BottomSheet", "Resume chat with stored token")
                            isConnecting = true
                            connectError = null
                            scope.launch {
                                val session = createConnectChatSessionOrNull()
                                if (session == null) {
                                    connectError = "Connect config not set"
                                    isConnecting = false
                                    return@launch
                                }
                                val details = ConnectChatDetails(
                                    participantToken = storedToken,
                                    contactId = storedContactId,
                                )
                                session.onConnectionEstablished = {
                                    isConnected = true
                                    isConnecting = false
                                    callbacks?.onDataToHost("connected")
                                }
                                session.onConnectionBroken = {
                                    isConnected = false
                                    isConnecting = false
                                    connectError = it?.message
                                    ChatSessionStorage.setParticipantToken(null)
                                }
                                session.onMessageReceived = { msg -> onMessageReceived(msg) }
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
                        Text(if (isConnecting) "Resuming…" else "Resume chat")
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
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
                            ChatSessionStorage.setParticipantToken(details.participantToken)
                            details.contactId?.let { ChatSessionStorage.setContactId(it) }
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
                                ChatSessionStorage.setParticipantToken(null)
                            }
                            session.onMessageReceived = { msg -> onMessageReceived(msg) }
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
                val listState = rememberLazyListState()
                LaunchedEffect(chatMessages.size) {
                    if (chatMessages.isNotEmpty()) {
                        listState.animateScrollToItem(chatMessages.size - 1)
                    }
                }
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(ChatUi.messageSpacing)
                ) {
                    items(chatMessages) { msg ->
                        val onQuickReply: (String) -> Unit = { value ->
                            addPendingAndStartTimeout(value)
                            scope.launch {
                                chatSession?.sendMessage(value)?.onFailure {
                                    val idx = chatMessages.indexOfLast { m -> m.isPending && m.text == value }
                                    if (idx >= 0) {
                                        chatMessages.removeAt(idx)
                                        chatMessages.add(
                                            ChatMessage(
                                                id = "failed-immediate",
                                                text = "Failed to send",
                                                participantId = null,
                                                displayName = null,
                                                timestamp = "",
                                                direction = MessageDirection.OUTGOING,
                                                sendFailed = true,
                                            )
                                        )
                                    }
                                }
                            }
                            Unit
                        }
                        val hasQuickReplies = msg.quickReplies != null && msg.quickReplies.isNotEmpty()
                        if (hasQuickReplies) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(ChatUi.quickReplyBlockPadding),
                            ) {
                                ChatMessageContent(
                                    msg = msg,
                                    modifier = Modifier.fillMaxWidth(),
                                    onQuickReplyClick = onQuickReply,
                                    onFlightActionClick = { href -> UrlOpener.openUrl(href) },
                                )
                            }
                        } else {
                            ChatBubble(
                                direction = msg.direction,
                                modifier = Modifier.fillMaxWidth(),
                                label = null,
                            ) {
                                ChatMessageContent(
                                    msg = msg,
                                    modifier = Modifier.fillMaxWidth(),
                                    onQuickReplyClick = onQuickReply,
                                    onFlightActionClick = { href -> UrlOpener.openUrl(href) },
                                )
                            }
                        }
                    }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = sendText,
                        onValueChange = { sendText = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Type a message") },
                        singleLine = true
                    )
                    Button(
                        onClick = {
                            val text = sendText.trim()
                            if (text.isEmpty()) return@Button
                            addPendingAndStartTimeout(text)
                            sendText = ""
                            scope.launch {
                                chatSession?.sendMessage(text)?.onFailure {
                                    val idx = chatMessages.indexOfLast { m -> m.isPending && m.text == text }
                                    if (idx >= 0) {
                                        chatMessages.removeAt(idx)
                                        chatMessages.add(
                                            ChatMessage(
                                                id = "failed-immediate",
                                                text = "Failed to send",
                                                participantId = null,
                                                displayName = null,
                                                timestamp = "",
                                                direction = MessageDirection.OUTGOING,
                                                sendFailed = true,
                                            )
                                        )
                                    }
                                }
                            }
                        },
                    ) {
                        Text("Send")
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
