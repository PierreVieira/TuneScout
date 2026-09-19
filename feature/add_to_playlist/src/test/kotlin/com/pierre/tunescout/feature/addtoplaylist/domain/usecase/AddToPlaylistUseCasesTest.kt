package com.pierre.tunescout.feature.addtoplaylist.domain.usecase

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.pierre.tunescout.core.model.Playlist
import com.pierre.tunescout.core.model.Song
import com.pierre.tunescout.core.testing.fixture.playlist
import com.pierre.tunescout.core.testing.fixture.song
import com.pierre.tunescout.feature.addtoplaylist.domain.repository.AddToPlaylistRepository
import com.pierre.tunescout.feature.addtoplaylist.domain.usecase.impl.AddSongToPlaylistUseCase
import com.pierre.tunescout.feature.addtoplaylist.domain.usecase.impl.CreatePlaylistWithSongUseCase
import com.pierre.tunescout.feature.addtoplaylist.domain.usecase.impl.ObservePlaylistsUseCase
import com.pierre.tunescout.feature.addtoplaylist.domain.usecase.impl.ObserveSongUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class AddToPlaylistUseCasesTest {
    private lateinit var repository: FakeAddToPlaylistRepository

    @Test
    fun `GIVEN playlists in the library WHEN observing them THEN they come back in order`() = runTest {
        // Given
        val playlists = listOf(playlist(id = 1), playlist(id = 2, name = "Focus"))
        prepareScenario(playlists = playlists)

        // When
        val observed = ObservePlaylistsUseCase(repository)()

        // Then
        observed.test {
            assertThat(awaitItem()).containsExactlyElementsIn(playlists).inOrder()
            awaitComplete()
        }
    }

    @Test
    fun `GIVEN the song the sheet was opened for WHEN observing it THEN it comes back`() = runTest {
        // Given
        prepareScenario(song = song(id = 3, title = "Get Lucky"))

        // When
        val observed = ObserveSongUseCase(repository)(songId = 3)

        // Then
        observed.test {
            assertThat(awaitItem()?.title).isEqualTo("Get Lucky")
            awaitComplete()
        }
        assertThat(repository.observedSongIds).containsExactly(3L)
    }

    @Test
    fun `WHEN adding a song to a playlist THEN the playlist takes it`() = runTest {
        // Given
        prepareScenario()

        // When
        AddSongToPlaylistUseCase(repository)(playlistId = 7, song = song(id = 1))

        // Then
        assertThat(repository.addedSongs).containsExactly(7L to 1L)
    }

    @Test
    fun `WHEN creating a playlist for a song THEN the song lands in the playlist just created`() = runTest {
        // Given
        prepareScenario(createdPlaylistId = 42)

        // When
        CreatePlaylistWithSongUseCase(repository)(name = "Road trip", song = song(id = 1))

        // Then
        assertThat(repository.createdNames).containsExactly("Road trip")
        assertThat(repository.addedSongs).containsExactly(42L to 1L)
    }

    private fun prepareScenario(
        playlists: List<Playlist> = emptyList(),
        song: Song? = null,
        createdPlaylistId: Long = 1,
    ) {
        repository = FakeAddToPlaylistRepository(
            playlists = playlists,
            song = song,
            createdPlaylistId = createdPlaylistId,
        )
    }
}

private class FakeAddToPlaylistRepository(
    private val playlists: List<Playlist>,
    private val song: Song?,
    private val createdPlaylistId: Long,
) : AddToPlaylistRepository {
    val observedSongIds = mutableListOf<Long>()
    val createdNames = mutableListOf<String>()
    val addedSongs = mutableListOf<Pair<Long, Long>>()

    override fun observePlaylists(): Flow<List<Playlist>> = flowOf(playlists)

    override fun observeSong(songId: Long): Flow<Song?> {
        observedSongIds += songId
        return flowOf(song)
    }

    override suspend fun addSong(
        playlistId: Long,
        song: Song,
    ) {
        addedSongs += playlistId to song.id
    }

    override suspend fun createPlaylist(name: String): Long {
        createdNames += name
        return createdPlaylistId
    }
}
