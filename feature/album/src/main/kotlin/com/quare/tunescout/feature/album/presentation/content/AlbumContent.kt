package com.quare.tunescout.feature.album.presentation.content

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.unit.dp
import com.quare.tunescout.core.model.Album
import com.quare.tunescout.feature.album.R
import com.quare.tunescout.feature.album.presentation.component.AlbumSkeleton
import com.quare.tunescout.feature.album.presentation.model.AlbumUiEvent
import com.quare.tunescout.feature.album.presentation.model.AlbumUiState
import com.quare.tunescout.ui.component.Artwork
import com.quare.tunescout.ui.component.SongRow
import com.quare.tunescout.ui.component.StateMessage
import com.quare.tunescout.ui.component.TopBar
import com.quare.tunescout.ui.theme.TuneScoutColors
import com.quare.tunescout.ui.theme.TuneScoutSpacing
import com.quare.tunescout.ui.component.R as ComponentR

private val artworkSize = 120.dp
private val artworkCornerRadius = 20.dp
private val artworkElevation = 8.dp
private val rowArtworkSize = 44.dp

@Composable
fun AlbumContent(
    uiState: AlbumUiState,
    onEvent: (AlbumUiEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize()) {
        TopBar(
            title = "",
            onBackClick = { onEvent(AlbumUiEvent.OnBackClicked) },
        )
        when (uiState) {
            AlbumUiState.Loading -> AlbumSkeleton(
                artworkSize = artworkSize,
                artworkCornerRadius = artworkCornerRadius,
                rowArtworkSize = rowArtworkSize,
            )

            AlbumUiState.Error -> StateMessage(
                title = stringResource(R.string.album_error_title),
                description = stringResource(R.string.album_error_description),
                onRetry = { onEvent(AlbumUiEvent.OnRetryClicked) },
            )

            is AlbumUiState.Loaded -> LoadedContent(
                album = uiState.album,
                nowPlayingId = uiState.nowPlayingId,
                onEvent = onEvent,
            )
        }
    }
}

@Composable
private fun LoadedContent(
    album: Album,
    nowPlayingId: Long?,
    onEvent: (AlbumUiEvent) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = TuneScoutSpacing.screen,
            end = TuneScoutSpacing.screen,
            bottom = TuneScoutSpacing.extraLarge,
        ),
    ) {
        item(key = "header") {
            AlbumHeader(album = album)
        }
        items(items = album.songs, key = { song -> song.id }) { song ->
            SongRow(
                title = song.title,
                subtitle = song.artistName,
                artworkUrl = song.artworkUrl,
                artworkSize = rowArtworkSize,
                isHighlighted = song.id == nowPlayingId,
                onClick = { onEvent(AlbumUiEvent.OnSongClicked(song)) },
            )
        }
    }
}

@Composable
private fun AlbumHeader(album: Album) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = TuneScoutSpacing.extraLarge),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(TuneScoutSpacing.medium),
    ) {
        Artwork(
            url = album.artworkUrl,
            contentDescription = stringResource(ComponentR.string.ui_artwork_of, album.title),
            cornerRadius = artworkCornerRadius,
            modifier = Modifier
                .size(artworkSize)
                .shadow(elevation = artworkElevation, shape = RoundedCornerShape(artworkCornerRadius)),
        )
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(TuneScoutSpacing.small),
        ) {
            Text(
                text = album.title,
                style = MaterialTheme.typography.titleLarge,
                color = TuneScoutColors.textPrimary,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = album.artistName,
                style = MaterialTheme.typography.bodyMedium,
                color = TuneScoutColors.textPrimary,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
