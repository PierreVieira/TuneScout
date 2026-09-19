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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.pierre.tunescout.core.model.Album
import com.pierre.tunescout.feature.album.R
import com.pierre.tunescout.feature.album.presentation.component.AlbumSkeleton
import com.pierre.tunescout.feature.album.presentation.model.AlbumUiEvent
import com.pierre.tunescout.feature.album.presentation.model.AlbumUiState
import com.pierre.tunescout.ui.component.Artwork
import com.pierre.tunescout.ui.component.SongRow
import com.pierre.tunescout.ui.component.StateMessage
import com.pierre.tunescout.ui.component.TopBar
import com.pierre.tunescout.ui.component.TopBarAction
import com.pierre.tunescout.ui.component.TuneScoutIcons
import com.pierre.tunescout.ui.theme.TuneScoutColors
import com.pierre.tunescout.ui.theme.TuneScoutSpacing
import com.pierre.tunescout.ui.component.R as ComponentR

private val artworkSize = 120.dp
private val inlineArtworkSize = 72.dp
private const val ARTWORK_CORNER_PERCENT = 17
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
            .safeDrawingPadding(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        TopBar(
            title = (uiState as? AlbumUiState.Loaded)?.album?.title.orEmpty(),
            onBackClick = { onEvent(AlbumUiEvent.OnBackClicked) },
            actions = {
                if (uiState is AlbumUiState.Loaded) {
                    TopBarAction(
                        icon = TuneScoutIcons.queueNext,
                        contentDescription = stringResource(R.string.album_play_next),
                        onClick = { onEvent(AlbumUiEvent.OnPlayNextClicked) },
                    )
                    TopBarAction(
                        icon = TuneScoutIcons.addToQueue,
                        contentDescription = stringResource(R.string.album_add_to_queue),
                        onClick = { onEvent(AlbumUiEvent.OnAddToQueueClicked) },
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

            is AlbumUiState.Loaded -> Box(contentAlignment = Alignment.TopCenter) {
                LoadedContent(
                    album = uiState.album,
                    nowPlayingId = uiState.nowPlayingId,
                    isHeaderInline = isHeaderInline,
                    onEvent = onEvent,
                )
            }
        }
    }
}

@Composable
private fun LoadedContent(
    album: Album,
    nowPlayingId: Long?,
    isHeaderInline: Boolean,
    onEvent: (AlbumUiEvent) -> Unit,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(),
        contentPadding = PaddingValues(
            start = TuneScoutSpacing.screen,
            end = TuneScoutSpacing.screen,
            bottom = TuneScoutSpacing.extraLarge,
        ),
    ) {
        item(key = "header") {
            AlbumHeader(album = album, isInline = isHeaderInline)
        }
        items(items = album.songs, key = { song -> song.id }) { song ->
            SongRow(
                title = song.title,
                subtitle = song.artistName,
                artworkUrl = song.artwork.thumbnailUrl,
                artworkSize = rowArtworkSize,
                isHighlighted = song.id == nowPlayingId,
                sharedSongId = song.id,
                onClick = { onEvent(AlbumUiEvent.OnSongClicked(song)) },
            )
        }
    }
}

@Composable
private fun AlbumHeader(
    album: Album,
    isInline: Boolean,
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
            AlbumTitles(album = album, horizontalAlignment = Alignment.Start)
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = TuneScoutSpacing.extraLarge),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(TuneScoutSpacing.medium),
        ) {
            AlbumArtwork(album = album, size = artworkSize)
            AlbumTitles(album = album, horizontalAlignment = Alignment.CenterHorizontally)
        }
    }
}

@Composable
private fun AlbumArtwork(
    album: Album,
    size: Dp,
) {
    Artwork(
        url = album.artwork.mediumUrl,
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
private fun AlbumTitles(
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
