package com.pierre.tunescout.feature.library.presentation.model

import com.pierre.tunescout.core.model.Artwork
import com.pierre.tunescout.core.model.LibraryItemKey

sealed interface LibraryItemUiModel {
    val key: LibraryItemKey
    val songCount: Int
    val artworks: List<Artwork>

    data class Favorites(
        override val songCount: Int,
        override val artworks: List<Artwork>,
    ) : LibraryItemUiModel {
        override val key: LibraryItemKey
            get() = LibraryItemKey.Favorites
    }

    data class Playlist(
        val id: Long,
        val name: String,
        override val songCount: Int,
        override val artworks: List<Artwork>,
    ) : LibraryItemUiModel {
        override val key: LibraryItemKey
            get() = LibraryItemKey.Playlist(id)
    }
}
