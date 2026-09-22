package com.pierre.tunescout.feature.library.presentation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.pierre.tunescout.core.model.Artwork
import com.pierre.tunescout.feature.library.domain.model.LibraryViewMode
import com.pierre.tunescout.feature.library.presentation.model.LibraryItemUiModel
import com.pierre.tunescout.ui.component.COVER_CORNER_PERCENT
import com.pierre.tunescout.ui.component.CoverImage
import com.pierre.tunescout.ui.component.PlaylistCover
import com.pierre.tunescout.ui.component.TuneScoutIcons
import com.pierre.tunescout.ui.theme.TuneScoutColors

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

    companion object {
        /** @return the size the items of [viewMode] draw their covers at. */
        fun of(viewMode: LibraryViewMode): LibraryArtworkSize = when (viewMode) {
            LibraryViewMode.LIST -> ROW
            LibraryViewMode.GRID -> CELL
        }
    }
}

@Composable
internal fun LibraryItemArtwork(
    item: LibraryItemUiModel,
    size: LibraryArtworkSize,
    modifier: Modifier = Modifier,
) {
    when (item) {
        is LibraryItemUiModel.Favorites -> TileBox(background = TuneScoutColors.accentContainer, modifier = modifier) {
            CollectionIcon(icon = TuneScoutIcons.favoriteFilled)
        }

        is LibraryItemUiModel.DownloadedSongs -> TileBox(
            background = TuneScoutColors.accentContainer,
            modifier = modifier,
        ) {
            CollectionIcon(icon = TuneScoutIcons.downloaded)
        }

        is LibraryItemUiModel.Playlist -> PlaylistCover(
            artworkUrls = item.artworks.map(size::getUrl),
            modifier = modifier,
        )

        is LibraryItemUiModel.Album -> TileBox(background = TuneScoutColors.surfaceSubtle, modifier = modifier) {
            CoverImage(url = size.getUrl(item.artwork))
        }
    }
}

/** The square every item's artwork sits in, shaped like a playlist's cover so the list lines up. */
@Composable
private fun TileBox(
    background: Color,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(percent = COVER_CORNER_PERCENT))
            .background(background),
        contentAlignment = Alignment.Center,
        content = content,
    )
}

/** The lists the app keeps for the user wear a glyph of what they hold rather than their songs' covers. */
@Composable
private fun CollectionIcon(icon: ImageVector) {
    Icon(
        imageVector = icon,
        contentDescription = null,
        tint = TuneScoutColors.accent,
        modifier = Modifier.fillMaxSize(fraction = ICON_FRACTION),
    )
}
