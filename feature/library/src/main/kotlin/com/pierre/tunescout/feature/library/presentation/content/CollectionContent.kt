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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import com.pierre.tunescout.core.model.Song
import com.pierre.tunescout.core.model.SongDownloadStatus
import com.pierre.tunescout.core.model.isOn
import com.pierre.tunescout.feature.library.R
import com.pierre.tunescout.feature.library.presentation.model.CollectionTitle
import com.pierre.tunescout.feature.library.presentation.model.CollectionUiEvent
import com.pierre.tunescout.feature.library.presentation.model.CollectionUiState
import com.pierre.tunescout.ui.component.CollectionDownloadButton
import com.pierre.tunescout.ui.component.CollectionPlaybackRow
import com.pierre.tunescout.ui.component.DownloadIndicator
import com.pierre.tunescout.ui.component.DuplicateInQueueDialog
import com.pierre.tunescout.ui.component.NowPlayingState
import com.pierre.tunescout.ui.component.ReorderableSongSwipeBox
import com.pierre.tunescout.ui.component.SongDragHandle
import com.pierre.tunescout.ui.component.SongRow
import com.pierre.tunescout.ui.component.SongRowMoreAction
import com.pierre.tunescout.ui.component.StateMessage
import com.pierre.tunescout.ui.component.TopBar
import com.pierre.tunescout.ui.component.TopBarAction
import com.pierre.tunescout.ui.component.TuneScoutIcons
import com.pierre.tunescout.ui.theme.TuneScoutSpacing
import com.pierre.tunescout.ui.utils.scroll.hideableTopBar
import com.pierre.tunescout.ui.utils.scroll.hidesBarsOnScroll
import com.pierre.tunescout.ui.utils.semantics.screenPane
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState
import com.pierre.tunescout.ui.component.R as ComponentR

private const val CONTENT_TYPE_PLAYBACK = "playback"
private const val CONTENT_TYPE_SONG = "song"

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
    NavigationBackHandler(
        state = rememberNavigationEventState(currentInfo = NavigationEventInfo.None),
        isBackEnabled = uiState.isReordering,
        onBackCompleted = { onEvent(CollectionUiEvent.OnReorderFinished) },
    )
    TopBar(
        title = collectionTitleText(uiState.title),
        modifier = Modifier.hideableTopBar(),
        onBackClick = { onEvent(CollectionUiEvent.OnBackClicked) },
        actions = {
            if (uiState.isReordering) {
                TopBarAction(
                    icon = TuneScoutIcons.check,
                    contentDescription = stringResource(ComponentR.string.ui_reorder_done),
                    onClick = { onEvent(CollectionUiEvent.OnReorderFinished) },
                )
            } else if (uiState.songs.isNotEmpty() || uiState.isDeletable) {
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
                description = stringResource(emptyDescriptionOf(uiState.title)),
            )
        } else {
            SongList(
                uiState = uiState,
                onEvent = onEvent,
            )
        }
    }
    uiState.songAlreadyQueued?.let { song ->
        DuplicateInQueueDialog(
            songTitle = song.title,
            onConfirm = { onEvent(CollectionUiEvent.OnDuplicateInQueueConfirmed) },
            onCancel = { onEvent(CollectionUiEvent.OnDuplicateInQueueDismissed) },
        )
    }
}

@Composable
private fun SongList(
    uiState: CollectionUiState.Loaded,
    onEvent: (CollectionUiEvent) -> Unit,
) {
    val nowPlaying = uiState.nowPlaying
    val lazyListState = rememberLazyListState()
    val reorderableState = rememberReorderableLazyListState(lazyListState) { from, to ->
        val fromSongId = from.key as? Long
        val toSongId = to.key as? Long
        if (fromSongId != null && toSongId != null) {
            onEvent(CollectionUiEvent.OnSongMoved(fromSongId = fromSongId, toSongId = toSongId))
        }
    }
    LazyColumn(
        state = lazyListState,
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(),
        contentPadding = PaddingValues(top = TuneScoutSpacing.small, bottom = TuneScoutSpacing.extraLarge),
    ) {
        item(key = "playback", contentType = CONTENT_TYPE_PLAYBACK) {
            CollectionPlaybackRow(
                isPlaying = uiState.isPlaying,
                isShuffleEnabled = uiState.isShuffleEnabled,
                playContentDescription = stringResource(R.string.library_collection_play_now),
                onPlayPauseClick = { onEvent(CollectionUiEvent.OnPlayPauseClicked) },
                onShuffleClick = { onEvent(CollectionUiEvent.OnShuffleClicked) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = TuneScoutSpacing.small),
                download = if (uiState.isDownloadable) {
                    {
                        CollectionDownloadButton(
                            progress = uiState.download.progress,
                            totalCount = uiState.songs.size,
                            onClick = { onEvent(CollectionUiEvent.OnDownloadClicked) },
                        )
                    }
                } else {
                    null
                },
            )
        }
        itemsIndexed(
            items = uiState.songs,
            key = { _, song -> song.id },
            contentType = { _, _ -> CONTENT_TYPE_SONG },
        ) { index, song ->
            val moveTo: (Song) -> () -> Unit = { target ->
                { onEvent(CollectionUiEvent.OnSongMoved(fromSongId = song.id, toSongId = target.id)) }
            }
            ReorderableItem(reorderableState, key = song.id, enabled = uiState.isReorderable) {
                ReorderableSongSwipeBox(
                    isReordering = uiState.isReordering,
                    isFavorite = song.id in uiState.favoriteSongIds,
                    onAddToQueue = { onEvent(CollectionUiEvent.OnSongSwipedToQueue(song)) },
                    onToggleFavorite = { onEvent(CollectionUiEvent.OnSongSwipedToFavorite(song)) },
                    onReorderStarted = { onEvent(CollectionUiEvent.OnReorderStarted) },
                    onMoveUp = uiState.songs.getOrNull(index - 1)?.let(moveTo),
                    onMoveDown = uiState.songs.getOrNull(index + 1)?.let(moveTo),
                    isReorderable = uiState.isReorderable,
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
                        downloadIndicator = DownloadIndicator.of(
                            isRequested = song.id in uiState.downloadStatuses,
                            isComplete = uiState.downloadStatuses[song.id] == SongDownloadStatus.Downloaded,
                        ),
                        sharedSongId = song.id,
                        onClick = {
                            onEvent(
                                CollectionUiEvent.OnSongClicked(song),
                            )
                        }.takeUnless { uiState.isReordering },
                        trailing = {
                            if (uiState.isReordering) {
                                SongDragHandle(dragsOnPress = true)
                            } else {
                                SongRowMoreAction { onEvent(CollectionUiEvent.OnSongOptionsClicked(song)) }
                            }
                        },
                    )
                }
            }
        }
    }
}

@Composable
internal fun collectionTitleText(title: CollectionTitle): String = when (title) {
    CollectionTitle.Favorites -> stringResource(R.string.library_favorites)
    CollectionTitle.DownloadedSongs -> stringResource(R.string.library_downloaded_songs)
    is CollectionTitle.Custom -> title.name
}

private fun emptyDescriptionOf(title: CollectionTitle): Int = when (title) {
    CollectionTitle.DownloadedSongs -> R.string.library_downloaded_songs_empty_description
    CollectionTitle.Favorites, is CollectionTitle.Custom -> R.string.library_collection_empty_description
}
