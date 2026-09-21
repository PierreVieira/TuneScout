package com.pierre.tunescout.core.database

import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import com.pierre.tunescout.core.database.internal.RoomAlbumLocalDataSource
import com.pierre.tunescout.core.database.internal.RoomDownloadLocalDataSource
import com.pierre.tunescout.core.database.internal.RoomFavoriteSongLocalDataSource
import com.pierre.tunescout.core.database.internal.RoomPlaybackSessionLocalDataSource
import com.pierre.tunescout.core.database.internal.RoomPlaylistLocalDataSource
import com.pierre.tunescout.core.database.internal.RoomRecentlyPlayedLocalDataSource
import com.pierre.tunescout.core.database.internal.RoomSongLocalDataSource
import com.pierre.tunescout.core.model.LibraryItemKey
import com.pierre.tunescout.core.model.PlaybackSession
import com.pierre.tunescout.core.model.RepeatMode
import com.pierre.tunescout.core.model.Song
import com.pierre.tunescout.core.testing.fixture.album
import com.pierre.tunescout.core.testing.fixture.queueEntries
import com.pierre.tunescout.core.testing.fixture.song
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import kotlin.time.Duration

class RoomSongLocalDataSourceTest {
    private lateinit var database: TuneScoutDatabase
    private lateinit var dataSource: SongLocalDataSource
    private var now: Long = 0

    @AfterEach
    fun tearDown() {
        database.close()
    }

    @Test
    fun aSearchMatchesTheTitleTheArtistAndTheAlbum() {
        runBlocking {
            // Given
            prepareScenario(
                cached = listOf(
                    song(id = 1, title = "Get Lucky", artistName = "Daft Punk", albumTitle = "Random Access Memories"),
                    song(id = 2, title = "Digital Love", artistName = "Daft Punk", albumTitle = "Discovery"),
                    song(id = 3, title = "Take Five", artistName = "Dave Brubeck", albumTitle = "Time Out"),
                ),
            )

            // When
            val byTitle = dataSource.findByTerm(term = "lucky", limit = 10)
            val byArtist = dataSource.findByTerm(term = "daft", limit = 10)
            val byAlbum = dataSource.findByTerm(term = "Time Out", limit = 10)

            // Then
            assertThat(byTitle.map { song -> song.id }).containsExactly(1L)
            assertThat(byArtist.map { song -> song.id }).containsExactly(1L, 2L)
            assertThat(byAlbum.map { song -> song.id }).containsExactly(3L)
        }
    }

    @Test
    fun aSearchThatMatchesNothingComesBackEmpty() {
        runBlocking {
            // Given
            prepareScenario(cached = listOf(song(id = 1, title = "Get Lucky")))

            // When
            val found = dataSource.findByTerm(term = "nothing like this", limit = 10)

            // Then
            assertThat(found).isEmpty()
        }
    }

    @Test
    fun aSearchReturnsTheMostRecentlyCachedMatchesFirstAndHonoursTheLimit() {
        runBlocking {
            // Given
            prepareScenario()
            dataSource.save(listOf(song(id = 1, title = "Lucky One")))
            dataSource.save(listOf(song(id = 2, title = "Lucky Two")))
            dataSource.save(listOf(song(id = 3, title = "Lucky Three")))

            // When
            val found = dataSource.findByTerm(term = "lucky", limit = 2)

            // Then
            assertThat(found.map { song -> song.id }).containsExactly(3L, 2L).inOrder()
        }
    }

    @Test
    fun savingMoreSongsThanTheCacheKeepsDropsTheOldestOnes() {
        runBlocking {
            // Given
            prepareScenario(maxCachedSongs = 2)

            // When
            dataSource.save(listOf(song(id = 1)))
            dataSource.save(listOf(song(id = 2)))
            dataSource.save(listOf(song(id = 3)))

            // Then
            assertThat(cachedIds()).containsExactly(2L, 3L)
        }
    }

    @Test
    fun trimmingTheCacheKeepsTheSongsTheHistoryPointsAt() {
        runBlocking {
            // Given
            prepareScenario(maxCachedSongs = 1)
            recentlyPlayed().record(song(id = 1))

            // When
            dataSource.save(listOf(song(id = 2)))
            dataSource.save(listOf(song(id = 3)))

            // Then
            assertThat(cachedIds()).contains(1L)
        }
    }

    @Test
    fun trimmingTheCacheKeepsTheLikedSongs() {
        runBlocking {
            // Given
            prepareScenario(maxCachedSongs = 1)
            favorites().add(song(id = 1))

            // When
            dataSource.save(listOf(song(id = 2)))
            dataSource.save(listOf(song(id = 3)))

            // Then
            assertThat(cachedIds()).contains(1L)
        }
    }

    @Test
    fun trimmingTheCacheKeepsTheSongsInAPlaylist() {
        runBlocking {
            // Given
            prepareScenario(maxCachedSongs = 1)
            val playlists = playlists()
            playlists.addSong(playlistId = playlists.create(name = "Road trip"), song = song(id = 1))

            // When
            dataSource.save(listOf(song(id = 2)))
            dataSource.save(listOf(song(id = 3)))

            // Then
            assertThat(cachedIds()).contains(1L)
        }
    }

    @Test
    fun trimmingTheCacheKeepsTheSongsOfACachedAlbum() {
        runBlocking {
            // Given
            prepareScenario(maxCachedSongs = 1)
            albums().save(album(id = 10, songs = listOf(song(id = 1, albumId = 10))))

            // When
            dataSource.save(listOf(song(id = 2, albumId = 99)))
            dataSource.save(listOf(song(id = 3, albumId = 99)))

            // Then
            assertThat(cachedIds()).contains(1L)
        }
    }

    @Test
    fun trimmingTheCacheKeepsTheSongsOfTheSavedQueue() {
        runBlocking {
            // Given
            prepareScenario(maxCachedSongs = 1)
            sessions().save(
                PlaybackSession(
                    entries = queueEntries(songs = listOf(song(id = 1))),
                    currentEntryId = "entry-1",
                    context = null,
                    position = Duration.ZERO,
                    repeatMode = RepeatMode.Off,
                    isShuffleEnabled = false,
                    unshuffledOrder = emptyList(),
                    hasEnded = false,
                ),
            )

            // When
            dataSource.save(listOf(song(id = 2)))
            dataSource.save(listOf(song(id = 3)))

            // Then
            assertThat(cachedIds()).contains(1L)
        }
    }

    @Test
    fun trimmingTheCacheKeepsTheSongsDownloadedOnTheirOwn() {
        runBlocking {
            // Given
            prepareScenario(maxCachedSongs = 1)
            downloads().addSong(song(id = 1))

            // When
            dataSource.save(listOf(song(id = 2)))
            dataSource.save(listOf(song(id = 3)))

            // Then
            assertThat(cachedIds()).contains(1L)
        }
    }

    @Test
    fun trimmingTheCacheKeepsTheSongsOfADownloadedAlbumThatWasNeverLookedUp() {
        runBlocking {
            // Given
            prepareScenario(maxCachedSongs = 1)
            dataSource.save(listOf(song(id = 1, albumId = 10)))
            downloads().addCollection(LibraryItemKey.Album(albumId = 10))

            // When
            dataSource.save(listOf(song(id = 2, albumId = 99)))
            dataSource.save(listOf(song(id = 3, albumId = 99)))

            // Then
            assertThat(cachedIds()).contains(1L)
        }
    }

    private suspend fun cachedIds(): List<Long> =
        database.songDao().findByTerm(term = "", limit = 100).map { entity -> entity.id }

    private fun recentlyPlayed(): RecentlyPlayedLocalDataSource = RoomRecentlyPlayedLocalDataSource(
        recentlyPlayedDao = database.recentlyPlayedDao(),
        songDao = database.songDao(),
        timestampProvider = { ++now },
        maxEntries = MAX_RECENTLY_PLAYED,
    )

    private fun favorites(): FavoriteSongLocalDataSource = RoomFavoriteSongLocalDataSource(
        favoriteSongDao = database.favoriteSongDao(),
        songDao = database.songDao(),
        timestampProvider = { ++now },
    )

    private fun playlists(): PlaylistLocalDataSource = RoomPlaylistLocalDataSource(
        playlistDao = database.playlistDao(),
        songDao = database.songDao(),
        timestampProvider = { ++now },
    )

    private fun albums(): AlbumLocalDataSource = RoomAlbumLocalDataSource(
        albumDao = database.albumDao(),
        songDao = database.songDao(),
        timestampProvider = { ++now },
    )

    private fun downloads(): DownloadLocalDataSource = RoomDownloadLocalDataSource(
        downloadDao = database.downloadDao(),
        songDao = database.songDao(),
        timestampProvider = { ++now },
    )

    private fun sessions(): PlaybackSessionLocalDataSource = RoomPlaybackSessionLocalDataSource(
        playbackSessionDao = database.playbackSessionDao(),
        timestampProvider = { ++now },
    )

    private suspend fun prepareScenario(
        cached: List<Song> = emptyList(),
        maxCachedSongs: Int = MAX_CACHED_SONGS,
    ) {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        database = Room
            .inMemoryDatabaseBuilder<TuneScoutDatabase>(context)
            .setDriver(BundledSQLiteDriver())
            .build()
        dataSource = RoomSongLocalDataSource(
            songDao = database.songDao(),
            timestampProvider = { ++now },
            maxCachedSongs = maxCachedSongs,
        )
        cached.forEach { song -> dataSource.save(listOf(song)) }
    }

    private companion object {
        const val MAX_CACHED_SONGS = 500
        const val MAX_RECENTLY_PLAYED = 20
    }
}
