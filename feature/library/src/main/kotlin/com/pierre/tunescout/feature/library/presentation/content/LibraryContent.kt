package com.pierre.tunescout.feature.library.presentation.content

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.pierre.tunescout.feature.library.R
import com.pierre.tunescout.feature.library.domain.model.LibraryViewMode
import com.pierre.tunescout.feature.library.presentation.component.LibraryFilterChips
import com.pierre.tunescout.feature.library.presentation.component.LibraryItemCell
import com.pierre.tunescout.feature.library.presentation.component.LibraryItemRow
import com.pierre.tunescout.feature.library.presentation.component.LibraryViewModeToggle
import com.pierre.tunescout.feature.library.presentation.model.LibraryItemUiModel
import com.pierre.tunescout.feature.library.presentation.model.LibraryUiEvent
import com.pierre.tunescout.feature.library.presentation.model.LibraryUiState
import com.pierre.tunescout.feature.library.presentation.model.getName
import com.pierre.tunescout.ui.component.TopBarAction
import com.pierre.tunescout.ui.component.TuneScoutIcons
import com.pierre.tunescout.ui.theme.TuneScoutColors
import com.pierre.tunescout.ui.theme.TuneScoutSpacing

private val titleHeight = 48.dp
private val minCellWidth = 160.dp

@Composable
fun LibraryContent(
    uiState: LibraryUiState,
    onEvent: (LibraryUiEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .safeDrawingPadding(),
        contentAlignment = Alignment.TopCenter,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(),
        ) {
            Header(onEvent = onEvent)
            LibraryFilterChips(
                selected = uiState.filter,
                onFilterClick = { filter -> onEvent(LibraryUiEvent.OnFilterClicked(filter)) },
                modifier = Modifier.padding(
                    horizontal = TuneScoutSpacing.large,
                    vertical = TuneScoutSpacing.small,
                ),
            )
            SectionBar(viewMode = uiState.viewMode, onEvent = onEvent)
            Box(modifier = Modifier.padding(horizontal = TuneScoutSpacing.screen)) {
                when (uiState.viewMode) {
                    LibraryViewMode.LIST -> LibraryList(items = uiState.filteredItems, onEvent = onEvent)
                    LibraryViewMode.GRID -> LibraryGrid(items = uiState.filteredItems, onEvent = onEvent)
                }
            }
        }
    }
}

@Composable
private fun Header(onEvent: (LibraryUiEvent) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = TuneScoutSpacing.screen, start = TuneScoutSpacing.large, end = TuneScoutSpacing.small),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(R.string.library_title),
            style = MaterialTheme.typography.headlineMedium,
            color = TuneScoutColors.textPrimary,
            modifier = Modifier.weight(1f),
        )
        TopBarAction(
            icon = TuneScoutIcons.search,
            contentDescription = stringResource(R.string.library_open_search),
            onClick = { onEvent(LibraryUiEvent.OnSearchClicked) },
        )
        TopBarAction(
            icon = TuneScoutIcons.add,
            contentDescription = stringResource(R.string.library_create_playlist),
            onClick = { onEvent(LibraryUiEvent.OnCreatePlaylistClicked) },
        )
    }
}

@Composable
private fun SectionBar(
    viewMode: LibraryViewMode,
    onEvent: (LibraryUiEvent) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = TuneScoutSpacing.large),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(R.string.library_recents),
            style = MaterialTheme.typography.bodySmall,
            color = TuneScoutColors.textSecondary,
            modifier = Modifier.weight(1f),
        )
        LibraryViewModeToggle(
            viewMode = viewMode,
            onViewModeClick = { mode -> onEvent(LibraryUiEvent.OnViewModeSelected(mode)) },
        )
    }
}

@Composable
private fun LibraryList(
    items: List<LibraryItemUiModel>,
    onEvent: (LibraryUiEvent) -> Unit,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(),
        contentPadding = PaddingValues(top = TuneScoutSpacing.small, bottom = TuneScoutSpacing.extraLarge),
    ) {
        items(items = items, key = { item -> item.key.toString() }) { item ->
            LibraryItemRow(
                item = item,
                name = item.getName(stringResource(R.string.library_favorites)),
                onClick = { onEvent(LibraryUiEvent.OnItemClicked(item)) },
                modifier = Modifier.animateItem(),
            )
        }
    }
}

@Composable
private fun LibraryGrid(
    items: List<LibraryItemUiModel>,
    onEvent: (LibraryUiEvent) -> Unit,
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = minCellWidth),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(top = TuneScoutSpacing.small, bottom = TuneScoutSpacing.extraLarge),
        horizontalArrangement = Arrangement.spacedBy(TuneScoutSpacing.medium),
        verticalArrangement = Arrangement.spacedBy(TuneScoutSpacing.medium),
    ) {
        items(items = items, key = { item -> item.key.toString() }) { item ->
            LibraryItemCell(
                item = item,
                name = item.getName(stringResource(R.string.library_favorites)),
                onClick = { onEvent(LibraryUiEvent.OnItemClicked(item)) },
                modifier = Modifier.animateItem(),
            )
        }
    }
}
