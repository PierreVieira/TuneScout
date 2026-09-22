package com.pierre.tunescout.feature.library.presentation.content

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
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import com.pierre.tunescout.feature.library.R
import com.pierre.tunescout.feature.library.presentation.component.LibraryItemRow
import com.pierre.tunescout.feature.library.presentation.model.LibraryItemUiModel
import com.pierre.tunescout.feature.library.presentation.model.LibrarySearchUiEvent
import com.pierre.tunescout.feature.library.presentation.model.LibrarySearchUiState
import com.pierre.tunescout.feature.library.presentation.model.getName
import com.pierre.tunescout.feature.library.presentation.model.isMatching
import com.pierre.tunescout.ui.component.SearchField
import com.pierre.tunescout.ui.component.SongRowAction
import com.pierre.tunescout.ui.component.StateMessage
import com.pierre.tunescout.ui.component.TopBarAction
import com.pierre.tunescout.ui.component.TuneScoutIcons
import com.pierre.tunescout.ui.theme.TuneScoutColors
import com.pierre.tunescout.ui.theme.TuneScoutSpacing
import com.pierre.tunescout.ui.utils.semantics.screenPane
import com.pierre.tunescout.ui.component.R as ComponentR

private const val CONTENT_TYPE_ITEM = "item"

@Composable
fun LibrarySearchContent(
    uiState: LibrarySearchUiState,
    onEvent: (LibrarySearchUiEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val favoritesName = stringResource(R.string.library_favorites)
    val downloadedSongsName = stringResource(R.string.library_downloaded_songs)
    val results = remember(uiState.query, uiState.items, favoritesName, downloadedSongsName) {
        uiState.items.filter { item ->
            item.isMatching(
                query = uiState.query,
                favoritesName = favoritesName,
                downloadedSongsName = downloadedSongsName,
            )
        }
    }
    Box(
        modifier = modifier
            .fillMaxSize()
            .screenPane(stringResource(R.string.library_open_search))
            .safeDrawingPadding(),
        contentAlignment = Alignment.TopCenter,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(),
        ) {
            SearchBar(uiState = uiState, onEvent = onEvent)
            Box(modifier = Modifier.padding(horizontal = TuneScoutSpacing.screen)) {
                if (uiState.isSearching) {
                    LibrarySearchResultsList(items = results, favoritesName = favoritesName, onEvent = onEvent)
                } else {
                    RecentSearchesList(
                        items = uiState.recentSearches,
                        favoritesName = favoritesName,
                        onEvent = onEvent,
                    )
                }
            }
        }
    }
}

@Composable
private fun SearchBar(
    uiState: LibrarySearchUiState,
    onEvent: (LibrarySearchUiEvent) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(end = TuneScoutSpacing.screen, top = TuneScoutSpacing.small, bottom = TuneScoutSpacing.small),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TopBarAction(
            icon = TuneScoutIcons.arrowBack,
            contentDescription = stringResource(ComponentR.string.ui_back),
            onClick = { onEvent(LibrarySearchUiEvent.OnBackClicked) },
        )
        SearchField(
            query = uiState.query,
            placeholder = stringResource(R.string.library_search_placeholder),
            onQueryChange = { query -> onEvent(LibrarySearchUiEvent.OnQueryChanged(query)) },
            onClear = { onEvent(LibrarySearchUiEvent.OnClearQueryClicked) },
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun LibrarySearchResultsList(
    items: List<LibraryItemUiModel>,
    favoritesName: String,
    onEvent: (LibrarySearchUiEvent) -> Unit,
) {
    if (items.isEmpty()) {
        StateMessage(
            title = stringResource(R.string.library_search_empty_title),
            description = stringResource(R.string.library_search_empty_description),
            isAnnounced = true,
        )
        return
    }
    ItemList(items = items, favoritesName = favoritesName, onEvent = onEvent, isRemovable = false)
}

@Composable
private fun RecentSearchesList(
    items: List<LibraryItemUiModel>,
    favoritesName: String,
    onEvent: (LibrarySearchUiEvent) -> Unit,
) {
    if (items.isEmpty()) return
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.library_recent_searches),
            style = MaterialTheme.typography.titleMedium,
            color = TuneScoutColors.textPrimary,
            modifier = Modifier
                .padding(vertical = TuneScoutSpacing.small)
                .semantics { heading() },
        )
        ItemList(items = items, favoritesName = favoritesName, onEvent = onEvent, isRemovable = true)
    }
}

@Composable
private fun ItemList(
    items: List<LibraryItemUiModel>,
    favoritesName: String,
    onEvent: (LibrarySearchUiEvent) -> Unit,
    isRemovable: Boolean,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(),
        contentPadding = PaddingValues(bottom = TuneScoutSpacing.extraLarge),
    ) {
        items(items = items, key = { item -> item.key.toString() }, contentType = { CONTENT_TYPE_ITEM }) { item ->
            LibraryItemRow(
                item = item,
                name = item.getName(favoritesName, stringResource(R.string.library_downloaded_songs)),
                onClick = { onEvent(LibrarySearchUiEvent.OnItemClicked(item)) },
                modifier = Modifier.animateItem(),
                trailing = {
                    if (isRemovable) {
                        SongRowAction(
                            icon = TuneScoutIcons.clear,
                            contentDescription = stringResource(R.string.library_remove_recent_search),
                            onClick = { onEvent(LibrarySearchUiEvent.OnRecentSearchRemoved(item)) },
                        )
                    }
                },
            )
        }
    }
}
