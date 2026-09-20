package com.pierre.tunescout

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.hasAnyDescendant
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performImeAction
import androidx.compose.ui.test.performTextInput
import com.pierre.tunescout.core.model.Album
import com.pierre.tunescout.core.model.Song
import com.pierre.tunescout.core.network.ITunesRemoteDataSource
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

@OptIn(ExperimentalTestApi::class)
class SearchToAlbumFlowTest {
    @JvmField
    @RegisterExtension
    val compose = createAndroidComposeExtension<MainActivity>()

    private val fakeRemoteModule: Module = module {
        single<ITunesRemoteDataSource> { FakeITunesRemoteDataSource() }
    }

    @BeforeEach
    fun setUp() {
        loadKoinModules(fakeRemoteModule)
    }

    @AfterEach
    fun tearDown() {
        unloadKoinModules(fakeRemoteModule)
    }

    @Test
    fun searchingASongPlaysItAndTheMiniPlayerLeadsToThePlayerAndItsAlbum() = compose.use {
        waitUntilAtLeastOneExists(hasSetTextAction(), SCREEN_TIMEOUT_MILLIS)

        searchFor("daft")
        playFromTheListRow("Get Lucky")

        openThePlayerFromTheMiniPlayer("Get Lucky")

        waitUntilAtLeastOneExists(hasText("Now playing"), SCREEN_TIMEOUT_MILLIS)
        onNodeWithContentDescription("More options").performClick()
        waitUntilAtLeastOneExists(hasText("View album"), SCREEN_TIMEOUT_MILLIS)
        onNodeWithText("View album").performClick()

        waitUntilAtLeastOneExists(hasText("Give Life Back to Music"), SCREEN_TIMEOUT_MILLIS)
    }

    /**
     * A landscape window leaves no room for results under the keyboard, so once [term] is typed this
     * closes it the way the search key does.
     */
    private fun ComposeContext.searchFor(term: String) {
        onNode(hasSetTextAction()).performTextInput(term)
        onNode(hasSetTextAction()).performImeAction()
    }

    /**
     * The same song can be drawn in the list and in the mini player — including one restored from
     * a previous session — so the row is the one that carries the options action, never an index.
     */
    private fun ComposeContext.playFromTheListRow(title: String) {
        val listRow = hasText(title).and(hasAnyDescendant(hasContentDescription("More options")))
        waitUntilAtLeastOneExists(listRow, SCREEN_TIMEOUT_MILLIS)
        onAllNodes(listRow)[0].performClick()
    }

    /**
     * The row only starts the song: the bar that rises under the results is what opens the player.
     * It is the one place the song is drawn beside the queue action, which is what tells it apart
     * from the row it came from.
     */
    private fun ComposeContext.openThePlayerFromTheMiniPlayer(title: String) {
        val miniPlayer = hasText(title).and(hasAnyDescendant(hasContentDescription("Open the queue")))
        waitUntilAtLeastOneExists(miniPlayer, SCREEN_TIMEOUT_MILLIS)
        onAllNodes(miniPlayer)[0].performClick()
    }

    private companion object {
        const val SCREEN_TIMEOUT_MILLIS = 10_000L
    }
}

private class FakeITunesRemoteDataSource : ITunesRemoteDataSource {
    private val catalog = listOf(
        song(id = 1, title = "Get Lucky"),
        song(id = 2, title = "Instant Crush"),
    )

    override suspend fun searchSongs(
        term: String,
        limit: Int,
        forceRefresh: Boolean,
    ): List<Song> = catalog.take(limit)

    override suspend fun fetchAlbum(albumId: Long): Album? = album(
        id = albumId,
        songs = listOf(
            song(id = 10, albumId = albumId, title = "Give Life Back to Music", trackNumber = 1),
            song(id = 1, albumId = albumId, title = "Get Lucky", trackNumber = 8),
        ),
    )
}
