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

/**
 * A key may only have one source. The song that is playing is drawn twice — once in the list and
 * once in the mini player bar — so the row gives the key up and lets the bar fly it to the player.
 */
fun getRowSharedSongId(
    songId: Long,
    nowPlayingId: Long?,
): Long? = songId.takeIf { it != nowPlayingId }
