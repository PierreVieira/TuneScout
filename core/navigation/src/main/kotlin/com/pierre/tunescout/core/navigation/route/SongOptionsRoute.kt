package com.pierre.tunescout.core.navigation.route

import kotlinx.serialization.Serializable

/**
 * @property songId the song the sheet acts on.
 * @property playlistId the playlist the sheet was opened from, which it then offers to take the
 * song out of; null anywhere else.
 */
@Serializable
data class SongOptionsRoute(
    val songId: Long,
    val playlistId: Long? = null,
) : OverlayRoute
