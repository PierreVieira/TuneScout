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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.pierre.tunescout.core.model.Artwork
import com.pierre.tunescout.feature.library.presentation.model.LibraryItemUiModel
import com.pierre.tunescout.ui.component.TuneScoutIcons
import com.pierre.tunescout.ui.theme.TuneScoutColors

private const val ARTWORK_CORNER_PERCENT = 8
private const val QUADRANT_COUNT = 4
private const val ICON_FRACTION = 0.4f

/**
 * How big the tile is drawn, which is what decides the artwork it asks Apple for: a grid cell is
 * three times the width of a row, and the thumbnail a row is happy with is visibly soft there.
 */
internal enum class LibraryArtworkSize {
    ROW,
    CELL,
    ;

    fun getUrl(artwork: Artwork): String = when (this) {
        ROW -> artwork.thumbnailUrl
        CELL -> artwork.mediumUrl
    }
}

/**
 * A playlist has no cover of its own, so it wears the first four songs' artwork as a quadrant grid,
 * the way Spotify does. One song fills the tile, none falls back to the placeholder note.
 */
@Composable
internal fun LibraryItemArtwork(
    item: LibraryItemUiModel,
    size: LibraryArtworkSize,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(percent = ARTWORK_CORNER_PERCENT))
            .background(backgroundOf(item)),
        contentAlignment = Alignment.Center,
    ) {
        when (item) {
            is LibraryItemUiModel.Favorites -> FavoritesIcon()
            is LibraryItemUiModel.Playlist -> PlaylistCover(artworks = item.artworks, size = size)
        }
    }
}

@Composable
private fun backgroundOf(item: LibraryItemUiModel): Color = when (item) {
    is LibraryItemUiModel.Favorites -> TuneScoutColors.accentContainer
    is LibraryItemUiModel.Playlist -> TuneScoutColors.surfaceSubtle
}

@Composable
private fun PlaylistCover(
    artworks: List<Artwork>,
    size: LibraryArtworkSize,
) {
    when {
        artworks.isEmpty() -> PlaceholderIcon()

        artworks.size < QUADRANT_COUNT -> CoverImage(
            artwork = artworks.first(),
            size = size,
            modifier = Modifier.fillMaxSize(),
        )

        else -> QuadrantGrid(artworks = artworks, size = size)
    }
}

@Composable
private fun QuadrantGrid(
    artworks: List<Artwork>,
    size: LibraryArtworkSize,
) {
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
                    CoverImage(artwork = artwork, size = size, modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun CoverImage(
    artwork: Artwork,
    size: LibraryArtworkSize,
    modifier: Modifier = Modifier,
) {
    AsyncImage(
        model = size.getUrl(artwork),
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = modifier.fillMaxSize(),
    )
}

@Composable
private fun FavoritesIcon() {
    Icon(
        imageVector = TuneScoutIcons.favoriteFilled,
        contentDescription = null,
        tint = TuneScoutColors.accent,
        modifier = Modifier.fillMaxSize(fraction = ICON_FRACTION),
    )
}

@Composable
private fun PlaceholderIcon() {
    Icon(
        imageVector = TuneScoutIcons.musicList,
        contentDescription = null,
        tint = TuneScoutColors.elementPlaceholder,
        modifier = Modifier.fillMaxSize(fraction = ICON_FRACTION),
    )
}
