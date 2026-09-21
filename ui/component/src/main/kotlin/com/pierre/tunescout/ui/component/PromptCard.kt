package com.pierre.tunescout.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.pierre.tunescout.ui.theme.TuneScoutColors
import com.pierre.tunescout.ui.theme.TuneScoutSpacing

private val promptCornerRadius = 16.dp
private val promptMaxWidth = 560.dp

/**
 * The rounded sheet every prompt draws itself on: [title] over whatever [content] adds under it,
 * ending in a [PromptActionsRow].
 *
 * The margin and [promptMaxWidth] are the card's own: a dialog window wraps its content, so a card
 * that only filled the width would stretch that window to both edges of the screen.
 */
@Composable
internal fun PromptCard(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Surface(
        modifier = modifier
            .padding(horizontal = TuneScoutSpacing.large)
            .widthIn(max = promptMaxWidth)
            .fillMaxWidth(),
        shape = RoundedCornerShape(promptCornerRadius),
        color = TuneScoutColors.sheet,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = TuneScoutSpacing.large, vertical = TuneScoutSpacing.medium),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(TuneScoutSpacing.medium),
            content = {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = TuneScoutColors.textPrimary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.semantics { heading() },
                )
                content()
            },
        )
    }
}

/**
 * The cancel and confirm buttons a [PromptCard] ends with, side by side at its end.
 *
 * @param confirmColor the confirm label's color, which says what confirming does — the error color
 *  when the prompt is about to remove something.
 */
@Composable
internal fun PromptActionsRow(
    confirmLabel: String,
    cancelLabel: String,
    confirmColor: Color,
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
    isConfirmEnabled: Boolean = true,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End,
    ) {
        TextButton(
            onClick = onCancel,
            colors = ButtonDefaults.textButtonColors(contentColor = TuneScoutColors.textSecondary),
        ) {
            Text(text = cancelLabel)
        }
        TextButton(
            onClick = onConfirm,
            enabled = isConfirmEnabled,
            colors = ButtonDefaults.textButtonColors(contentColor = confirmColor),
        ) {
            Text(text = confirmLabel)
        }
    }
}
