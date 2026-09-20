package com.pierre.tunescout.core.database

import com.pierre.tunescout.core.model.Album
import kotlinx.coroutines.flow.Flow
import kotlin.time.Duration

interface AlbumLocalDataSource {
    suspend fun save(album: Album)

    /**
     * The album as the device has it. One it never looked up is put together from the tracks it
     * saved on their own, marked incomplete, so the screen still opens with no connection.
     *
     * @return the cached album, a partial one when only some of its tracks are saved, or null when
     * none is.
     */
    fun observe(albumId: Long): Flow<Album?>

    /**
     * Whether the album was last written less than [maxAge] ago, so a screen that opens again
     * right after can draw the cached rows instead of waiting on a call that would return them.
     *
     * @return true while the cached album is that recent, and false when it is older or absent.
     */
    suspend fun isFresherThan(
        albumId: Long,
        maxAge: Duration,
    ): Boolean
}
