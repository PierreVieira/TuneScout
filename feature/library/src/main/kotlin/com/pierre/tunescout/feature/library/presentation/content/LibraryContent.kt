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
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.pierre.tunescout.feature.library.R
import com.pierre.tunescout.feature.library.domain.model.LibraryViewMode
import com.pierre.tunescout.feature.library.presentation.component.LibraryFilterChipRow
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
import com.pierre.tunescout.ui.utils.scroll.hideableTopBar
import com.pierre.tunescout.ui.utils.scroll.hidesBarsOnScroll
import com.pierre.tunescout.ui.utils.semantics.screenPane

private val minCellWidth = 160.dp
private val fabSize = 56.dp
private val listBottomPadding = fabSize + TuneScoutSpacing.screen * 2

@Composable
fun LibraryContent(
    uiState: LibraryUiState,
    onEvent: (LibraryUiEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .screenPane(stringResource(R.string.library_title))
            .safeDrawingPadding()
            .hidesBarsOnScroll(),
        contentAlignment = Alignment.TopCenter,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(),
        ) {
            Header(
                uiState = uiState,
                onEvent = onEvent,
                modifier = Modifier.hideableTopBar(),
            )
            Box(modifier = Modifier.padding(horizontal = TuneScoutSpacing.screen)) {
                when (uiState.viewMode) {
                    LibraryViewMode.LIST -> LibraryList(items = uiState.filteredItems, onEvent = onEvent)
                    LibraryViewMode.GRID -> LibraryGrid(items = uiState.filteredItems, onEvent = onEvent)
                }
            }
        }
        CreatePlaylistButton(
            onClick = { onEvent(LibraryUiEvent.OnCreatePlaylistClicked) },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(TuneScoutSpacing.screen),
        )
    }
}

@Composable
private fun CreatePlaylistButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    FloatingActionButton(
        onClick = onClick,
        containerColor = TuneScoutColors.accentContainer,
        contentColor = TuneScoutColors.textPrimary,
        modifier = modifier,
    ) {
        Icon(
            imageVector = TuneScoutIcons.add,
            contentDescription = stringResource(R.string.library_create_playlist),
        )
    }
}

@Composable
private fun Header(
    uiState: LibraryUiState,
    onEvent: (LibraryUiEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        TitleRow(onEvent = onEvent)
        LibraryFilterChipRow(
            selected = uiState.filter,
            onFilterClick = { filter -> onEvent(LibraryUiEvent.OnFilterClicked(filter)) },
            modifier = Modifier.padding(
                horizontal = TuneScoutSpacing.large,
                vertical = TuneScoutSpacing.small,
            ),
        )
        SectionBar(viewMode = uiState.viewMode, onEvent = onEvent)
    }
}

@Composable
private fun TitleRow(onEvent: (LibraryUiEvent) -> Unit) {
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
            modifier = Modifier
                .weight(1f)
                .semantics { heading() },
        )
        TopBarAction(
            icon = TuneScoutIcons.search,
            contentDescription = stringResource(R.string.library_open_search),
            onClick = { onEvent(LibraryUiEvent.OnSearchClicked) },
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
            modifier = Modifier
                .weight(1f)
                .semantics { heading() },
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
        contentPadding = PaddingValues(top = TuneScoutSpacing.small, bottom = listBottomPadding),
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
        contentPadding = PaddingValues(top = TuneScoutSpacing.small, bottom = listBottomPadding),
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
