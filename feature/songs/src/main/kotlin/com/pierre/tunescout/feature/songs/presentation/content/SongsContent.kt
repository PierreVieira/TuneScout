package com.pierre.tunescout.feature.songs.presentation.content

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
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
import com.pierre.tunescout.ui.component.ConfirmationDialog
import com.pierre.tunescout.ui.component.NoticeBar
import com.pierre.tunescout.ui.component.SearchField
import com.pierre.tunescout.ui.component.TopBarAction
import com.pierre.tunescout.ui.component.TuneScoutIcons
import com.pierre.tunescout.ui.theme.TuneScoutColors
import com.pierre.tunescout.ui.theme.TuneScoutSpacing
import com.pierre.tunescout.ui.utils.scroll.hideableTopBar
import com.pierre.tunescout.ui.utils.scroll.hidesBarsOnScroll

private val titleHeight = 48.dp

@Composable
fun SongsContent(
    uiState: SongsUiState,
    searchResults: LazyPagingItems<Song>,
    isHeaderInline: Boolean,
    onEvent: (SongsUiEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
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
                isHeaderInline = isHeaderInline,
                onEvent = onEvent,
                modifier = Modifier.hideableTopBar(),
            )
            if (uiState.isOffline) {
                NoticeBar(text = stringResource(R.string.songs_offline_notice))
            }
            if (uiState.isSearching) {
                SearchResultsList(
                    searchResults = searchResults,
                    nowPlaying = uiState.nowPlaying,
                    isOffline = uiState.isOffline,
                    onEvent = onEvent,
                )
            } else {
                RecentlyPlayedList(
                    songs = uiState.recentlyPlayed,
                    nowPlaying = uiState.nowPlaying,
                    songPendingRemoval = uiState.songPendingRemoval,
                    onEvent = onEvent,
                )
            }
        }
        uiState.songPendingRemoval?.let { song ->
            RemoveRecentDialog(song = song, onEvent = onEvent)
        }
    }
}

@Composable
private fun RemoveRecentDialog(
    song: Song,
    onEvent: (SongsUiEvent) -> Unit,
) {
    ConfirmationDialog(
        title = stringResource(R.string.songs_remove_recent_confirm_title),
        message = stringResource(R.string.songs_remove_recent_confirm_message, song.title),
        confirmLabel = stringResource(R.string.songs_remove_recent_confirm_action),
        cancelLabel = stringResource(R.string.songs_confirm_cancel),
        onConfirm = { onEvent(SongsUiEvent.OnRemoveRecentConfirmed) },
        onCancel = { onEvent(SongsUiEvent.OnRemoveRecentDismissed) },
    )
}

@Composable
private fun Header(
    uiState: SongsUiState,
    isHeaderInline: Boolean,
    onEvent: (SongsUiEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        if (isHeaderInline) {
            HeaderRow(horizontalArrangement = Arrangement.spacedBy(TuneScoutSpacing.small)) {
                Title()
                SongsSearchField(uiState = uiState, onEvent = onEvent, modifier = Modifier.weight(1f))
                ThemeAction(onEvent = onEvent)
            }
        } else {
            HeaderRow {
                Title(modifier = Modifier.weight(1f))
                ThemeAction(onEvent = onEvent)
            }
            SongsSearchField(uiState = uiState, onEvent = onEvent)
        }
    }
}

@Composable
private fun HeaderRow(
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    content: @Composable RowScope.() -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = TuneScoutSpacing.screen),
        horizontalArrangement = horizontalArrangement,
        verticalAlignment = Alignment.CenterVertically,
        content = content,
    )
}

@Composable
private fun Title(modifier: Modifier = Modifier) {
    Text(
        text = stringResource(R.string.songs_title),
        style = MaterialTheme.typography.headlineMedium,
        color = TuneScoutColors.textPrimary,
        modifier = modifier
            .height(titleHeight)
            .padding(horizontal = TuneScoutSpacing.large, vertical = TuneScoutSpacing.small),
    )
}

@Composable
private fun ThemeAction(onEvent: (SongsUiEvent) -> Unit) {
    TopBarAction(
        icon = TuneScoutIcons.theme,
        contentDescription = stringResource(R.string.songs_open_theme),
        onClick = { onEvent(SongsUiEvent.OnThemeClicked) },
        modifier = Modifier.padding(end = TuneScoutSpacing.small),
    )
}

@Composable
private fun SongsSearchField(
    uiState: SongsUiState,
    onEvent: (SongsUiEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    SearchField(
        query = uiState.query,
        placeholder = stringResource(R.string.songs_search_placeholder),
        onQueryChange = { query -> onEvent(SongsUiEvent.OnQueryChanged(query)) },
        onClear = { onEvent(SongsUiEvent.OnClearQueryClicked) },
        modifier = modifier.padding(horizontal = TuneScoutSpacing.screen, vertical = TuneScoutSpacing.small),
    )
}
