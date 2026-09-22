package com.pierre.tunescout.feature.library.presentation.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp
import com.pierre.tunescout.feature.library.R
import com.pierre.tunescout.feature.library.domain.model.LibraryGridColumns
import com.pierre.tunescout.ui.theme.TuneScoutColors

private val targetSize = 48.dp
private val glyphSize = 18.dp
private val glyphGap = 2.dp
private const val GLYPH_CORNER_FRACTION = 0.25f

/**
 * One tap moves the grid to its next size, the way the player's repeat button moves through its
 * modes: the grid itself reflows under the finger, so the button needs no menu to preview what a
 * size looks like. Its glyph is the grid it stands for, [columns] tiles across and down.
 *
 * Three sizes are not a switch, so the label stays "Items per row" and the state says which of the
 * three the grid is at.
 */
@Composable
internal fun LibraryGridColumnsButton(
    columns: LibraryGridColumns,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val label = stringResource(R.string.library_grid_columns)
    val state = stringResource(R.string.library_grid_columns_state, columns.count)
    val tint = TuneScoutColors.textPrimary
    Box(
        modifier = modifier
            .size(targetSize)
            .clip(CircleShape)
            .clickable(role = Role.Button, onClick = onClick)
            .semantics {
                contentDescription = label
                stateDescription = state
            },
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.size(glyphSize)) {
            drawColumnsGlyph(count = columns.count, color = tint)
        }
    }
}

/** [count] rounded tiles across and down, with the gap between them held whatever their number. */
private fun DrawScope.drawColumnsGlyph(
    count: Int,
    color: Color,
) {
    val gap = glyphGap.toPx()
    val tile = (size.width - gap * (count - 1)) / count
    val cornerRadius = CornerRadius(tile * GLYPH_CORNER_FRACTION)
    repeat(count) { row ->
        repeat(count) { column ->
            drawRoundRect(
                color = color,
                topLeft = Offset(x = column * (tile + gap), y = row * (tile + gap)),
                size = Size(width = tile, height = tile),
                cornerRadius = cornerRadius,
            )
        }
    }
}
