package com.pierre.tunescout.feature.library.presentation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.pierre.tunescout.feature.library.R
import com.pierre.tunescout.feature.library.domain.model.LibraryViewMode
import com.pierre.tunescout.ui.component.TuneScoutIcons
import com.pierre.tunescout.ui.theme.TuneScoutColors

private const val INDICATOR_ALPHA = 0.25f
private val segmentSize = 48.dp
private val indicatorSize = 32.dp
private val trackInset = 6.dp
private val segmentIconSize = 18.dp

/**
 * Both modes are on screen with the current one filled, rather than one button showing the mode it
 * would switch to: a single icon never says which of the two you are looking at.
 *
 * Each segment is a full touch target, which is larger than the toggle should look: the track and
 * the indicator are drawn inset from it, so only the area that answers a finger grew.
 */
@Composable
internal fun LibraryViewModeToggle(
    viewMode: LibraryViewMode,
    onViewModeClick: (LibraryViewMode) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .padding(trackInset)
                .background(color = TuneScoutColors.surfaceSubtle, shape = CircleShape),
        )
        Row(modifier = Modifier.selectableGroup()) {
            LibraryViewMode.entries.forEach { mode ->
                SegmentButton(
                    icon = mode.icon,
                    contentDescription = stringResource(mode.contentDescriptionRes),
                    isSelected = mode == viewMode,
                    onClick = { onViewModeClick(mode) },
                )
            }
        }
    }
}

@Composable
private fun SegmentButton(
    icon: ImageVector,
    contentDescription: String,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(segmentSize)
            .clip(CircleShape)
            .selectable(selected = isSelected, role = Role.RadioButton, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(indicatorSize)
                .background(
                    color = if (isSelected) TuneScoutColors.accent.copy(alpha = INDICATOR_ALPHA) else Color.Transparent,
                    shape = CircleShape,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = if (isSelected) TuneScoutColors.accent else TuneScoutColors.textTertiary,
                modifier = Modifier.size(segmentIconSize),
            )
        }
    }
}

private val LibraryViewMode.icon: ImageVector
    get() = when (this) {
        LibraryViewMode.LIST -> TuneScoutIcons.viewList
        LibraryViewMode.GRID -> TuneScoutIcons.viewGrid
    }

private val LibraryViewMode.contentDescriptionRes: Int
    get() = when (this) {
        LibraryViewMode.LIST -> R.string.library_view_as_list
        LibraryViewMode.GRID -> R.string.library_view_as_grid
    }
