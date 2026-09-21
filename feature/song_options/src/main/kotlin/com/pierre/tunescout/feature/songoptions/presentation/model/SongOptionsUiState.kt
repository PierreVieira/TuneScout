package com.pierre.tunescout.feature.songoptions.presentation.model

import com.pierre.tunescout.core.model.Song

/**
 * @property song the song the sheet acts on, once the device has it.
 * @property isFavorite whether the song is liked.
 * @property isDownloaded whether anything keeps the song on the device: its own request, or an
 * album or playlist downloaded with it.
 * @property isRemovableFromPlaylist the sheet was opened from a playlist, so it offers to take the
 * song out of it.
 * @property isReorderable the sheet was opened from a list the user can reorder — an album or a
 * playlist — so it offers to start reordering it.
 */
data class SongOptionsUiState(
    val song: Song?,
    val isFavorite: Boolean,
    val isDownloaded: Boolean,
    val isRemovableFromPlaylist: Boolean,
    val isReorderable: Boolean,
)
