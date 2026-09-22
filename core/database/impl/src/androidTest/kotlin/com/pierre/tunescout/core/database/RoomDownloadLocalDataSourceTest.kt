package com.pierre.tunescout.core.database

import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import com.pierre.tunescout.core.database.internal.RoomAlbumLocalDataSource
import com.pierre.tunescout.core.database.internal.RoomDownloadLocalDataSource
import com.pierre.tunescout.core.database.internal.RoomFavoriteSongLocalDataSource
import com.pierre.tunescout.core.database.internal.RoomPlaylistLocalDataSource
import com.pierre.tunescout.core.model.LibraryItemKey
import com.pierre.tunescout.core.testing.fixture.album
import com.pierre.tunescout.core.testing.fixture.song
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class RoomDownloadLocalDataSourceTest {
    private lateinit var database: TuneScoutDatabase
    private lateinit var downloads: DownloadLocalDataSource
    private lateinit var playlists: PlaylistLocalDataSource
    private lateinit var favorites: FavoriteSongLocalDataSource
    private lateinit var albums: AlbumLocalDataSource
    private var now: Long = 0

    @BeforeEach
    fun setUp() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        database = Room
            .inMemoryDatabaseBuilder<TuneScoutDatabase>(context)
            .setDriver(BundledSQLiteDriver())
            .build()
        downloads = RoomDownloadLocalDataSource(
            downloadDao = database.downloadDao(),
            songDao = database.songDao(),
            timestampProvider = { ++now },
        )
        playlists = RoomPlaylistLocalDataSource(
            playlistDao = database.playlistDao(),
            songDao = database.songDao(),
            timestampProvider = { ++now },
        )
        favorites = RoomFavoriteSongLocalDataSource(
            favoriteSongDao = database.favoriteSongDao(),
            songDao = database.songDao(),
            timestampProvider = { ++now },
        )
        albums = RoomAlbumLocalDataSource(
            albumDao = database.albumDao(),
            songDao = database.songDao(),
            timestampProvider = { ++now },
        )
    }

    @AfterEach
    fun tearDown() {
        database.close()
    }

    @Test
    fun aSongAskedForOnItsOwnIsWanted() {
        runBlocking {
            // When
            downloads.addSong(song(id = 1))

            // Then
            assertThat(wantedIds()).containsExactly(1L)
        }
    }

    @Test
    fun onlyTheSongsAskedForOnTheirOwnAreTheirOwnLatestFirst() {
        runBlocking {
            // Given
            albums.save(album(id = 10, songs = listOf(song(id = 1, albumId = 10))))
            downloads.addCollection(LibraryItemKey.Album(albumId = 10))

            // When
            downloads.addSong(song(id = 2))
            downloads.addSong(song(id = 3))

            // Then
            assertThat(downloads.observeOwnSongs().first().map { song -> song.id }).containsExactly(3L, 2L).inOrder()
        }
    }

    @Test
    fun aDownloadedAlbumWantsEveryTrackTheDeviceHasOfIt() {
        runBlocking {
            // Given
            albums.save(album(id = 10, songs = listOf(song(id = 1, albumId = 10), song(id = 2, albumId = 10))))
            favorites.add(song(id = 3, albumId = 99))

            // When
            downloads.addCollection(LibraryItemKey.Album(albumId = 10))

            // Then
            assertThat(wantedIds()).containsExactly(1L, 2L)
        }
    }

    @Test
    fun aSongAddedToADownloadedPlaylistIsWantedWithItAndOneTakenOutIsNot() {
        runBlocking {
            // Given
            val playlistId = playlists.create(name = "Road trip")
            playlists.addSong(playlistId = playlistId, song = song(id = 1))
            downloads.addCollection(LibraryItemKey.Playlist(playlistId = playlistId))

            // When
            playlists.addSong(playlistId = playlistId, song = song(id = 2))
            playlists.removeSong(playlistId = playlistId, songId = 1)

            // Then
            assertThat(wantedIds()).containsExactly(2L)
        }
    }

    @Test
    fun theLikedSongsAreOnlyWantedWhileTheLikedSongsAreDownloaded() {
        runBlocking {
            // Given
            favorites.add(song(id = 1))
            val beforeTheRequest = wantedIds()

            // When
            downloads.addCollection(LibraryItemKey.Favorites)
            favorites.add(song(id = 2))

            // Then
            assertThat(beforeTheRequest).isEmpty()
            assertThat(wantedIds()).containsExactly(1L, 2L)
        }
    }

    @Test
    fun aSongNoLongerLikedLeavesTheDownloadedLikedSongs() {
        runBlocking {
            // Given
            favorites.add(song(id = 1))
            favorites.add(song(id = 2))
            downloads.addCollection(LibraryItemKey.Favorites)

            // When
            favorites.remove(songId = 1)

            // Then
            assertThat(wantedIds()).containsExactly(2L)
        }
    }

    @Test
    fun aSongHeldByTwoRequestsIsListedOnceAndTakingEitherBackTakesItOffTheDevice() {
        runBlocking {
            // Given
            favorites.add(song(id = 1))
            downloads.addCollection(LibraryItemKey.Favorites)
            downloads.addSong(song(id = 1))
            val heldTwice = wantedIds()

            // When
            downloads.removeSong(songId = 1)

            // Then
            assertThat(heldTwice).containsExactly(1L)
            assertThat(downloads.observeIsWanted(songId = 1).first()).isFalse()
        }
    }

    @Test
    fun aSongNothingHoldsAnyMoreIsNotWanted() {
        runBlocking {
            // Given
            downloads.addSong(song(id = 1))

            // When
            downloads.removeSong(songId = 1)

            // Then
            assertThat(downloads.observeIsWanted(songId = 1).first()).isFalse()
        }
    }

    @Test
    fun aSongKeptOnlyByADownloadedAlbumIsTakenOffTheDeviceWhenItsOwnDownloadIsTakenBack() {
        runBlocking {
            // Given
            albums.save(album(id = 10, songs = listOf(song(id = 1, albumId = 10))))
            downloads.addCollection(LibraryItemKey.Album(albumId = 10))
            val heldByTheAlbum = wantedIds()

            // When
            downloads.removeSong(songId = 1)

            // Then
            assertThat(heldByTheAlbum).containsExactly(1L)
            assertThat(wantedIds()).isEmpty()
        }
    }

    @Test
    fun aSongExcludedFromADownloadedAlbumIsWantedAgainOnceItIsAskedForOnItsOwn() {
        runBlocking {
            // Given
            albums.save(album(id = 10, songs = listOf(song(id = 1, albumId = 10))))
            downloads.addCollection(LibraryItemKey.Album(albumId = 10))
            downloads.removeSong(songId = 1)

            // When
            downloads.addSong(song(id = 1, albumId = 10))

            // Then
            assertThat(wantedIds()).containsExactly(1L)
        }
    }

    @Test
    fun undoingAnAlbumDownloadTakesOffTheDeviceItsSongsDownloadedOnTheirOwnToo() {
        runBlocking {
            // Given
            albums.save(album(id = 10, songs = listOf(song(id = 1, albumId = 10), song(id = 2, albumId = 10))))
            downloads.addSong(song(id = 1, albumId = 10))
            downloads.addSong(song(id = 3, albumId = 99))
            downloads.addCollection(LibraryItemKey.Album(albumId = 10))

            // When
            downloads.removeCollection(LibraryItemKey.Album(albumId = 10))

            // Then
            assertThat(wantedIds()).containsExactly(3L)
            assertThat(downloads.observeOwnSongs().first().map { song -> song.id }).containsExactly(3L)
        }
    }

    @Test
    fun undoingAPlaylistDownloadTakesOffTheDeviceItsSongsDownloadedOnTheirOwnToo() {
        runBlocking {
            // Given
            val playlistId = playlists.create(name = "Road trip")
            playlists.addSong(playlistId = playlistId, song = song(id = 1))
            downloads.addSong(song(id = 1))
            downloads.addSong(song(id = 2))
            downloads.addCollection(LibraryItemKey.Playlist(playlistId = playlistId))

            // When
            downloads.removeCollection(LibraryItemKey.Playlist(playlistId = playlistId))

            // Then
            assertThat(wantedIds()).containsExactly(2L)
        }
    }

    @Test
    fun undoingTheLikedSongsDownloadTakesOffTheDeviceTheLikedSongsDownloadedOnTheirOwnToo() {
        runBlocking {
            // Given
            favorites.add(song(id = 1))
            downloads.addSong(song(id = 1))
            downloads.addSong(song(id = 2))
            downloads.addCollection(LibraryItemKey.Favorites)

            // When
            downloads.removeCollection(LibraryItemKey.Favorites)

            // Then
            assertThat(wantedIds()).containsExactly(2L)
        }
    }

    @Test
    fun undoingAnAlbumDownloadKeepsASongAnotherDownloadedCollectionStillHolds() {
        runBlocking {
            // Given
            albums.save(album(id = 10, songs = listOf(song(id = 1, albumId = 10))))
            val playlistId = playlists.create(name = "Road trip")
            playlists.addSong(playlistId = playlistId, song = song(id = 1, albumId = 10))
            downloads.addSong(song(id = 1, albumId = 10))
            downloads.addCollection(LibraryItemKey.Album(albumId = 10))
            downloads.addCollection(LibraryItemKey.Playlist(playlistId = playlistId))

            // When
            downloads.removeCollection(LibraryItemKey.Album(albumId = 10))

            // Then
            assertThat(wantedIds()).containsExactly(1L)
        }
    }

    @Test
    fun deletingADownloadedPlaylistTakesItsRequestWithIt() {
        runBlocking {
            // Given
            val playlistId = playlists.create(name = "Road trip")
            downloads.addCollection(LibraryItemKey.Playlist(playlistId = playlistId))

            // When
            playlists.delete(playlistId)

            // Then
            assertThat(downloads.observeCollections().first()).isEmpty()
        }
    }

    private suspend fun wantedIds(): List<Long> = downloads.observeWantedSongs().first().map { song -> song.id }
}
