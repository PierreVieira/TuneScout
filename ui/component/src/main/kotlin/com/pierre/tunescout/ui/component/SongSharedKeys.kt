package com.pierre.tunescout.ui.component

enum class SongSharedElement {
    ARTWORK,
    TITLE,
    ARTIST,
}

data class SongSharedKey(
    val songId: Long,
    val element: SongSharedElement,
)

fun getSongSharedKey(
    songId: Long?,
    element: SongSharedElement,
): SongSharedKey? = songId?.let { id -> SongSharedKey(songId = id, element = element) }
