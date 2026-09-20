package com.pierre.tunescout.feature.library.presentation.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.pierre.tunescout.feature.library.R
import com.pierre.tunescout.feature.library.domain.model.LibraryFilter
import com.pierre.tunescout.ui.theme.TuneScoutColors
import com.pierre.tunescout.ui.theme.TuneScoutSpacing

/**
 * Nothing selected means everything, and tapping the chip that is already on clears it — the way
 * the same bar behaves in Spotify's library.
 */
@Composable
internal fun LibraryFilterChipRow(
    selected: LibraryFilter?,
    onFilterClick: (LibraryFilter) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(TuneScoutSpacing.small),
    ) {
        LibraryFilter.entries.forEach { filter ->
            FilterChip(
                selected = filter == selected,
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
            )
        }
    }
}

private val LibraryFilter.labelRes: Int
    get() = when (this) {
        LibraryFilter.PLAYLISTS -> R.string.library_filter_playlists
        LibraryFilter.ALBUMS -> R.string.library_filter_albums
    }
