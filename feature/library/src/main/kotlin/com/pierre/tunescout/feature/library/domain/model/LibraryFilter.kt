package com.pierre.tunescout.feature.library.domain.model

/**
 * A chip above the library. Playlists and albums are what an item is, so one of them at most is on;
 * downloaded narrows whichever of them is, the way Spotify's chips combine.
 */
enum class LibraryFilter {
    PLAYLISTS,
    ALBUMS,
    DOWNLOADED,
    ;

    /** Whether the chip picks what kind of item to show, rather than narrowing the kind picked. */
    val isKind: Boolean
        get() = this != DOWNLOADED
}
