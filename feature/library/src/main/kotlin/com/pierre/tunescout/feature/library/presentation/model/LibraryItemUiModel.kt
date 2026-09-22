package com.pierre.tunescout.feature.library.presentation.model

import com.pierre.tunescout.core.model.Artwork
import com.pierre.tunescout.core.model.LibraryItemKey
import com.pierre.tunescout.feature.library.domain.model.LibraryFilter

sealed interface LibraryItemUiModel {
    val key: LibraryItemKey
    val artworks: List<Artwork>

    /** The chip that picks this kind of item, one of the [LibraryFilter.isKind] ones. */
    val kind: LibraryFilter

    data class Favorites(
        val songCount: Int,
        override val artworks: List<Artwork>,
    ) : LibraryItemUiModel {
        override val key: LibraryItemKey
            get() = LibraryItemKey.Favorites

        override val kind: LibraryFilter
            get() = LibraryFilter.PLAYLISTS
    }

    data class Playlist(
        val id: Long,
        val name: String,
        val songCount: Int,
        override val artworks: List<Artwork>,
    ) : LibraryItemUiModel {
        override val key: LibraryItemKey
            get() = LibraryItemKey.Playlist(id)

        override val kind: LibraryFilter
            get() = LibraryFilter.PLAYLISTS
    }

    data class Album(
        val id: Long,
        val title: String,
        val artistName: String,
        val artwork: Artwork,
    ) : LibraryItemUiModel {
        override val key: LibraryItemKey
            get() = LibraryItemKey.Album(id)

        override val artworks: List<Artwork>
            get() = listOf(artwork)

        override val kind: LibraryFilter
            get() = LibraryFilter.ALBUMS
    }

    /**
     * The songs the user downloaded one by one. They are listed like a playlist, and only under the
     * downloaded chip: they are there because they were downloaded, not because the user made a list.
     *
     * @property songCount how many songs were downloaded one by one.
     * @property artworks the covers of the latest of them, up to four.
     */
    data class DownloadedSongs(
        val songCount: Int,
        override val artworks: List<Artwork>,
    ) : LibraryItemUiModel {
        override val key: LibraryItemKey
            get() = LibraryItemKey.DownloadedSongs

        override val kind: LibraryFilter
            get() = LibraryFilter.PLAYLISTS
    }
}
