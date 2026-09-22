package com.pierre.tunescout.feature.songs.presentation.model

import com.pierre.tunescout.core.model.NowPlaying
import com.pierre.tunescout.core.model.Song
import com.pierre.tunescout.core.model.SongDownloadStatus

/**
 * @property query what the user typed in the search field, or said to the audio search.
 * @property isAudioSearchAvailable the device can turn speech into text, so the microphone is offered.
 * @property recentlyPlayed the songs played last, newest first.
 * @property nowPlaying which song is playing, when one is.
 * @property songPendingRemoval the recent song asked to be removed, while the removal can still be undone.
 * @property songAlreadyQueued the song swiped into the queue while the user already had it queued
 * there, while the screen asks whether to add it again.
 * @property favoriteSongIds the songs the user liked, which is what a swipe toward the start of a row
 * — a recent one or a search result — would take back.
 * @property isOffline the device has no connection right now.
 * @property unplayableSongIds which of [recentlyPlayed] the player cannot reach right now — offline,
 * the ones whose preview never reached the device. Their rows are drawn dimmer, so a tap that is
 * refused is seen coming.
 * @property downloadStatuses how far each song the user asked to keep has got; a song absent from
 * it has no download.
 */
data class SongsUiState(
    val query: String,
    val isAudioSearchAvailable: Boolean,
    val recentlyPlayed: List<Song>,
    val nowPlaying: NowPlaying?,
    val songPendingRemoval: Song?,
    val songAlreadyQueued: Song?,
    val favoriteSongIds: Set<Long>,
    val isOffline: Boolean,
    val unplayableSongIds: Set<Long>,
    val downloadStatuses: Map<Long, SongDownloadStatus>,
) {
    val isSearching: Boolean
        get() = query.isNotBlank()
}
