package com.pierre.tunescout.feature.library.presentation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.pierre.tunescout.feature.library.R
import com.pierre.tunescout.feature.library.domain.model.LibraryViewMode
import com.pierre.tunescout.ui.component.TuneScoutIcons
import com.pierre.tunescout.ui.theme.TuneScoutColors

private const val INDICATOR_ALPHA = 0.25f
private val togglePadding = 2.dp
private val segmentSize = 32.dp
private val segmentIconSize = 18.dp

/**
 * Both modes are on screen with the current one filled, rather than one button showing the mode it
 * would switch to: a single icon never says which of the two you are looking at.
 */
@Composable
internal fun LibraryViewModeToggle(
    viewMode: LibraryViewMode,
    onViewModeClick: (LibraryViewMode) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .background(color = TuneScoutColors.surfaceSubtle, shape = CircleShape)
            .padding(togglePadding),
        horizontalArrangement = Arrangement.spacedBy(togglePadding),
    ) {
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
            .background(if (isSelected) TuneScoutColors.accent.copy(alpha = INDICATOR_ALPHA) else Color.Transparent)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = if (isSelected) TuneScoutColors.accent else TuneScoutColors.elementMuted,
            modifier = Modifier.size(segmentIconSize),
        )
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
