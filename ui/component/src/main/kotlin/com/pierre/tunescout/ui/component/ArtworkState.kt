package com.pierre.tunescout.ui.component

import coil3.compose.AsyncImagePainter

/**
 * How far an artwork got. [EMPTY] is a song that names no artwork at all, and [FAILED] one whose
 * artwork was asked for and did not arrive — offline, that is the image the device never cached.
 */
enum class ArtworkState {
    LOADING,
    LOADED,
    EMPTY,
    FAILED,
    ;

    companion object {
        /** @return where an artwork at [url] starts, before the loader has said anything. */
        fun of(url: String): ArtworkState = if (url.isBlank()) EMPTY else LOADING

        /** @return what [painterState] means for an artwork at [url]. */
        fun of(
            painterState: AsyncImagePainter.State,
            url: String,
        ): ArtworkState = when (painterState) {
            is AsyncImagePainter.State.Loading -> LOADING
            is AsyncImagePainter.State.Success -> LOADED
            is AsyncImagePainter.State.Error -> if (url.isBlank()) EMPTY else FAILED
            AsyncImagePainter.State.Empty -> of(url)
        }
    }
}
