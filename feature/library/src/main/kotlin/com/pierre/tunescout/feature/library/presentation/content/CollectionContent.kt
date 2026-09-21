package com.pierre.tunescout.feature.library.presentation.content

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.pierre.tunescout.core.model.isOn
import com.pierre.tunescout.feature.library.R
import com.pierre.tunescout.feature.library.presentation.model.CollectionTitle
import com.pierre.tunescout.feature.library.presentation.model.CollectionUiEvent
import com.pierre.tunescout.feature.library.presentation.model.CollectionUiState
import com.pierre.tunescout.ui.component.CollectionPlaybackRow
import com.pierre.tunescout.ui.component.NowPlayingState
import com.pierre.tunescout.ui.component.SongRow
import com.pierre.tunescout.ui.component.SongRowMoreAction
import com.pierre.tunescout.ui.component.SongSwipeActionsBox
import com.pierre.tunescout.ui.component.StateMessage
import com.pierre.tunescout.ui.component.TopBar
import com.pierre.tunescout.ui.component.TopBarAction
import com.pierre.tunescout.ui.component.TuneScoutIcons
import com.pierre.tunescout.ui.theme.TuneScoutSpacing
import com.pierre.tunescout.ui.utils.scroll.hideableTopBar
import com.pierre.tunescout.ui.utils.scroll.hidesBarsOnScroll
import com.pierre.tunescout.ui.utils.semantics.screenPane

@Composable
fun CollectionContent(
    uiState: CollectionUiState,
    onEvent: (CollectionUiEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val title = (uiState as? CollectionUiState.Loaded)?.let { loaded -> collectionTitleText(loaded.title) }
    Column(
        modifier = modifier
            .fillMaxSize()
            .screenPane(title)
            .safeDrawingPadding()
            .hidesBarsOnScroll(),
    ) {
        when (uiState) {
            CollectionUiState.Loading -> TopBar(title = "", onBackClick = { onEvent(CollectionUiEvent.OnBackClicked) })
            is CollectionUiState.Loaded -> CollectionLoadedContent(uiState = uiState, onEvent = onEvent)
        }
    }
}

@Composable
private fun CollectionLoadedContent(
    uiState: CollectionUiState.Loaded,
    onEvent: (CollectionUiEvent) -> Unit,
) {
    TopBar(
        title = collectionTitleText(uiState.title),
        modifier = Modifier.hideableTopBar(),
        onBackClick = { onEvent(CollectionUiEvent.OnBackClicked) },
        actions = {
            if (uiState.songs.isNotEmpty() || uiState.isDeletable) {
                TopBarAction(
                    icon = TuneScoutIcons.moreMenu,
                    contentDescription = stringResource(R.string.library_collection_more_options),
                    onClick = { onEvent(CollectionUiEvent.OnMoreClicked) },
                )
            }
        },
    )
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = TuneScoutSpacing.screen),
        contentAlignment = Alignment.TopCenter,
    ) {
        if (uiState.songs.isEmpty()) {
            StateMessage(
                title = stringResource(R.string.library_collection_empty_title),
                description = stringResource(R.string.library_collection_empty_description),
            )
        } else {
            SongList(
                uiState = uiState,
                onEvent = onEvent,
            )
        }
    }
}

@Composable
private fun SongList(
    uiState: CollectionUiState.Loaded,
    onEvent: (CollectionUiEvent) -> Unit,
) {
    val nowPlaying = uiState.nowPlaying
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(),
        contentPadding = PaddingValues(top = TuneScoutSpacing.small, bottom = TuneScoutSpacing.extraLarge),
    ) {
        item(key = "playback") {
            CollectionPlaybackRow(
                isPlaying = uiState.isPlaying,
                isShuffleEnabled = uiState.isShuffleEnabled,
                playContentDescription = stringResource(R.string.library_collection_play_now),
                onPlayPauseClick = { onEvent(CollectionUiEvent.OnPlayPauseClicked) },
                onShuffleClick = { onEvent(CollectionUiEvent.OnShuffleClicked) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = TuneScoutSpacing.small),
            )
        }
        items(items = uiState.songs, key = { song -> song.id }) { song ->
            SongSwipeActionsBox(
                isFavorite = song.id in uiState.favoriteSongIds,
                onAddToQueue = { onEvent(CollectionUiEvent.OnSongSwipedToQueue(song)) },
                onToggleFavorite = { onEvent(CollectionUiEvent.OnSongSwipedToFavorite(song)) },
                modifier = Modifier.animateItem(),
            ) {
                SongRow(
                    title = song.title,
                    subtitle = song.artistName,
                    artworkUrl = song.artwork.thumbnailUrl,
                    nowPlaying = NowPlayingState.of(
                        isCurrentSong = nowPlaying.isOn(song.id),
                        isPlaying = nowPlaying?.isPlaying == true,
                    ),
                    isUnavailable = song.id in uiState.unplayableSongIds,
                    sharedSongId = song.id,
                    onClick = { onEvent(CollectionUiEvent.OnSongClicked(song)) },
                    trailing = { SongRowMoreAction { onEvent(CollectionUiEvent.OnSongOptionsClicked(song)) } },
                )
            }
        }
    }
}

@Composable
internal fun collectionTitleText(title: CollectionTitle): String = when (title) {
    CollectionTitle.Favorites -> stringResource(R.string.library_favorites)
    is CollectionTitle.Custom -> title.name
}
