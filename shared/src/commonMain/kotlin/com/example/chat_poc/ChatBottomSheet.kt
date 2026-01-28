package com.example.chat_poc

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Shared bottom sheet content for Phase 1.
 * Displays simple text when the library is invoked from Android or iOS.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatBottomSheetContent(onDismiss: () -> Unit) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
    ) {
        Text(
            text = "Hello from Chat Library",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
        )
    }
}
