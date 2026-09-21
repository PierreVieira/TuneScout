package com.pierre.tunescout

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.hasAnyDescendant
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasScrollToNodeAction
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isEnabled
import androidx.compose.ui.test.isOn
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performImeAction
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextInput
import androidx.test.espresso.Espresso.pressBack
import com.google.common.truth.Truth.assertThat
import com.pierre.tunescout.core.model.Album
import com.pierre.tunescout.core.model.Song
import com.pierre.tunescout.core.network.AlbumRemoteDataSource
import com.pierre.tunescout.core.network.SongSearchRemoteDataSource
import com.pierre.tunescout.core.testing.fixture.album
import com.pierre.tunescout.core.testing.fixture.song
import de.mannodermaus.junit5.compose.ComposeContext
import de.mannodermaus.junit5.compose.createAndroidComposeExtension
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import org.koin.core.context.loadKoinModules
import org.koin.core.context.unloadKoinModules
import org.koin.core.module.Module
import org.koin.dsl.module

private val getLucky = song(id = 1, title = "Get Lucky", trackNumber = 8)
private val albumTracks = listOf(
    song(id = 10, title = "Give Life Back to Music", trackNumber = 1),
    song(id = 11, title = "The Game of Love", trackNumber = 2),
    song(id = 12, title = "Giorgio by Moroder", trackNumber = 3),
    song(id = 13, title = "Within", trackNumber = 4),
    song(id = 14, title = "Instant Crush", trackNumber = 5),
    getLucky,
)

@OptIn(ExperimentalTestApi::class)
class AlbumShuffleFlowTest {
    @JvmField
    @RegisterExtension
    val compose = createAndroidComposeExtension<MainActivity>()

    private val fakeRemoteModule: Module = module {
        single<SongSearchRemoteDataSource> { ShuffleCatalogRemoteDataSource() }
        single<AlbumRemoteDataSource> { ShuffleAlbumRemoteDataSource() }
    }

    @BeforeEach
    fun setUp() {
        loadKoinModules(fakeRemoteModule)
    }

    @AfterEach
    fun tearDown() {
        unloadKoinModules(fakeRemoteModule)
    }

    /**
     * Shuffle is turned on from the album before it plays, so the album starts on any of its tracks
     * and the rest follows in a random order: whichever that is, the queue holds every other track of
     * the album — which is what shows the real player took the reordered queue as it was.
     */
    @Test
    fun shufflingAnAlbumAndPlayingItQueuesEveryOtherTrack() = compose.use {
        waitUntilAtLeastOneExists(hasSetTextAction(), SCREEN_TIMEOUT_MILLIS)
        searchFor("daft")
        waitUntilAtLeastOneExists(hasText("Get Lucky"), SCREEN_TIMEOUT_MILLIS)

        openTheAlbumOfTheFirstResult()
        onNodeWithContentDescription("Shuffle").performClick()
        waitUntilAtLeastOneExists(hasContentDescription("Shuffle") and isOn(), SCREEN_TIMEOUT_MILLIS)
        onNodeWithContentDescription("Play the album").performClick()
        closeTheAlbumCoveringThePlayer()

        waitUntilAtLeastOneExists(hasContentDescription("Open the queue"), SCREEN_TIMEOUT_MILLIS)
        onNodeWithContentDescription("Open the queue").performClick()

        waitUntilAtLeastOneExists(hasTestTag(QUEUE_ENTRY_TAG), SCREEN_TIMEOUT_MILLIS)
        assertThat(countQueuedTracks()).isEqualTo(albumTracks.size - 1)
    }

    /**
     * The queue is reached from the player. A phone upright has the mini player under the album, but a
     * wide window has the player itself beside the tabs, and the album is open over it.
     */
    private fun closeTheAlbumCoveringThePlayer() {
        if (isTwoPaneWindow) pressBack()
    }

    /**
     * The queue is a lazy list, and a landscape window only lays out the rows it has room for, so
     * each track is scrolled to rather than counted on screen. The album list under the sheet
     * scrolls too, which is why the queue is the list that holds a reorder handle.
     *
     * @return how many of the album's tracks the queue holds.
     */
    private fun ComposeContext.countQueuedTracks(): Int {
        val queueList = onAllNodes(
            hasScrollToNodeAction() and hasAnyDescendant(hasTestTag(QUEUE_ENTRY_TAG)),
        )[0]
        return albumTracks.count { track ->
            runCatching {
                queueList.performScrollToNode(
                    hasTestTag(QUEUE_ENTRY_TAG) and hasText(track.title),
                )
            }.isSuccess
        }
    }

    /**
     * A landscape window leaves no room for results under the keyboard, so once [term] is typed this
     * closes it the way the search key does.
     */
    private fun ComposeContext.searchFor(term: String) {
        onNode(hasSetTextAction()).performTextInput(term)
        onNode(hasSetTextAction()).performImeAction()
    }

    /** A landscape window cuts the options sheet short of its last options, so it is scrolled first. */
    private fun ComposeContext.openTheAlbumOfTheFirstResult() {
        onAllNodesWithContentDescription("More options")[0].performClick()
        waitUntilAtLeastOneExists(hasText("View album") and isEnabled(), SCREEN_TIMEOUT_MILLIS)
        onNodeWithText("View album").performScrollTo().performClick()
        waitUntilAtLeastOneExists(hasContentDescription("Play the album"), SCREEN_TIMEOUT_MILLIS)
    }

    private companion object {
        const val SCREEN_TIMEOUT_MILLIS = 10_000L
        const val QUEUE_ENTRY_TAG = "queue_entry"
    }
}

private class ShuffleCatalogRemoteDataSource : SongSearchRemoteDataSource {
    override suspend fun searchSongs(
        term: String,
        limit: Int,
        forceRefresh: Boolean,
    ): List<Song> = listOf(getLucky)
}

private class ShuffleAlbumRemoteDataSource : AlbumRemoteDataSource {
    override suspend fun fetchAlbum(albumId: Long): Album? = album(
        id = albumId,
        songs = albumTracks.map { track -> track.copy(albumId = albumId) },
    )
}
