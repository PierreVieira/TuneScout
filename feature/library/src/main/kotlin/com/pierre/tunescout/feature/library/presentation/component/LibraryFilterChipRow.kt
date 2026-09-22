package com.pierre.tunescout.feature.library.presentation.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import com.pierre.tunescout.feature.library.R
import com.pierre.tunescout.feature.library.domain.model.LibraryFilter
import com.pierre.tunescout.ui.component.TuneScoutIcons
import com.pierre.tunescout.ui.theme.TuneScoutColors
import com.pierre.tunescout.ui.theme.TuneScoutSpacing

private const val CLEAR_KEY = "clear"
private const val CONTENT_TYPE_CLEAR = "clear"
private const val CONTENT_TYPE_FILTER = "filter"

/**
 * Nothing selected means everything, and tapping a chip that is on turns it off — the way the same
 * bar behaves in Spotify's library. Once a chip is on, a round button ahead of the chips turns them
 * all off at once, and the chips that are on move to the front.
 *
 * @param filters the chips to draw, in the order to draw them.
 * @param selected the chips that are on.
 * @param horizontalPadding the room before the first chip and after the last, which the chips
 * scroll through rather than being cut at.
 */
@Composable
internal fun LibraryFilterChipRow(
    filters: List<LibraryFilter>,
    selected: Set<LibraryFilter>,
    onFilterClick: (LibraryFilter) -> Unit,
    onClearClick: () -> Unit,
    horizontalPadding: Dp,
    modifier: Modifier = Modifier,
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = horizontalPadding),
        horizontalArrangement = Arrangement.spacedBy(TuneScoutSpacing.small),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (selected.isNotEmpty()) {
            item(key = CLEAR_KEY, contentType = CONTENT_TYPE_CLEAR) {
                ClearFiltersButton(onClick = onClearClick, modifier = Modifier.animateItem())
            }
        }
        items(items = filters, key = { filter -> filter.name }, contentType = { CONTENT_TYPE_FILTER }) { filter ->
            FilterChip(
                selected = filter in selected,
                onClick = { onFilterClick(filter) },
                label = { Text(text = stringResource(filter.labelRes)) },
                shape = CircleShape,
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = TuneScoutColors.surfaceSubtle,
                    labelColor = TuneScoutColors.textPrimary,
                    selectedContainerColor = TuneScoutColors.accentContainer,
                    selectedLabelColor = TuneScoutColors.textPrimary,
                ),
                border = null,
                modifier = Modifier.animateItem(),
            )
        }
    }
}

/**
 * Drawn as round as a chip is tall. The surface keeps its 48dp target around it, the way a chip
 * does, however small it is drawn.
 */
@Composable
private fun ClearFiltersButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = TuneScoutColors.surfaceSubtle,
        contentColor = TuneScoutColors.textPrimary,
        modifier = modifier,
    ) {
        Box(
            modifier = Modifier.size(FilterChipDefaults.Height),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = TuneScoutIcons.clear,
                contentDescription = stringResource(R.string.library_clear_filters),
                modifier = Modifier.size(FilterChipDefaults.IconSize),
            )
        }
    }
}

private val LibraryFilter.labelRes: Int
    get() = when (this) {
        LibraryFilter.PLAYLISTS -> R.string.library_filter_playlists
        LibraryFilter.ALBUMS -> R.string.library_filter_albums
        LibraryFilter.DOWNLOADED -> R.string.library_filter_downloaded
    }
