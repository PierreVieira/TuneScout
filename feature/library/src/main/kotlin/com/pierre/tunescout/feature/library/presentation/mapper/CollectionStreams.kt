package com.pierre.tunescout.feature.library.presentation.mapper

import com.pierre.tunescout.core.model.Playlist
import com.pierre.tunescout.core.model.Song
import com.pierre.tunescout.feature.library.domain.model.CollectionKey
import com.pierre.tunescout.feature.library.domain.usecase.CollectionUseCases
import com.pierre.tunescout.feature.library.presentation.model.CollectionTitle
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

class CollectionStreams(
    private val useCases: CollectionUseCases,
) {
    fun observeSongs(key: CollectionKey): Flow<List<Song>> = when (key) {
        CollectionKey.Favorites -> useCases.observeFavorites()
        is CollectionKey.Playlist -> useCases.observePlaylistSongs(key.playlistId)
    }

    fun observeFavoriteSongIds(): Flow<Set<Long>> = useCases
        .observeFavorites()
        .map { songs -> songs.mapTo(mutableSetOf()) { song -> song.id } }

    fun observeTitle(key: CollectionKey): Flow<CollectionTitle?> = when (key) {
        CollectionKey.Favorites -> flowOf(CollectionTitle.Favorites)

        is CollectionKey.Playlist ->
            useCases
                .observePlaylist(key.playlistId)
                .map { playlist -> playlist?.let(::toCustomTitle) }
    }

    private fun toCustomTitle(playlist: Playlist): CollectionTitle = CollectionTitle.Custom(playlist.name)
}
