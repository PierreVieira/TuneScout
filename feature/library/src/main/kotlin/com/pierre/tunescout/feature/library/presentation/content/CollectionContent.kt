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
import com.pierre.tunescout.core.model.Song
import com.pierre.tunescout.feature.library.R
import com.pierre.tunescout.feature.library.presentation.model.CollectionTitle
import com.pierre.tunescout.feature.library.presentation.model.CollectionUiEvent
import com.pierre.tunescout.feature.library.presentation.model.CollectionUiState
import com.pierre.tunescout.ui.component.SongRow
import com.pierre.tunescout.ui.component.SongRowMoreAction
import com.pierre.tunescout.ui.component.StateMessage
import com.pierre.tunescout.ui.component.SwipeToRemoveBox
import com.pierre.tunescout.ui.component.TopBar
import com.pierre.tunescout.ui.component.TopBarAction
import com.pierre.tunescout.ui.component.TuneScoutIcons
import com.pierre.tunescout.ui.component.getNowPlayingState
import com.pierre.tunescout.ui.theme.TuneScoutSpacing
import com.pierre.tunescout.ui.utils.scroll.hideableTopBar
import com.pierre.tunescout.ui.utils.scroll.hidesBarsOnScroll

@Composable
fun CollectionContent(
    uiState: CollectionUiState,
    onEvent: (CollectionUiEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .hidesBarsOnScroll(),
    ) {
        when (uiState) {
            CollectionUiState.Loading -> TopBar(title = "", onBackClick = { onEvent(CollectionUiEvent.OnBackClicked) })
            is CollectionUiState.Loaded -> CollectionLoaded(uiState = uiState, onEvent = onEvent)
        }
    }
}

@Composable
private fun CollectionLoaded(
    uiState: CollectionUiState.Loaded,
    onEvent: (CollectionUiEvent) -> Unit,
) {
    TopBar(
        title = collectionTitleText(uiState.title),
        modifier = Modifier.hideableTopBar(),
        onBackClick = { onEvent(CollectionUiEvent.OnBackClicked) },
        actions = {
            val hasSongs = uiState.songs.isNotEmpty()
            if (hasSongs) {
                TopBarAction(
                    icon = TuneScoutIcons.play,
                    contentDescription = stringResource(R.string.library_collection_play_now),
                    onClick = { onEvent(CollectionUiEvent.OnPlayNowClicked) },
                )
            }
            if (hasSongs || uiState.isDeletable) {
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
                songs = uiState.songs,
                nowPlayingId = uiState.nowPlayingId,
                isPlaying = uiState.isPlaying,
                onEvent = onEvent,
            )
        }
    }
}

@Composable
private fun SongList(
    songs: List<Song>,
    nowPlayingId: Long?,
    isPlaying: Boolean,
    onEvent: (CollectionUiEvent) -> Unit,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(),
        contentPadding = PaddingValues(top = TuneScoutSpacing.small, bottom = TuneScoutSpacing.extraLarge),
    ) {
        items(items = songs, key = { song -> song.id }) { song ->
            SwipeToRemoveBox(
                onRemove = { onEvent(CollectionUiEvent.OnSongRemoved(song)) },
                modifier = Modifier.animateItem(),
            ) {
                SongRow(
                    title = song.title,
                    subtitle = song.artistName,
                    artworkUrl = song.artwork.thumbnailUrl,
                    nowPlaying = getNowPlayingState(
                        isCurrentSong = song.id == nowPlayingId,
                        isPlaying = isPlaying,
                    ),
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
