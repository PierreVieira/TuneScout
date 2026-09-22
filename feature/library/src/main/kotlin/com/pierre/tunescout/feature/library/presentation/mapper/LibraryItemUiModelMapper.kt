package com.pierre.tunescout.feature.library.presentation.mapper

import com.pierre.tunescout.core.model.AlbumSummary
import com.pierre.tunescout.core.model.Playlist
import com.pierre.tunescout.core.model.Song
import com.pierre.tunescout.feature.library.presentation.model.LibraryItemUiModel

private const val MAX_COVER_ARTWORKS = 4

class LibraryItemUiModelMapper {
    /**
     * @return the library in the order it is listed: the liked songs, the songs downloaded one by one
     * — only while there is one at least — the playlists, then the albums.
     */
    fun buildLibraryItems(
        favorites: List<Song>,
        playlists: List<Playlist>,
        albums: List<AlbumSummary>,
        downloadedSongs: List<Song>,
    ): List<LibraryItemUiModel> = buildList {
        add(favorites.toFavoritesItem())
        if (downloadedSongs.isNotEmpty()) add(downloadedSongs.toDownloadedSongsItem())
        addAll(playlists.map(Playlist::toUiModel))
        addAll(albums.map(AlbumSummary::toUiModel))
    }
}

fun List<Song>.toFavoritesItem(): LibraryItemUiModel.Favorites = LibraryItemUiModel.Favorites(
    songCount = size,
    artworks = take(MAX_COVER_ARTWORKS).map(Song::artwork),
)

fun List<Song>.toDownloadedSongsItem(): LibraryItemUiModel.DownloadedSongs = LibraryItemUiModel.DownloadedSongs(
    songCount = size,
    artworks = take(MAX_COVER_ARTWORKS).map(Song::artwork),
)

fun Playlist.toUiModel(): LibraryItemUiModel.Playlist = LibraryItemUiModel.Playlist(
    id = id,
    name = name,
    songCount = songCount,
    artworks = artworks.take(MAX_COVER_ARTWORKS),
)

fun AlbumSummary.toUiModel(): LibraryItemUiModel.Album = LibraryItemUiModel.Album(
    id = id,
    title = title,
    artistName = artistName,
    artwork = artwork,
)
