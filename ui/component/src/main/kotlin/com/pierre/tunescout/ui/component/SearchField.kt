package com.pierre.tunescout.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.pierre.tunescout.ui.theme.TuneScoutColors
import com.pierre.tunescout.ui.theme.TuneScoutSpacing

private val fieldMinHeight = 48.dp
private val fieldCornerRadius = 12.dp
private val leadingIconSize = 24.dp
private val clearButtonSize = 48.dp
private val clearIconSize = 20.dp

/**
 * The field is named after its [placeholder] whether or not anything is typed: the placeholder
 * itself leaves the screen with the first character, and would take the field's only label with it.
 * That is also why the placeholder and the leading icon are silent — each would repeat the name.
 *
 * The clear button is a full touch target, so the field gives up its end padding to it and stands as
 * tall as it does whether the button is there or not.
 */
@Composable
fun SearchField(
    query: String,
    placeholder: String,
    onQueryChange: (String) -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val textStyle = MaterialTheme.typography.bodyLarge.copy(color = TuneScoutColors.textPrimary)
    BasicTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = fieldMinHeight)
            .background(TuneScoutColors.surfaceSubtle, RoundedCornerShape(fieldCornerRadius))
            .padding(start = TuneScoutSpacing.medium)
            .semantics { contentDescription = placeholder },
        textStyle = textStyle,
        singleLine = true,
        cursorBrush = SolidColor(TuneScoutColors.textPrimary),
        keyboardOptions = KeyboardOptions(
            capitalization = KeyboardCapitalization.Sentences,
            imeAction = ImeAction.Search,
        ),
        keyboardActions = KeyboardActions(onSearch = { keyboardController?.hide() }),
        decorationBox = { innerTextField ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(TuneScoutSpacing.small),
            ) {
                Icon(
                    imageVector = TuneScoutIcons.search,
                    contentDescription = null,
                    tint = TuneScoutColors.elementSubtle,
                    modifier = Modifier.size(leadingIconSize),
                )
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.CenterStart,
                ) {
                    if (query.isEmpty()) {
                        Text(
                            text = placeholder,
                            style = MaterialTheme.typography.bodyLarge,
                            color = TuneScoutColors.textPlaceholder,
                            modifier = Modifier.clearAndSetSemantics {},
                        )
                    }
                    innerTextField()
                }
                if (query.isNotEmpty()) {
                    IconButton(
                        onClick = onClear,
                        modifier = Modifier.size(clearButtonSize),
                    ) {
                        Icon(
                            imageVector = TuneScoutIcons.clear,
                            contentDescription = stringResource(R.string.ui_clear_search),
                            tint = TuneScoutColors.textPlaceholder,
                            modifier = Modifier.size(clearIconSize),
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.width(TuneScoutSpacing.small))
                }
            }
        },
    )
}
