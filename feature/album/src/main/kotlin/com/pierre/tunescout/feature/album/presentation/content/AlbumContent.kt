package com.pierre.tunescout.feature.album.presentation.content

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.pierre.tunescout.core.model.Album
import com.pierre.tunescout.core.model.isOn
import com.pierre.tunescout.feature.album.R
import com.pierre.tunescout.feature.album.presentation.component.AlbumSkeleton
import com.pierre.tunescout.feature.album.presentation.model.AlbumUiEvent
import com.pierre.tunescout.feature.album.presentation.model.AlbumUiState
import com.pierre.tunescout.ui.component.Artwork
import com.pierre.tunescout.ui.component.CollectionPlaybackRow
import com.pierre.tunescout.ui.component.NoticeBar
import com.pierre.tunescout.ui.component.NowPlayingState
import com.pierre.tunescout.ui.component.SongRow
import com.pierre.tunescout.ui.component.SongRowMoreAction
import com.pierre.tunescout.ui.component.SongSwipeActionsBox
import com.pierre.tunescout.ui.component.StateMessage
import com.pierre.tunescout.ui.component.TopBar
import com.pierre.tunescout.ui.component.TopBarAction
import com.pierre.tunescout.ui.component.TuneScoutIcons
import com.pierre.tunescout.ui.theme.TuneScoutColors
import com.pierre.tunescout.ui.theme.TuneScoutSpacing
import com.pierre.tunescout.ui.utils.scroll.hideableTopBar
import com.pierre.tunescout.ui.utils.scroll.hidesBarsOnScroll
import com.pierre.tunescout.ui.component.R as ComponentR

private val artworkSize = 120.dp
private val inlineArtworkSize = 72.dp
private const val ARTWORK_CORNER_PERCENT = 17
private const val ALBUM_TRACKS_TAG = "album_tracks"
private val artworkElevation = 8.dp
private val rowArtworkSize = 44.dp

@Composable
fun AlbumContent(
    uiState: AlbumUiState,
    isHeaderInline: Boolean,
    onEvent: (AlbumUiEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .hidesBarsOnScroll(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        TopBar(
            title = (uiState as? AlbumUiState.Loaded)?.album?.title.orEmpty(),
            modifier = Modifier.hideableTopBar(),
            onBackClick = { onEvent(AlbumUiEvent.OnBackClicked) },
            actions = {
                if (uiState is AlbumUiState.Loaded) {
                    if (uiState.album.isComplete) {
                        FavoriteAction(isFavorite = uiState.isFavorite, onEvent = onEvent)
                    }
                    TopBarAction(
                        icon = TuneScoutIcons.moreMenu,
                        contentDescription = stringResource(R.string.album_more_options),
                        onClick = { onEvent(AlbumUiEvent.OnMoreClicked) },
                    )
                }
            },
        )
        when (uiState) {
            AlbumUiState.Loading -> AlbumSkeleton(
                artworkSize = artworkSize,
                artworkCornerPercent = ARTWORK_CORNER_PERCENT,
                rowArtworkSize = rowArtworkSize,
                modifier = Modifier.fillMaxWidth(),
            )

            AlbumUiState.Error -> StateMessage(
                title = stringResource(R.string.album_error_title),
                description = stringResource(R.string.album_error_description),
                onRetry = { onEvent(AlbumUiEvent.OnRetryClicked) },
            )

            is AlbumUiState.Loaded -> {
                LoadedNoticeBar(uiState = uiState)
                Box(contentAlignment = Alignment.TopCenter) {
                    LoadedContent(
                        uiState = uiState,
                        isHeaderInline = isHeaderInline,
                        onEvent = onEvent,
                    )
                }
            }
        }
    }
}

/**
 * An album put together from the saved tracks says so before anything else: the rows are real, but
 * the list is not the whole album.
 */
@Composable
private fun LoadedNoticeBar(uiState: AlbumUiState.Loaded) {
    when {
        !uiState.album.isComplete -> NoticeBar(text = stringResource(R.string.album_partial_notice))
        uiState.isStale -> NoticeBar(text = stringResource(R.string.album_stale_notice))
    }
}

/**
 * Liking is a state, so it stays on the bar where a filled heart can show it; the queue commands
 * have no state to show and move into the sheet behind the overflow. It is left out for an album
 * put together from the saved tracks: liking stores the album, and storing a partial one would keep
 * the rest of its tracks from ever being fetched.
 */
@Composable
private fun FavoriteAction(
    isFavorite: Boolean,
    onEvent: (AlbumUiEvent) -> Unit,
) {
    TopBarAction(
        icon = if (isFavorite) TuneScoutIcons.favoriteFilled else TuneScoutIcons.favorite,
        contentDescription = stringResource(
            if (isFavorite) R.string.album_unfavorite else R.string.album_favorite,
        ),
        tint = if (isFavorite) TuneScoutColors.accent else TuneScoutColors.textPrimary,
        onClick = { onEvent(AlbumUiEvent.OnFavoriteClicked) },
    )
}

@Composable
private fun LoadedContent(
    uiState: AlbumUiState.Loaded,
    isHeaderInline: Boolean,
    onEvent: (AlbumUiEvent) -> Unit,
) {
    val album = uiState.album
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .testTag(ALBUM_TRACKS_TAG),
        contentPadding = PaddingValues(
            start = TuneScoutSpacing.screen,
            end = TuneScoutSpacing.screen,
            bottom = TuneScoutSpacing.extraLarge,
        ),
    ) {
        item(key = "header") {
            AlbumHeader(
                album = album,
                isInline = isHeaderInline,
                playbackRow = { modifier ->
                    if (album.songs.isNotEmpty()) {
                        CollectionPlaybackRow(
                            isPlaying = uiState.isPlaying,
                            isShuffleEnabled = uiState.isShuffleEnabled,
                            playContentDescription = stringResource(R.string.album_play),
                            onPlayPauseClick = { onEvent(AlbumUiEvent.OnPlayPauseClicked) },
                            onShuffleClick = { onEvent(AlbumUiEvent.OnShuffleClicked) },
                            modifier = modifier,
                        )
                    }
                },
            )
        }
        items(items = album.songs, key = { song -> song.id }) { song ->
            SongSwipeActionsBox(
                isFavorite = song.id in uiState.favoriteSongIds,
                onAddToQueue = { onEvent(AlbumUiEvent.OnSongSwipedToQueue(song)) },
                onToggleFavorite = { onEvent(AlbumUiEvent.OnSongSwipedToFavorite(song)) },
            ) {
                SongRow(
                    title = song.title,
                    subtitle = song.artistName,
                    artworkUrl = song.artwork.thumbnailUrl,
                    artworkSize = rowArtworkSize,
                    nowPlaying = NowPlayingState.of(
                        isCurrentSong = uiState.nowPlaying.isOn(song.id),
                        isPlaying = uiState.nowPlaying?.isPlaying == true,
                    ),
                    isUnavailable = song.id in uiState.unplayableSongIds,
                    sharedSongId = song.id,
                    onClick = { onEvent(AlbumUiEvent.OnSongClicked(song)) },
                    trailing = { SongRowMoreAction { onEvent(AlbumUiEvent.OnSongOptionsClicked(song)) } },
                )
            }
        }
    }
}

/**
 * A header laid out inline, beside a landscape list, has height to spare for nothing: the play button
 * and shuffle join its row instead of taking one of their own under it.
 */
@Composable
private fun AlbumHeader(
    album: Album,
    isInline: Boolean,
    playbackRow: @Composable (Modifier) -> Unit,
) {
    if (isInline) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = TuneScoutSpacing.medium),
            horizontalArrangement = Arrangement.spacedBy(TuneScoutSpacing.medium),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AlbumArtwork(album = album, size = inlineArtworkSize)
            AlbumTitlesHeading(
                album = album,
                horizontalAlignment = Alignment.Start,
                modifier = Modifier.weight(1f),
            )
            playbackRow(Modifier)
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = TuneScoutSpacing.medium),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(TuneScoutSpacing.medium),
        ) {
            AlbumArtwork(album = album, size = artworkSize)
            AlbumTitlesHeading(album = album, horizontalAlignment = Alignment.CenterHorizontally)
            playbackRow(Modifier.fillMaxWidth())
        }
    }
}

/**
 * An album put together from the saved tracks draws the thumbnail its rows already loaded: the
 * larger cover was never downloaded, and with no connection it would stay a placeholder. The whole
 * album brings the larger url back, which is also what makes the cover load once the device is online.
 */
@Composable
private fun AlbumArtwork(
    album: Album,
    size: Dp,
) {
    Artwork(
        url = if (album.isComplete) album.artwork.mediumUrl else album.artwork.thumbnailUrl,
        contentDescription = stringResource(ComponentR.string.ui_artwork_of, album.title),
        cornerPercent = ARTWORK_CORNER_PERCENT,
        modifier = Modifier
            .size(size)
            .shadow(
                elevation = artworkElevation,
                shape = RoundedCornerShape(percent = ARTWORK_CORNER_PERCENT),
            ),
    )
}

@Composable
private fun AlbumTitlesHeading(
    album: Album,
    horizontalAlignment: Alignment.Horizontal,
    modifier: Modifier = Modifier,
) {
    val textAlign = if (horizontalAlignment == Alignment.CenterHorizontally) {
        TextAlign.Center
    } else {
        TextAlign.Start
    }
    Column(
        modifier = modifier,
        horizontalAlignment = horizontalAlignment,
        verticalArrangement = Arrangement.spacedBy(TuneScoutSpacing.small),
    ) {
        Text(
            text = album.title,
            style = MaterialTheme.typography.titleLarge,
            color = TuneScoutColors.textPrimary,
            textAlign = textAlign,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = album.artistName,
            style = MaterialTheme.typography.bodyMedium,
            color = TuneScoutColors.textPrimary,
            textAlign = textAlign,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
