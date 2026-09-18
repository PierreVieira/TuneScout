package com.pierre.tunescout.feature.songs.presentation.content

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.paging.compose.LazyPagingItems
import com.pierre.tunescout.core.model.Song
import com.pierre.tunescout.feature.songs.R
import com.pierre.tunescout.feature.songs.presentation.component.RecentlyPlayedList
import com.pierre.tunescout.feature.songs.presentation.component.SearchResultsList
import com.pierre.tunescout.feature.songs.presentation.model.SongsUiEvent
import com.pierre.tunescout.feature.songs.presentation.model.SongsUiState
import com.pierre.tunescout.ui.component.SearchField
import com.pierre.tunescout.ui.theme.TuneScoutColors
import com.pierre.tunescout.ui.theme.TuneScoutSpacing

private val titleHeight = 48.dp

@Composable
fun SongsContent(
    uiState: SongsUiState,
    searchResults: LazyPagingItems<Song>,
    onEvent: (SongsUiEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding(),
    ) {
        Text(
            text = stringResource(R.string.songs_title),
            style = MaterialTheme.typography.headlineMedium,
            color = TuneScoutColors.textPrimary,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = TuneScoutSpacing.screen)
                .height(titleHeight)
                .padding(horizontal = TuneScoutSpacing.large, vertical = TuneScoutSpacing.small),
        )
        SearchField(
            query = uiState.query,
            placeholder = stringResource(R.string.songs_search_placeholder),
            onQueryChange = { query -> onEvent(SongsUiEvent.OnQueryChanged(query)) },
            onClear = { onEvent(SongsUiEvent.OnClearQueryClicked) },
            modifier = Modifier.padding(horizontal = TuneScoutSpacing.screen, vertical = TuneScoutSpacing.small),
        )
        if (uiState.isSearching) {
            SearchResultsList(
                query = uiState.query,
                searchResults = searchResults,
                onEvent = onEvent,
            )
        } else {
            RecentlyPlayedList(
                songs = uiState.recentlyPlayed,
                nowPlayingId = uiState.nowPlayingId,
                onEvent = onEvent,
            )
        }
    }
}
