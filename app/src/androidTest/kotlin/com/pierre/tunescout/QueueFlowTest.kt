package com.pierre.tunescout

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isEnabled
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performImeAction
import androidx.compose.ui.test.performTextInput
import com.pierre.tunescout.core.model.Song
import com.pierre.tunescout.core.network.SongSearchRemoteDataSource
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
class QueueFlowTest {
    @JvmField
    @RegisterExtension
    val compose = createAndroidComposeExtension<MainActivity>()

    private val fakeRemoteModule: Module = module {
        single<SongSearchRemoteDataSource> { FakeCatalogRemoteDataSource() }
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
     * Starting the first song again keeps what was queued by hand, which is why the first result is
     * played a second time before the queue is opened.
     */
    @Test
    fun playingASongThenQueueingAnotherShowsBothInTheQueue() = compose.use {
        waitUntilAtLeastOneExists(hasSetTextAction(), SCREEN_TIMEOUT_MILLIS)
        searchFor("daft")
        waitUntilAtLeastOneExists(hasText("Get Lucky"), SCREEN_TIMEOUT_MILLIS)

        playSearchResult("Get Lucky")
        queueSecondResultFromItsOptionsSheet()

        onAllNodesWithText("Get Lucky")[0].performClick()
        onNodeWithContentDescription("Open the queue").performClick()

        assertBothQueueTiersAreOnScreen()
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
     * Playing a search result raises the mini player and leaves the results on screen, so the same
     * song is now drawn twice: its row and the bar under the list.
     */
    private fun ComposeContext.playSearchResult(title: String) {
        onNodeWithText(title).performClick()
        waitUntilAtLeastOneExists(hasContentDescription("Open the queue"), SCREEN_TIMEOUT_MILLIS)
        onAllNodesWithText(title).assertCountEquals(2)
    }

    private fun ComposeContext.queueSecondResultFromItsOptionsSheet() {
        onAllNodesWithContentDescription("More options")[1].performClick()
        waitUntilAtLeastOneExists(hasText("Add to queue") and isEnabled(), SCREEN_TIMEOUT_MILLIS)
        onNodeWithText("Add to queue").performClick()
        waitUntilDoesNotExist(hasText("Add to queue"), SCREEN_TIMEOUT_MILLIS)
    }

    /**
     * Both tiers are on screen: what plays, and what was queued by hand. The results are still behind
     * the sheet, so the queued song is taken by its row's reorder handle rather than by a title the
     * list under it also carries.
     */
    private fun ComposeContext.assertBothQueueTiersAreOnScreen() {
        waitUntilAtLeastOneExists(hasText("Next in queue"), SCREEN_TIMEOUT_MILLIS)
        onNodeWithText("Queue").assertExists()
        onNodeWithContentDescription("Reorder Instant Crush").assertExists()
    }

    private companion object {
        const val SCREEN_TIMEOUT_MILLIS = 10_000L
    }
}

private class FakeCatalogRemoteDataSource : SongSearchRemoteDataSource {
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
