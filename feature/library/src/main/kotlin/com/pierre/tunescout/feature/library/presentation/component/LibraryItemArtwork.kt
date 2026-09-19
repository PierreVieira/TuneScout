package com.pierre.tunescout.feature.library.presentation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.pierre.tunescout.core.model.Artwork
import com.pierre.tunescout.feature.library.presentation.model.LibraryItemUiModel
import com.pierre.tunescout.ui.component.TuneScoutIcons
import com.pierre.tunescout.ui.theme.TuneScoutColors

private const val ARTWORK_CORNER_PERCENT = 8
private const val QUADRANT_COUNT = 4

/**
 * A playlist has no cover of its own, so it wears the first four songs' artwork as a quadrant grid,
 * the way Spotify does. One song fills the tile, none falls back to the placeholder note.
 */
@Composable
internal fun LibraryItemArtwork(
    item: LibraryItemUiModel,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(percent = ARTWORK_CORNER_PERCENT))
            .background(TuneScoutColors.surfaceSubtle),
        contentAlignment = Alignment.Center,
    ) {
        when (item) {
            is LibraryItemUiModel.Favorites -> CenteredIcon(isFavorites = true)
            is LibraryItemUiModel.Playlist -> PlaylistCover(artworks = item.artworks)
        }
    }
}

@Composable
private fun PlaylistCover(artworks: List<Artwork>) {
    when {
        artworks.isEmpty() -> CenteredIcon(isFavorites = false)
        artworks.size < QUADRANT_COUNT -> CoverImage(artwork = artworks.first(), modifier = Modifier.fillMaxSize())
        else -> QuadrantGrid(artworks = artworks)
    }
}

@Composable
private fun QuadrantGrid(artworks: List<Artwork>) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(0.dp),
    ) {
        artworks.take(QUADRANT_COUNT).chunked(2).forEach { pair ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
            ) {
                pair.forEach { artwork ->
                    CoverImage(artwork = artwork, modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun CoverImage(
    artwork: Artwork,
    modifier: Modifier = Modifier,
) {
    AsyncImage(
        model = artwork.thumbnailUrl,
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = modifier.fillMaxSize(),
    )
}

@Composable
private fun CenteredIcon(isFavorites: Boolean) {
    Icon(
        imageVector = if (isFavorites) TuneScoutIcons.favoriteFilled else TuneScoutIcons.musicList,
        contentDescription = null,
        tint = if (isFavorites) TuneScoutColors.textEmphasis else TuneScoutColors.elementPlaceholder,
        modifier = Modifier.fillMaxSize(fraction = 0.4f),
    )
}
