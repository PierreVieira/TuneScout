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
import com.pierre.tunescout.ui.component.StateMessage
import com.pierre.tunescout.ui.component.TopBarAction
import com.pierre.tunescout.ui.component.TuneScoutIcons
import com.pierre.tunescout.ui.theme.TuneScoutColors
import com.pierre.tunescout.ui.theme.TuneScoutSpacing
import com.pierre.tunescout.ui.utils.scroll.hideableTopBar
import com.pierre.tunescout.ui.utils.scroll.hidesBarsOnScroll
import com.pierre.tunescout.ui.utils.semantics.screenPane

private val minCellWidth = 160.dp
private val fabSize = 56.dp
private val fabListBottomPadding = fabSize + TuneScoutSpacing.screen * 2
private const val CONTENT_TYPE_ROW = "row"
private const val CONTENT_TYPE_CELL = "cell"

/**
 * The create-playlist action is a FAB on a single pane, where the list has the whole width to spare.
 * On a wide window the library sits in a narrower list pane beside a detail, so the action moves into
 * the top bar next to search instead of floating over the list.
 *
 * @param isTwoPane whether the window lays a pane beside the tabs, narrowing the library's own pane.
 */
@Composable
fun LibraryContent(
    uiState: LibraryUiState,
    isTwoPane: Boolean,
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
                isTwoPane = isTwoPane,
                onEvent = onEvent,
                modifier = Modifier.hideableTopBar(),
            )
            Box(
                modifier = Modifier.padding(horizontal = TuneScoutSpacing.screen),
                contentAlignment = Alignment.TopCenter,
            ) {
                when {
                    uiState.isDownloadedEmpty -> StateMessage(
                        title = stringResource(R.string.library_downloaded_empty_title),
                        description = stringResource(R.string.library_downloaded_empty_description),
                        isAnnounced = true,
                    )

                    uiState.viewMode == LibraryViewMode.LIST ->
                        LibraryList(uiState = uiState, isTwoPane = isTwoPane, onEvent = onEvent)

                    else -> LibraryGrid(uiState = uiState, isTwoPane = isTwoPane, onEvent = onEvent)
                }
            }
        }
        if (!isTwoPane) {
            CreatePlaylistButton(
                onClick = { onEvent(LibraryUiEvent.OnCreatePlaylistClicked) },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(TuneScoutSpacing.screen),
            )
        }
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
    isTwoPane: Boolean,
    onEvent: (LibraryUiEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        TitleRow(isTwoPane = isTwoPane, onEvent = onEvent)
        LibraryFilterChipRow(
            filters = uiState.visibleFilters,
            selected = uiState.filters,
            onFilterClick = { filter -> onEvent(LibraryUiEvent.OnFilterClicked(filter)) },
            onClearClick = { onEvent(LibraryUiEvent.OnClearFiltersClicked) },
            horizontalPadding = TuneScoutSpacing.large,
            modifier = Modifier.padding(vertical = TuneScoutSpacing.small),
        )
        SectionBar(viewMode = uiState.viewMode, onEvent = onEvent)
    }
}

@Composable
private fun TitleRow(
    isTwoPane: Boolean,
    onEvent: (LibraryUiEvent) -> Unit,
) {
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
        if (isTwoPane) {
            TopBarAction(
                icon = TuneScoutIcons.add,
                contentDescription = stringResource(R.string.library_create_playlist),
                onClick = { onEvent(LibraryUiEvent.OnCreatePlaylistClicked) },
            )
        }
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
    uiState: LibraryUiState,
    isTwoPane: Boolean,
    onEvent: (LibraryUiEvent) -> Unit,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(),
        contentPadding = PaddingValues(
            top = TuneScoutSpacing.small,
            bottom = if (isTwoPane) TuneScoutSpacing.extraLarge else fabListBottomPadding,
        ),
    ) {
        items(
            items = uiState.filteredItems,
            key = { item -> item.key.toString() },
            contentType = { CONTENT_TYPE_ROW },
        ) { item ->
            LibraryItemRow(
                item = item,
                name = libraryItemName(item),
                onClick = { onEvent(LibraryUiEvent.OnItemClicked(item)) },
                modifier = Modifier.animateItem(),
                isDownloaded = uiState.isDownloaded(item),
            )
        }
    }
}

@Composable
private fun LibraryGrid(
    uiState: LibraryUiState,
    isTwoPane: Boolean,
    onEvent: (LibraryUiEvent) -> Unit,
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = minCellWidth),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            top = TuneScoutSpacing.small,
            bottom = if (isTwoPane) TuneScoutSpacing.extraLarge else fabListBottomPadding,
        ),
        horizontalArrangement = Arrangement.spacedBy(TuneScoutSpacing.medium),
        verticalArrangement = Arrangement.spacedBy(TuneScoutSpacing.medium),
    ) {
        items(
            items = uiState.filteredItems,
            key = { item -> item.key.toString() },
            contentType = { CONTENT_TYPE_CELL },
        ) { item ->
            LibraryItemCell(
                item = item,
                name = libraryItemName(item),
                onClick = { onEvent(LibraryUiEvent.OnItemClicked(item)) },
                modifier = Modifier.animateItem(),
                isDownloaded = uiState.isDownloaded(item),
            )
        }
    }
}

@Composable
private fun libraryItemName(item: LibraryItemUiModel): String = item.getName(
    favoritesName = stringResource(R.string.library_favorites),
    downloadedSongsName = stringResource(R.string.library_downloaded_songs),
)
