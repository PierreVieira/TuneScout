package com.pierre.tunescout.core.navigation.route

import com.pierre.tunescout.core.navigation.reorder.ReorderTarget
import kotlinx.serialization.Serializable

/**
 * @property songId the song the sheet acts on.
 * @property playlistId the playlist the sheet was opened from, which it then offers to take the
 * song out of; null anywhere else.
 * @property reorderTarget the list the sheet was opened from, when the user can reorder it — an
 * album or a playlist — which the sheet then offers to start; null anywhere else.
 */
@Serializable
data class SongOptionsRoute(
    val songId: Long,
    val playlistId: Long? = null,
    val reorderTarget: ReorderTarget? = null,
) : OverlayRoute
