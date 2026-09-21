package com.pierre.tunescout.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.pierre.tunescout.ui.theme.TuneScoutColors
import com.pierre.tunescout.ui.theme.TuneScoutSpacing

private val fieldHeight = 44.dp
private val fieldCornerRadius = 12.dp

/**
 * Asks for one line of text. Both places that name a playlist draw this: the library's create
 * dialog and the add-to-playlist sheet, which opens it over itself rather than navigating away.
 */
@Composable
fun NamePromptCard(
    title: String,
    placeholder: String,
    confirmLabel: String,
    cancelLabel: String,
    name: String,
    canConfirm: Boolean,
    onNameChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    PromptCard(title = title, modifier = modifier) {
        NameField(
            name = name,
            placeholder = placeholder,
            onNameChange = onNameChange,
            onConfirm = onConfirm,
        )
        PromptActionsRow(
            confirmLabel = confirmLabel,
            cancelLabel = cancelLabel,
            confirmColor = TuneScoutColors.textPrimary,
            onConfirm = onConfirm,
            onCancel = onCancel,
            isConfirmEnabled = canConfirm,
        )
    }
}

@Composable
private fun NameField(
    name: String,
    placeholder: String,
    onNameChange: (String) -> Unit,
    onConfirm: () -> Unit,
) {
    BasicTextField(
        value = name,
        onValueChange = onNameChange,
        modifier = Modifier
            .fillMaxWidth()
            .height(fieldHeight)
            .background(TuneScoutColors.surfaceSubtle, RoundedCornerShape(fieldCornerRadius))
            .padding(horizontal = TuneScoutSpacing.medium),
        textStyle = MaterialTheme.typography.bodyLarge.copy(color = TuneScoutColors.textPrimary),
        singleLine = true,
        cursorBrush = SolidColor(TuneScoutColors.textPrimary),
        keyboardOptions = KeyboardOptions(
            capitalization = KeyboardCapitalization.Sentences,
            imeAction = ImeAction.Done,
        ),
        keyboardActions = KeyboardActions(onDone = { onConfirm() }),
        decorationBox = { innerTextField ->
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.CenterStart,
            ) {
                if (name.isEmpty()) {
                    Text(
                        text = placeholder,
                        style = MaterialTheme.typography.bodyLarge,
                        color = TuneScoutColors.textPlaceholder,
                    )
                }
                innerTextField()
            }
        },
    )
}
