package com.pierre.tunescout.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.pierre.tunescout.ui.component.shimmer.ShimmerBox
import com.pierre.tunescout.ui.theme.TuneScoutColors
import com.pierre.tunescout.ui.utils.animation.sharedArtwork

private val placeholderIconSize = 48.dp

@Composable
fun Artwork(
    url: String,
    contentDescription: String?,
    cornerPercent: Int,
    modifier: Modifier = Modifier,
    sharedKey: SongSharedKey? = null,
) {
    val shape = RoundedCornerShape(percent = cornerPercent)
    var state by remember(url) { mutableStateOf(ArtworkState.of(url)) }
    Box(
        modifier = modifier
            .sharedArtwork(sharedKey)
            .aspectRatio(1f)
            .clip(shape)
            .background(TuneScoutColors.surfaceSubtle),
        contentAlignment = Alignment.Center,
    ) {
        AsyncImage(
            model = url,
            contentDescription = contentDescription,
            contentScale = ContentScale.Crop,
            onState = { newState -> state = ArtworkState.of(painterState = newState, url = url) },
            modifier = Modifier.fillMaxSize(),
        )
        when (state) {
            ArtworkState.LOADING -> ShimmerBox(shape = shape, modifier = Modifier.fillMaxSize())

            ArtworkState.EMPTY, ArtworkState.FAILED -> ArtworkPlaceholderIcon(
                hasFailed = state == ArtworkState.FAILED,
                modifier = Modifier.size(placeholderIconSize),
            )

            ArtworkState.LOADED -> Unit
        }
    }
}
