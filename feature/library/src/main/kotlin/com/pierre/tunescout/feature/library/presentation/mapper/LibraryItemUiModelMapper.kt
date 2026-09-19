package com.pierre.tunescout.feature.library.presentation.mapper

import com.pierre.tunescout.core.model.Playlist
import com.pierre.tunescout.core.model.Song
import com.pierre.tunescout.feature.library.presentation.model.LibraryItemUiModel

private const val MAX_COVER_ARTWORKS = 4

fun buildLibraryItems(
    favorites: List<Song>,
    playlists: List<Playlist>,
): List<LibraryItemUiModel> = buildList {
    add(favorites.toFavoritesItem())
    addAll(playlists.map(Playlist::toUiModel))
}

fun List<Song>.toFavoritesItem(): LibraryItemUiModel.Favorites = LibraryItemUiModel.Favorites(
    songCount = size,
    artworks = take(MAX_COVER_ARTWORKS).map(Song::artwork),
)

fun Playlist.toUiModel(): LibraryItemUiModel.Playlist = LibraryItemUiModel.Playlist(
    id = id,
    name = name,
    songCount = songCount,
    artworks = artworks.take(MAX_COVER_ARTWORKS),
)
