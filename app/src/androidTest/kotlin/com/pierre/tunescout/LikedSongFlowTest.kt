package com.pierre.tunescout

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isEnabled
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.google.common.truth.Truth.assertThat
import com.pierre.tunescout.core.database.FavoriteSongLocalDataSource
import com.pierre.tunescout.core.database.RecentlyPlayedLocalDataSource
import com.pierre.tunescout.core.model.Song
import com.pierre.tunescout.core.network.SongSearchRemoteDataSource
import com.pierre.tunescout.core.testing.fixture.song
import de.mannodermaus.junit5.compose.createAndroidComposeExtension
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
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
class LikedSongFlowTest {
    @JvmField
    @RegisterExtension
    val compose = createAndroidComposeExtension<MainActivity>()

    private val recentlyPlayed: RecentlyPlayedLocalDataSource
        get() = GlobalContext.get().get()

    private val favorites: FavoriteSongLocalDataSource
        get() = GlobalContext.get().get()

    private val liked = song(id = 201, title = "Digital Love")

    private val fakeRemoteModule: Module = module {
        single<SongSearchRemoteDataSource> { FakeLibraryRemoteDataSource() }
    }

    @BeforeEach
    fun setUp() = runTest {
        loadKoinModules(fakeRemoteModule)
        storedFavorites().forEach { song -> favorites.remove(song.id) }
        storedHistory().forEach { song -> recentlyPlayed.remove(song.id) }
        recentlyPlayed.record(liked)
    }

    @AfterEach
    fun tearDown() = runTest {
        storedFavorites().forEach { song -> favorites.remove(song.id) }
        unloadKoinModules(fakeRemoteModule)
    }

    /**
     * The sheet draws its options disabled until the song loads from the database, and a tap on a
     * disabled option is dropped, so the test waits for "Like" to be enabled before tapping it.
     */
    @Test
    fun aSongLikedFromItsOptionsSheetShowsUpUnderLikedSongsInTheLibraryTab() = compose.use {
        waitUntilAtLeastOneExists(hasSetTextAction(), SCREEN_TIMEOUT_MILLIS)
        waitUntilAtLeastOneExists(hasText("Digital Love"), SCREEN_TIMEOUT_MILLIS)

        onAllNodesWithContentDescription("More options")[0].performClick()
        waitUntilAtLeastOneExists(hasText("Like") and isEnabled(), SCREEN_TIMEOUT_MILLIS)
        onNodeWithText("Like").performClick()
        waitUntilDoesNotExist(hasText("Like"), SCREEN_TIMEOUT_MILLIS)
        assertThat(storedFavoriteIds()).containsExactly(201L)

        onNodeWithText("Library").performClick()
        waitUntilAtLeastOneExists(hasText("Liked songs"), SCREEN_TIMEOUT_MILLIS)
        onNodeWithText("Liked songs").performClick()
        waitUntilAtLeastOneExists(hasText("Digital Love"), SCREEN_TIMEOUT_MILLIS)
    }

    private suspend fun storedFavorites(): List<Song> = favorites.observeAll().first()

    private suspend fun storedHistory(): List<Song> = recentlyPlayed.observe(limit = HISTORY_LIMIT).first()

    private fun storedFavoriteIds(): List<Long> = runBlocking { storedFavorites().map { song -> song.id } }

    private companion object {
        const val SCREEN_TIMEOUT_MILLIS = 10_000L
        const val HISTORY_LIMIT = 100
    }
}

private class FakeLibraryRemoteDataSource : SongSearchRemoteDataSource {
    override suspend fun searchSongs(
        term: String,
        limit: Int,
        forceRefresh: Boolean,
    ): List<Song> = emptyList()
}
