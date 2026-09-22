package com.pierre.tunescout.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.clearAndSetSemantics
import coil3.compose.AsyncImage
import coil3.compose.AsyncImagePainter
import coil3.compose.rememberAsyncImagePainter
import com.pierre.tunescout.ui.theme.TuneScoutColors

private const val QUADRANT_COUNT = 4
private const val PLACEHOLDER_FRACTION = 0.4f

/** The corner every cover drawn as a square tile rounds to, as a percent of its side. */
const val COVER_CORNER_PERCENT = 8

/**
 * A playlist has no cover of its own, so it wears the first four songs' artwork as a quadrant grid,
 * the way Spotify does. One song fills the tile, none falls back to the placeholder note.
 *
 * @param artworkUrls the playlist's songs' artwork, in its order, at the size the tile is drawn at.
 */
@Composable
fun PlaylistCover(
    artworkUrls: List<String>,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(percent = COVER_CORNER_PERCENT))
            .background(TuneScoutColors.surfaceSubtle),
        contentAlignment = Alignment.Center,
    ) {
        when {
            artworkUrls.isEmpty() -> ArtworkPlaceholderIcon(
                hasFailed = false,
                modifier = Modifier.fillMaxSize(fraction = PLACEHOLDER_FRACTION),
            )

            artworkUrls.size < QUADRANT_COUNT -> CoverImage(url = artworkUrls.first())

            else -> QuadrantGrid(artworkUrls = artworkUrls)
        }
    }
}

/**
 * Only the first tile speaks. Offline, each of the four would say its artwork is unavailable, and a
 * playlist's cover would be read as the same sentence four times over.
 */
@Composable
private fun QuadrantGrid(artworkUrls: List<String>) {
    Column(modifier = Modifier.fillMaxSize()) {
        artworkUrls.take(QUADRANT_COUNT).withIndex().chunked(2).forEach { pair ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
            ) {
                pair.forEach { (index, url) ->
                    val isFirst = index == 0
                    CoverImage(
                        url = url,
                        modifier = Modifier
                            .weight(1f)
                            .then(if (isFirst) Modifier else Modifier.clearAndSetSemantics {}),
                    )
                }
            }
        }
    }
}

/**
 * A cover square that fills its tile. One that did not arrive leaves the tile to the placeholder,
 * which says whether the image is simply missing or one connection away.
 *
 * A cover the tile showed before, at another size, stays up until the one at [url] arrives: a tile
 * that grows or shrinks between sizes would go blank halfway otherwise.
 */
@Composable
fun CoverImage(
    url: String,
    modifier: Modifier = Modifier,
) {
    val previousCover = rememberPreviousUrl(url)?.let { previousUrl -> rememberAsyncImagePainter(model = previousUrl) }
    var state by remember(url) { mutableStateOf(ArtworkState.of(url)) }
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        AsyncImage(
            model = url,
            contentDescription = null,
            transform = { painterState ->
                if (painterState is AsyncImagePainter.State.Loading && previousCover != null) {
                    painterState.copy(painter = previousCover)
                } else {
                    painterState
                }
            },
            contentScale = ContentScale.Crop,
            onState = { newState -> state = ArtworkState.of(painterState = newState, url = url) },
            modifier = Modifier.fillMaxSize(),
        )
        if (state == ArtworkState.FAILED || state == ArtworkState.EMPTY) {
            ArtworkPlaceholderIcon(
                hasFailed = state == ArtworkState.FAILED,
                modifier = Modifier.fillMaxSize(fraction = PLACEHOLDER_FRACTION),
            )
        }
    }
}

/** @return the url this tile drew before [url] became its own, or null while [url] is its first. */
@Composable
private fun rememberPreviousUrl(url: String): String? {
    val urls = remember { CoverUrls(url) }
    urls.update(url)
    return urls.previous
}

/**
 * The url a tile draws and the one it drew before, kept outside the snapshot: both are only read
 * while the tile composes, and a change of [current] is what recomposes it.
 *
 * @property current the url the tile draws now.
 */
private class CoverUrls(
    private var current: String,
) {
    var previous: String? = null
        private set

    fun update(url: String) {
        if (url == current) return
        previous = current
        current = url
    }
}
