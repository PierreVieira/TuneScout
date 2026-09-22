package com.pierre.tunescout

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.hasAnyDescendant
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasScrollToNodeAction
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import com.google.common.truth.Truth.assertThat
import com.pierre.tunescout.core.database.FavoriteSongLocalDataSource
import com.pierre.tunescout.core.model.Song
import com.pierre.tunescout.core.network.SongSearchRemoteDataSource
import com.pierre.tunescout.core.testing.fixture.song
import de.mannodermaus.junit5.compose.ComposeContext
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
class LikedSongsQueueFlowTest {
    @JvmField
    @RegisterExtension
    val compose = createAndroidComposeExtension<MainActivity>()

    private val likedSongs = listOf(
        song(id = 301, title = "Digital Love"),
        song(id = 302, title = "Something About Us"),
        song(id = 303, title = "Voyager"),
    )

    private val favorites: FavoriteSongLocalDataSource
        get() = GlobalContext.get().get()

    private val fakeRemoteModule: Module = module {
        single<SongSearchRemoteDataSource> { EmptySearchRemoteDataSource() }
    }

    @BeforeEach
    fun setUp() = runTest {
        loadKoinModules(fakeRemoteModule)
        storedFavorites().forEach { song -> favorites.remove(song.id) }
        likedSongs.forEach { song -> favorites.add(song) }
    }

    @AfterEach
    fun tearDown() = runTest {
        storedFavorites().forEach { song -> favorites.remove(song.id) }
        unloadKoinModules(fakeRemoteModule)
    }

    /**
     * The liked songs are listed in the order the database gives them, so the song tapped is the
     * first of that order, and the rest of the liked songs is what the queue must hold after it.
     */
    @Test
    fun playingALikedSongQueuesTheRestOfTheLikedSongsBehindIt() = compose.use {
        val (first, rest) = storedFavorites().let { songs -> songs.first() to songs.drop(1) }
        waitUntilAtLeastOneExists(hasSetTextAction(), SCREEN_TIMEOUT_MILLIS)

        onNodeWithText("Library").performClick()
        waitUntilAtLeastOneExists(hasText("Liked songs"), SCREEN_TIMEOUT_MILLIS)
        onNodeWithText("Liked songs").performClick()
        waitUntilAtLeastOneExists(hasText(first.title), SCREEN_TIMEOUT_MILLIS)

        onNodeWithText(first.title).performClick()
        waitUntilAtLeastOneExists(hasContentDescription("Open the queue"), SCREEN_TIMEOUT_MILLIS)
        onNodeWithContentDescription("Open the queue").performClick()

        waitUntilAtLeastOneExists(hasText("Playing from Liked songs"), SCREEN_TIMEOUT_MILLIS)
        assertThat(findQueuedTitles(rest)).containsExactlyElementsIn(rest.map(Song::title))
    }

    /**
     * The queue is a lazy list, and a landscape window only lays out the rows it has room for, so
     * each song is scrolled to rather than looked for on screen. The liked songs under the sheet
     * scroll too, which is why the queue is the list that holds a queue entry.
     *
     * @return the titles of [songs] the queue holds.
     */
    private fun ComposeContext.findQueuedTitles(songs: List<Song>): List<String> {
        val queueList = onAllNodes(
            hasScrollToNodeAction() and hasAnyDescendant(hasTestTag(QUEUE_ENTRY_TAG)),
        )[0]
        return songs
            .filter { song ->
                runCatching {
                    queueList.performScrollToNode(hasTestTag(QUEUE_ENTRY_TAG) and hasText(song.title))
                }.isSuccess
            }.map(Song::title)
    }

    private fun storedFavorites(): List<Song> = runBlocking { favorites.observeAll().first() }

    private companion object {
        const val SCREEN_TIMEOUT_MILLIS = 10_000L
        const val QUEUE_ENTRY_TAG = "queue_entry"
    }
}

private class EmptySearchRemoteDataSource : SongSearchRemoteDataSource {
    override suspend fun searchSongs(
        term: String,
        limit: Int,
        forceRefresh: Boolean,
    ): List<Song> = emptyList()
}
