package com.pierre.tunescout.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.compose.AsyncImagePainter
import com.pierre.tunescout.ui.component.shimmer.ShimmerBox
import com.pierre.tunescout.ui.theme.TuneScoutColors

private val placeholderIconSize = 48.dp

@Composable
fun Artwork(
    url: String,
    contentDescription: String?,
    cornerRadius: Dp,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(cornerRadius)
    var state by remember(url) { mutableStateOf(getInitialArtworkState(url)) }
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(shape)
            .background(TuneScoutColors.surfaceSubtle),
        contentAlignment = Alignment.Center,
    ) {
        AsyncImage(
            model = url,
            contentDescription = contentDescription,
            contentScale = ContentScale.Crop,
            onState = { newState -> state = newState.toArtworkState(url) },
            modifier = Modifier.fillMaxSize(),
        )
        when (state) {
            ArtworkState.LOADING -> ShimmerBox(shape = shape, modifier = Modifier.fillMaxSize())
            ArtworkState.EMPTY -> PlaceholderIcon()
            ArtworkState.LOADED -> Unit
        }
    }
}

@Composable
private fun PlaceholderIcon() {
    Icon(
        imageVector = TuneScoutIcons.musicList,
        contentDescription = null,
        tint = TuneScoutColors.elementPlaceholder,
        modifier = Modifier.size(placeholderIconSize),
    )
}

private enum class ArtworkState {
    LOADING,
    LOADED,
    EMPTY,
}

private fun getInitialArtworkState(url: String): ArtworkState =
    if (url.isBlank()) ArtworkState.EMPTY else ArtworkState.LOADING

private fun AsyncImagePainter.State.toArtworkState(url: String): ArtworkState = when (this) {
    is AsyncImagePainter.State.Loading -> ArtworkState.LOADING
    is AsyncImagePainter.State.Success -> ArtworkState.LOADED
    is AsyncImagePainter.State.Error -> ArtworkState.EMPTY
    AsyncImagePainter.State.Empty -> getInitialArtworkState(url)
}
