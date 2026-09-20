package com.pierre.tunescout.core.playback.internal

import com.pierre.tunescout.core.database.FavoriteSongLocalDataSource
import com.pierre.tunescout.core.model.PlaybackState
import com.pierre.tunescout.core.model.Song
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

/**
 * Whether the song currently loaded in the player is one of the user's favorites.
 *
 * @property song the song the state is about.
 * @property isFavorite whether [song] is in the user's favorites right now.
 */
internal data class FavoriteButtonState(
    val song: Song,
    val isFavorite: Boolean,
)

internal class PlaybackFavoriteController(
    private val playbackState: Flow<PlaybackState>,
    private val favoriteSongLocalDataSource: FavoriteSongLocalDataSource,
) {
    fun observe(): Flow<FavoriteButtonState?> = playbackState
        .map { state -> state.currentSong }
        .distinctUntilChanged { previous, next -> previous?.id == next?.id }
        .flatMapLatest { song ->
            song
                ?.let { current ->
                    favoriteSongLocalDataSource
                        .observeIsFavorite(current.id)
                        .map { isFavorite -> FavoriteButtonState(current, isFavorite) }
                }
                ?: flowOf(null)
        }

    suspend fun toggle(state: FavoriteButtonState) {
        if (state.isFavorite) {
            favoriteSongLocalDataSource.remove(state.song.id)
        } else {
            favoriteSongLocalDataSource.add(state.song)
        }
    }
}
