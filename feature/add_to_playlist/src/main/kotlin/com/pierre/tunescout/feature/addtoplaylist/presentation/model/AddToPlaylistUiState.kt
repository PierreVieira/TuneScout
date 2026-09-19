package com.pierre.tunescout.feature.addtoplaylist.presentation.model

import com.pierre.tunescout.core.model.Playlist
import com.pierre.tunescout.core.model.Song

data class AddToPlaylistUiState(
    val song: Song?,
    val playlists: List<Playlist>,
    val newPlaylistName: String?,
) {
    val isPromptOpen: Boolean
        get() = newPlaylistName != null

    val canConfirmNewPlaylist: Boolean
        get() = !newPlaylistName.isNullOrBlank()
}
