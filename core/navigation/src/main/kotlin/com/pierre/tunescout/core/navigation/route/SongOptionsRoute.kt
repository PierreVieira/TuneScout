package com.pierre.tunescout.core.navigation.route

import com.pierre.tunescout.core.navigation.reorder.ReorderTarget
import kotlinx.serialization.Serializable

/**
 * @property songId the song the sheet acts on.
 * @property playlistId the playlist the sheet was opened from, which it then offers to take the
 * song out of; null anywhere else.
 * @property reorderTarget the list the sheet was opened from, when the user can reorder it — an
 * album or a playlist — which the sheet then offers to start; null anywhere else.
 * @property hidesFavorite whether the sheet leaves liking out, for a caller that already shows it
 * of its own — the player, whose bar carries a filled heart the sheet would only duplicate.
 */
@Serializable
data class SongOptionsRoute(
    val songId: Long,
    val playlistId: Long? = null,
    val reorderTarget: ReorderTarget? = null,
    val hidesFavorite: Boolean = false,
) : OverlayRoute
