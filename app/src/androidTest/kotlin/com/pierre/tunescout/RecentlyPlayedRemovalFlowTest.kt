package com.pierre.tunescout

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeRight
import com.google.common.truth.Truth.assertThat
import com.pierre.tunescout.core.database.RecentlyPlayedLocalDataSource
import com.pierre.tunescout.core.model.Album
import com.pierre.tunescout.core.model.Song
import com.pierre.tunescout.core.network.ITunesRemoteDataSource
import com.pierre.tunescout.core.testing.fixture.album
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
class RecentlyPlayedRemovalFlowTest {
    @JvmField
    @RegisterExtension
    val compose = createAndroidComposeExtension<MainActivity>()

    private val recentlyPlayed: RecentlyPlayedLocalDataSource
        get() = GlobalContext.get().get()

    private val history = listOf(
        song(id = 101, title = "Around the World"),
        song(id = 102, title = "Digital Love"),
    )

    private val fakeRemoteModule: Module = module {
        single<ITunesRemoteDataSource> { FakeHistoryRemoteDataSource() }
    }

    @BeforeEach
    fun setUp() {
        loadKoinModules(fakeRemoteModule)
        runBlocking {
            clearHistory()
            history.forEach { song -> recentlyPlayed.record(song) }
        }
    }

    @AfterEach
    fun tearDown() {
        unloadKoinModules(fakeRemoteModule)
    }

    /**
     * Swiping a row only asks; confirming drops it, and the database keeps it dropped. The X icon
     * on the row then drops the last one, leaving the empty state behind.
     */
    @Test
    fun aRecentlyPlayedSongIsDroppedBySwipingItsRowAndByItsRemoveIcon() = compose.use {
        waitUntilAtLeastOneExists(hasSetTextAction(), SCREEN_TIMEOUT_MILLIS)
        waitUntilAtLeastOneExists(hasText("Recently played"), SCREEN_TIMEOUT_MILLIS)
        onNodeWithText("Around the World").assertExists()
        onNodeWithText("Digital Love").assertExists()

        onNodeWithText("Around the World").performTouchInput { swipeRight() }
        waitUntilAtLeastOneExists(hasText("Remove from recently played?"), SCREEN_TIMEOUT_MILLIS)
        onNodeWithText("Remove").performClick()
        waitUntilDoesNotExist(hasText("Around the World"), SCREEN_TIMEOUT_MILLIS)
        assertThat(storedIds()).containsExactly(102L)

        onAllNodesWithContentDescription("Remove from recently played")[0].performClick()
        waitUntilAtLeastOneExists(hasText("Remove from recently played?"), SCREEN_TIMEOUT_MILLIS)
        onNodeWithText("Remove").performClick()

        waitUntilAtLeastOneExists(hasText("Nothing played yet"), SCREEN_TIMEOUT_MILLIS)
        onAllNodesWithText("Digital Love").assertCountEquals(0)
        assertThat(storedIds()).isEmpty()
    }

    private suspend fun clearHistory() {
        storedHistory().forEach { song -> recentlyPlayed.remove(song.id) }
    }

    private suspend fun storedHistory(): List<Song> = recentlyPlayed.observe(limit = HISTORY_LIMIT).first()

    private fun storedIds(): List<Long> = runBlocking { storedHistory().map { song -> song.id } }

    private companion object {
        const val SCREEN_TIMEOUT_MILLIS = 10_000L
        const val HISTORY_LIMIT = 100
    }
}

private class FakeHistoryRemoteDataSource : ITunesRemoteDataSource {
    override suspend fun searchSongs(
        term: String,
        limit: Int,
    ): List<Song> = emptyList()

    override suspend fun fetchAlbum(albumId: Long): Album? = album(id = albumId)
}
