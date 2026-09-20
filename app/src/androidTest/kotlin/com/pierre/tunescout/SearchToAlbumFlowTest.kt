package com.pierre.tunescout

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onAllNodesWithText
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
        waitUntilAtLeastOneExists(hasText("Get Lucky"), SCREEN_TIMEOUT_MILLIS)
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

    /** Anything already loaded is also in the mini player, so this takes the list row. */
    private fun ComposeContext.playFromTheListRow(title: String) {
        onAllNodesWithText(title)[0].performClick()
    }

    /**
     * The row only starts the song: the bar that rises under the results is what opens the player,
     * and it is the second of the two places the song is now drawn.
     */
    private fun ComposeContext.openThePlayerFromTheMiniPlayer(title: String) {
        waitUntilAtLeastOneExists(hasContentDescription("Open the queue"), SCREEN_TIMEOUT_MILLIS)
        onAllNodesWithText(title)[1].performClick()
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
