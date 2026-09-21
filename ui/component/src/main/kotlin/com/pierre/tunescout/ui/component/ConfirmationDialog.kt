package com.pierre.tunescout.ui.component

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.window.Dialog
import com.pierre.tunescout.ui.theme.TuneScoutColors

@Composable
fun ConfirmationDialog(
    title: String,
    message: String,
    confirmLabel: String,
    cancelLabel: String,
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
) {
    Dialog(onDismissRequest = onCancel) {
        PromptCard(title = title) {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = TuneScoutColors.textSecondary,
                textAlign = TextAlign.Center,
            )
            PromptActionsRow(
                confirmLabel = confirmLabel,
                cancelLabel = cancelLabel,
                confirmColor = TuneScoutColors.error,
                onConfirm = onConfirm,
                onCancel = onCancel,
            )
        }
    }
}
