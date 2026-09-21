package com.pierre.tunescout

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isEnabled
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.google.common.truth.Truth.assertThat
import com.pierre.tunescout.core.database.PlaylistLocalDataSource
import com.pierre.tunescout.core.model.Song
import com.pierre.tunescout.core.network.SongSearchRemoteDataSource
import com.pierre.tunescout.core.testing.fixture.song
import de.mannodermaus.junit5.compose.createAndroidComposeExtension
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import org.koin.core.context.GlobalContext
import org.koin.core.context.loadKoinModules
import org.koin.core.context.unloadKoinModules
import org.koin.core.module.Module
import org.koin.dsl.module

@OptIn(ExperimentalTestApi::class)
class PlaylistSongRemovalFlowTest {
    @JvmField
    @RegisterExtension
    val compose = createAndroidComposeExtension<MainActivity>()

    private val playlists: PlaylistLocalDataSource
        get() = GlobalContext.get().get()

    private val fakeRemoteModule: Module = module {
        single<SongSearchRemoteDataSource> { FakeEmptyRemoteDataSource() }
    }

    private var playlistId: Long = 0

    @BeforeEach
    fun setUp() {
        loadKoinModules(fakeRemoteModule)
        runBlocking {
            playlistId = playlists.create(PLAYLIST_NAME)
            playlists.addSong(playlistId = playlistId, song = song(id = 401, title = "Veridis Quo"))
        }
    }

    @AfterEach
    fun tearDown() {
        runBlocking { playlists.delete(playlistId) }
        unloadKoinModules(fakeRemoteModule)
    }

    /**
     * A swipe no longer takes a song out of a playlist, so the options sheet opened from the playlist
     * does, at once. The sheet draws its options disabled until the song loads, so the test waits for
     * the option to be enabled before tapping it.
     */
    @Test
    fun aSongIsTakenOutOfAPlaylistFromItsOptionsSheet() = compose.use {
        waitUntilAtLeastOneExists(hasSetTextAction(), SCREEN_TIMEOUT_MILLIS)
        onNodeWithText("Library").performClick()
        waitUntilAtLeastOneExists(hasText(PLAYLIST_NAME), SCREEN_TIMEOUT_MILLIS)
        onNodeWithText(PLAYLIST_NAME).performClick()
        waitUntilAtLeastOneExists(hasText("Veridis Quo"), SCREEN_TIMEOUT_MILLIS)

        onAllNodesWithContentDescription("More options")[0].performClick()
        waitUntilAtLeastOneExists(hasText("Remove from this playlist") and isEnabled(), SCREEN_TIMEOUT_MILLIS)
        onNodeWithText("Remove from this playlist").performClick()

        waitUntilDoesNotExist(hasText("Veridis Quo"), SCREEN_TIMEOUT_MILLIS)
        assertThat(storedSongIds()).isEmpty()
    }

    private fun storedSongIds(): List<Long> = runBlocking {
        playlists.observeSongs(playlistId).first().map { song -> song.id }
    }

    private companion object {
        const val SCREEN_TIMEOUT_MILLIS = 10_000L
        const val PLAYLIST_NAME = "Road trip"
    }
}

private class FakeEmptyRemoteDataSource : SongSearchRemoteDataSource {
    override suspend fun searchSongs(
        term: String,
        limit: Int,
        forceRefresh: Boolean,
    ): List<Song> = emptyList()
}
