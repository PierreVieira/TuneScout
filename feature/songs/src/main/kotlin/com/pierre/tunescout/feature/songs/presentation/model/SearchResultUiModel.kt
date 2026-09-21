package com.pierre.tunescout.feature.songs.presentation.model

import com.pierre.tunescout.core.model.Song

/**
 * A search result next to what the player can do with it. Paging items never travel through
 * [SongsUiState], so the answer the other lists read there rides along with each result instead.
 *
 * @property song the song the row draws.
 * @property isUnavailable the player cannot reach the song right now — offline, one whose preview
 * never reached the device. The row is drawn dimmer, so a tap that is refused is seen coming.
 */
data class SearchResultUiModel(
    val song: Song,
    val isUnavailable: Boolean,
)
