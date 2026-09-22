package com.pierre.tunescout.feature.album.presentation.model

import com.pierre.tunescout.core.model.Album
import com.pierre.tunescout.core.playback.DuplicatesInQueue

/**
 * @property album the album the sheet acts on, once the device has it.
 * @property duplicates the tracks the user already queued, while the sheet asks whether to queue
 * the whole album anyway. Null while nothing waits on that answer.
 */
data class AlbumOptionsUiState(
    val album: Album?,
    val duplicates: DuplicatesInQueue?,
)
