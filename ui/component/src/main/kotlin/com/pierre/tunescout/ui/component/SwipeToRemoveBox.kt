package com.pierre.tunescout.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.pierre.tunescout.ui.theme.TuneScoutColors
import com.pierre.tunescout.ui.theme.TuneScoutSpacing

private val rowCornerRadius = 8.dp
private val removeIconSize = 20.dp

@Composable
fun SwipeToRemoveBox(
    onRemove: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val state = rememberSwipeToDismissBoxState()
    SwipeToDismissBox(
        state = state,
        backgroundContent = {
            val direction = state.dismissDirection
            if (direction != SwipeToDismissBoxValue.Settled) RemoveBackground(direction = direction)
        },
        modifier = modifier.clip(RoundedCornerShape(rowCornerRadius)),
        onDismiss = { onRemove() },
    ) {
        Box(modifier = Modifier.background(TuneScoutColors.background)) {
            content()
        }
    }
}

@Composable
private fun RemoveBackground(direction: SwipeToDismissBoxValue) {
    val alignment = when (direction) {
        SwipeToDismissBoxValue.StartToEnd -> Alignment.CenterStart
        else -> Alignment.CenterEnd
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(TuneScoutColors.error)
            .padding(horizontal = TuneScoutSpacing.large),
        contentAlignment = alignment,
    ) {
        Icon(
            imageVector = TuneScoutIcons.delete,
            contentDescription = null,
            tint = TuneScoutColors.textPrimary,
            modifier = Modifier.size(removeIconSize),
        )
    }
}
