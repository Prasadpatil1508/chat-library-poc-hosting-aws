package com.example.chat_poc

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

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
    ModalBottomSheet(
        onDismissRequest = onDismiss,
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

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
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
