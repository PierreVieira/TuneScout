package com.pierre.tunescout

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performImeAction
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import androidx.compose.ui.test.swipeRight
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
class SongSwipeFlowTest {
    @JvmField
    @RegisterExtension
    val compose = createAndroidComposeExtension<MainActivity>()

    private val recentlyPlayed: RecentlyPlayedLocalDataSource
        get() = GlobalContext.get().get()

    private val favorites: FavoriteSongLocalDataSource
        get() = GlobalContext.get().get()

    private val fakeRemoteModule: Module = module {
        single<SongSearchRemoteDataSource> { FakeSwipeCatalogRemoteDataSource() }
    }

    @BeforeEach
    fun setUp() = runTest {
        loadKoinModules(fakeRemoteModule)
        storedFavorites().forEach { song -> favorites.remove(song.id) }
        storedHistory().forEach { song -> recentlyPlayed.remove(song.id) }
    }

    @AfterEach
    fun tearDown() = runTest {
        storedFavorites().forEach { song -> favorites.remove(song.id) }
        unloadKoinModules(fakeRemoteModule)
    }

    /**
     * Something has to be playing for a song queued by hand to have a place, so the first result is
     * played before the second one is swiped. The row springs back, so the result stays on screen.
     */
    @Test
    fun swipingASearchResultTowardTheEndQueuesIt() = compose.use {
        waitUntilAtLeastOneExists(hasSetTextAction(), SCREEN_TIMEOUT_MILLIS)
        onNode(hasSetTextAction()).performTextInput("daft")
        onNode(hasSetTextAction()).performImeAction()
        waitUntilAtLeastOneExists(hasText("Get Lucky"), SCREEN_TIMEOUT_MILLIS)

        onNodeWithText("Get Lucky").performClick()
        waitUntilAtLeastOneExists(hasContentDescription("Open the queue"), SCREEN_TIMEOUT_MILLIS)
        onNodeWithText("Instant Crush").performTouchInput { swipeRight() }
        waitUntilAtLeastOneExists(hasText("Added to queue"), SCREEN_TIMEOUT_MILLIS)
        onNodeWithText("Instant Crush").assertExists()

        onNodeWithContentDescription("Open the queue").performClick()
        waitUntilAtLeastOneExists(hasText("Next in queue"), SCREEN_TIMEOUT_MILLIS)
        onNode(hasTestTag("queue_entry") and hasText("Instant Crush")).assertExists()
    }

    /**
     * A swipe toward the start likes the song, which the liked songs then list; the same swipe there
     * takes the like back, and with it the song out of the list.
     */
    @Test
    fun swipingASongTowardTheStartLikesItAndSwipingItAgainFromTheLikedSongsTakesItBack() = compose.use {
        runTest { recentlyPlayed.record(liked) }
        waitUntilAtLeastOneExists(hasText("Digital Love"), SCREEN_TIMEOUT_MILLIS)

        onNodeWithText("Digital Love").performTouchInput { swipeLeft() }
        waitUntilAtLeastOneExists(hasText("Added to liked songs"), SCREEN_TIMEOUT_MILLIS)
        assertThat(storedFavoriteIds()).containsExactly(liked.id)

        onNodeWithText("Library").performClick()
        waitUntilAtLeastOneExists(hasText("Liked songs"), SCREEN_TIMEOUT_MILLIS)
        onNodeWithText("Liked songs").performClick()
        waitUntilAtLeastOneExists(hasText("Digital Love"), SCREEN_TIMEOUT_MILLIS)

        onNodeWithText("Digital Love").performTouchInput { swipeLeft() }
        waitUntilDoesNotExist(hasText("Digital Love"), SCREEN_TIMEOUT_MILLIS)
        onAllNodesWithText("Digital Love").assertCountEquals(0)
        assertThat(storedFavoriteIds()).isEmpty()
    }

    private val liked = song(id = 301, title = "Digital Love")

    private suspend fun storedFavorites(): List<Song> = favorites.observeAll().first()

    private suspend fun storedHistory(): List<Song> = recentlyPlayed.observe(limit = HISTORY_LIMIT).first()

    private fun storedFavoriteIds(): List<Long> = runBlocking { storedFavorites().map { song -> song.id } }

    private companion object {
        const val SCREEN_TIMEOUT_MILLIS = 10_000L
        const val HISTORY_LIMIT = 100
    }
}

private class FakeSwipeCatalogRemoteDataSource : SongSearchRemoteDataSource {
    private val catalog = listOf(
        song(id = 1, title = "Get Lucky"),
        song(id = 2, title = "Instant Crush"),
    )

    override suspend fun searchSongs(
        term: String,
        limit: Int,
        forceRefresh: Boolean,
    ): List<Song> = catalog.take(limit)
}
