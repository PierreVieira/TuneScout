package com.pierre.tunescout

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isEnabled
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performCustomAccessibilityActionWithLabel
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTouchInput
import com.google.common.truth.Truth.assertThat
import com.pierre.tunescout.core.database.PlaylistLocalDataSource
import com.pierre.tunescout.core.model.Song
import com.pierre.tunescout.core.network.SongSearchRemoteDataSource
import com.pierre.tunescout.core.testing.fixture.song
import de.mannodermaus.junit5.compose.ComposeContext
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
class PlaylistReorderFlowTest {
    @JvmField
    @RegisterExtension
    val compose = createAndroidComposeExtension<MainActivity>()

    private val playlists: PlaylistLocalDataSource
        get() = GlobalContext.get().get()

    private val fakeRemoteModule: Module = module {
        single<SongSearchRemoteDataSource> { FakeReorderRemoteDataSource() }
    }

    private var playlistId: Long = 0
    private val veridisQuo = song(id = 701, title = "Veridis Quo")
    private val digitalLove = song(id = 702, title = "Digital Love")
    private val harderBetter = song(id = 703, title = "Harder, Better, Faster, Stronger")

    @BeforeEach
    fun setUp() {
        loadKoinModules(fakeRemoteModule)
        runBlocking {
            playlistId = playlists.create(PLAYLIST_NAME)
            listOf(veridisQuo, digitalLove, harderBetter).forEach { song ->
                playlists.addSong(playlistId = playlistId, song = song)
            }
        }
    }

    @AfterEach
    fun tearDown() {
        runBlocking { playlists.delete(playlistId) }
        unloadKoinModules(fakeRemoteModule)
    }

    /**
     * The song's own options sheet starts the reordering on the playlist under it; the moves a
     * screen reader offers are what drags the row here, since they land on the same event.
     */
    @Test
    fun aPlaylistIsReorderedFromASongsOptionsSheet() = compose.use {
        openThePlaylist()

        onAllNodesWithContentDescription("More options")[0].performClick()
        waitUntilAtLeastOneExists(hasText("Reorder songs") and isEnabled(), SCREEN_TIMEOUT_MILLIS)
        onNodeWithText("Reorder songs").performScrollTo().performClick()
        waitUntilAtLeastOneExists(hasContentDescription("Done reordering"), SCREEN_TIMEOUT_MILLIS)

        onNodeWithText(veridisQuo.title).performCustomAccessibilityActionWithLabel("Move down")
        waitUntil(SCREEN_TIMEOUT_MILLIS) { storedSongIds() == listOf(702L, 701L, 703L) }

        onNodeWithContentDescription("Done reordering").performClick()
        waitUntilDoesNotExist(hasContentDescription("Done reordering"), SCREEN_TIMEOUT_MILLIS)
        assertThat(onAllNodesWithContentDescription("More options").fetchSemanticsNodes()).hasSize(3)
    }

    @Test
    fun aPlaylistStartsReorderingFromItsOwnOptionsSheet() = compose.use {
        openThePlaylist()

        onNodeWithContentDescription("More options for this collection").performClick()
        waitUntilAtLeastOneExists(hasText("Reorder songs") and isEnabled(), SCREEN_TIMEOUT_MILLIS)
        onNodeWithText("Reorder songs").performScrollTo().performClick()

        waitUntilAtLeastOneExists(hasContentDescription("Done reordering"), SCREEN_TIMEOUT_MILLIS)
        assertThat(onAllNodesWithContentDescription("More options").fetchSemanticsNodes()).isEmpty()
    }

    /**
     * A long press anywhere on the row picks it up, and dragging it past the next row puts it there.
     * The finger moves in small steps, the way a real one does, so the list sees every row it passes.
     */
    @Test
    fun aSongIsDraggedIntoANewPlaceAfterALongPress() = compose.use {
        openThePlaylist()

        onNodeWithText(veridisQuo.title).performTouchInput {
            down(center)
            advanceEventTime(viewConfiguration.longPressTimeoutMillis * 2)
            repeat(DRAG_STEPS) {
                moveBy(Offset(x = 0f, y = height * ROWS_TO_DRAG / DRAG_STEPS))
                advanceEventTime(DRAG_STEP_MILLIS)
            }
            up()
        }

        waitUntil(SCREEN_TIMEOUT_MILLIS) { storedSongIds().first() != veridisQuo.id }
        waitUntilAtLeastOneExists(hasContentDescription("Done reordering"), SCREEN_TIMEOUT_MILLIS)
        assertThat(storedSongIds()).containsExactly(701L, 702L, 703L)
    }

    private fun ComposeContext.openThePlaylist() {
        waitUntilAtLeastOneExists(hasSetTextAction(), SCREEN_TIMEOUT_MILLIS)
        onNodeWithText("Library").performClick()
        waitUntilAtLeastOneExists(hasText(PLAYLIST_NAME), SCREEN_TIMEOUT_MILLIS)
        onNodeWithText(PLAYLIST_NAME).performClick()
        waitUntilAtLeastOneExists(hasText(veridisQuo.title), SCREEN_TIMEOUT_MILLIS)
    }

    private fun storedSongIds(): List<Long> = runBlocking {
        playlists.observeSongs(playlistId).first().map { song -> song.id }
    }

    private companion object {
        const val SCREEN_TIMEOUT_MILLIS = 10_000L
        const val PLAYLIST_NAME = "Reorder trip"
        const val DRAG_STEPS = 20
        const val DRAG_STEP_MILLIS = 16L
        const val ROWS_TO_DRAG = 1.6f
    }
}

private class FakeReorderRemoteDataSource : SongSearchRemoteDataSource {
    override suspend fun searchSongs(
        term: String,
        limit: Int,
        forceRefresh: Boolean,
    ): List<Song> = emptyList()
}
