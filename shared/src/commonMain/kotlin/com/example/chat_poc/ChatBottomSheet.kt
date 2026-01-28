package com.example.chat_poc

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Shared bottom sheet content for Phase 1.
 * Displays simple text and dummy data when the library is invoked from Android or iOS.
 * Users can drag the bottom sheet up to expand it and see more content.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatBottomSheetContent(onDismiss: () -> Unit) {
    // ModalBottomSheet is draggable by default; no sheetState needed for compatibility
    // with different host app Material3/Compose versions.
    ModalBottomSheet(
        onDismissRequest = onDismiss,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
        ) {
            // Header
            Text(
                text = "Hello from Chat Library",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            Text(
                text = "Drag up to expand and see more content",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 24.dp)
            )
            
            // Dummy data list
            Text(
                text = "Sample Messages:",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            
            // Generate dummy chat messages
            val dummyMessages = listOf(
                "Message 1: Welcome to the Chat Library!",
                "Message 2: This is a sample message to demonstrate the expandable bottom sheet.",
                "Message 3: You can drag the sheet up to see more content.",
                "Message 4: The sheet will expand smoothly as you drag.",
                "Message 5: Try dragging it all the way up to see all messages.",
                "Message 6: This is another sample message with some text.",
                "Message 7: The bottom sheet supports scrolling when expanded.",
                "Message 8: You can add more content here as needed.",
                "Message 9: The Material3 ModalBottomSheet provides smooth animations.",
                "Message 10: This is the last sample message in the list."
            )
            
            dummyMessages.forEach { message ->
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp, horizontal = 4.dp)
                )
            }
            
            // Add some padding at the bottom for better scrolling
            Text(
                text = "",
                modifier = Modifier.padding(bottom = 24.dp)
            )
        }
    }
}
