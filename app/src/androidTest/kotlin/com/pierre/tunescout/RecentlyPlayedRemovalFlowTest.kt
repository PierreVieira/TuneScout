package com.pierre.tunescout

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.google.common.truth.Truth.assertThat
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
        single<SongSearchRemoteDataSource> { FakeHistoryRemoteDataSource() }
    }

    @BeforeEach
    fun setUp() = runTest {
        loadKoinModules(fakeRemoteModule)
        clearHistory()
        history.forEach { song -> recentlyPlayed.record(song) }
    }

    @AfterEach
    fun tearDown() {
        unloadKoinModules(fakeRemoteModule)
    }

    /**
     * The list is newest first, so the first X belongs to the song recorded last. It only asks; confirming drops it, and the database keeps it dropped. Doing it
     * again for the last one leaves the empty state behind. A swipe no longer removes a row: it
     * queues or likes the song instead.
     */
    @Test
    fun aRecentlyPlayedSongIsDroppedByItsRemoveIcon() = compose.use {
        waitUntilAtLeastOneExists(hasSetTextAction(), SCREEN_TIMEOUT_MILLIS)
        waitUntilAtLeastOneExists(hasText("Recently played"), SCREEN_TIMEOUT_MILLIS)
        onNodeWithText("Around the World").assertExists()
        onNodeWithText("Digital Love").assertExists()

        onAllNodesWithContentDescription("Remove from recently played")[0].performClick()
        waitUntilAtLeastOneExists(hasText("Remove from recently played?"), SCREEN_TIMEOUT_MILLIS)
        onNodeWithText("Remove").performClick()
        waitUntilDoesNotExist(hasText("Digital Love"), SCREEN_TIMEOUT_MILLIS)
        assertThat(storedIds()).containsExactly(101L)

        onAllNodesWithContentDescription("Remove from recently played")[0].performClick()
        waitUntilAtLeastOneExists(hasText("Remove from recently played?"), SCREEN_TIMEOUT_MILLIS)
        onNodeWithText("Remove").performClick()

        waitUntilAtLeastOneExists(hasText("Nothing played yet"), SCREEN_TIMEOUT_MILLIS)
        onAllNodesWithText("Around the World").assertCountEquals(0)
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

private class FakeHistoryRemoteDataSource : SongSearchRemoteDataSource {
    override suspend fun searchSongs(
        term: String,
        limit: Int,
        forceRefresh: Boolean,
    ): List<Song> = emptyList()
}
