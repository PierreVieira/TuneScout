package com.pierre.tunescout.ui.component

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.window.Dialog
import com.pierre.tunescout.ui.theme.TuneScoutColors

/**
 * Asks before a song the user already queued goes in a second time, which is rarely meant: a
 * repeated swipe or tap would otherwise stack copies of it without a word.
 */
@Composable
fun DuplicateInQueueDialog(
    songTitle: String,
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
) {
    DuplicateInQueuePromptDialog(
        message = stringResource(R.string.ui_duplicate_in_queue_message, songTitle),
        onConfirm = onConfirm,
        onCancel = onCancel,
    )
}

/**
 * Asks before a whole album or playlist is queued while [queuedCount] of its songs are already in
 * the queue, since confirming adds those again too.
 */
@Composable
fun DuplicatesInQueueDialog(
    queuedCount: Int,
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
) {
    DuplicateInQueuePromptDialog(
        message = pluralStringResource(R.plurals.ui_duplicates_in_queue_message, queuedCount, queuedCount),
        onConfirm = onConfirm,
        onCancel = onCancel,
    )
}

/**
 * Adding songs again is not destructive, so the confirm label keeps the text color rather than the
 * error one a [ConfirmationDialog] uses.
 */
@Composable
private fun DuplicateInQueuePromptDialog(
    message: String,
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
) {
    Dialog(onDismissRequest = onCancel) {
        PromptCard(title = stringResource(R.string.ui_duplicate_in_queue_title)) {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = TuneScoutColors.textSecondary,
                textAlign = TextAlign.Center,
            )
            PromptActionsRow(
                confirmLabel = stringResource(R.string.ui_duplicate_in_queue_confirm),
                cancelLabel = stringResource(R.string.ui_duplicate_in_queue_cancel),
                confirmColor = TuneScoutColors.textPrimary,
                onConfirm = onConfirm,
                onCancel = onCancel,
            )
        }
    }
}
