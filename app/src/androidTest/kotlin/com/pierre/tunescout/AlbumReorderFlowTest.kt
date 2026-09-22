package com.pierre.tunescout

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isEnabled
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performCustomAccessibilityActionWithLabel
import androidx.compose.ui.test.performImeAction
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import com.google.common.truth.Truth.assertThat
import com.pierre.tunescout.core.database.AlbumLocalDataSource
import com.pierre.tunescout.core.model.Album
import com.pierre.tunescout.core.model.Song
import com.pierre.tunescout.core.network.AlbumRemoteDataSource
import com.pierre.tunescout.core.network.SongSearchRemoteDataSource
import com.pierre.tunescout.core.testing.fixture.album
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

/** An album of its own, so a cached copy another flow left on the device is not what opens. */
private const val REORDER_ALBUM_ID = 88L
private val giveLifeBack =
    song(id = 801, title = "Give Life Back to Music", albumId = REORDER_ALBUM_ID, trackNumber = 1)
private val giorgio = song(id = 803, title = "Giorgio by Moroder", albumId = REORDER_ALBUM_ID, trackNumber = 3)

@OptIn(ExperimentalTestApi::class)
class AlbumReorderFlowTest {
    @JvmField
    @RegisterExtension
    val compose = createAndroidComposeExtension<MainActivity>()

    private val albums: AlbumLocalDataSource
        get() = GlobalContext.get().get()

    private val fakeRemoteModule: Module = module {
        single<SongSearchRemoteDataSource> { ReorderCatalogRemoteDataSource() }
        single<AlbumRemoteDataSource> { ReorderAlbumRemoteDataSource() }
    }

    @BeforeEach
    fun setUp() {
        loadKoinModules(fakeRemoteModule)
    }

    /** The order is kept apart from the album and outlives it, so it is cleared by hand. */
    @AfterEach
    fun tearDown() = runTest {
        albums.saveTrackOrder(albumId = REORDER_ALBUM_ID, songIds = emptyList())
        unloadKoinModules(fakeRemoteModule)
    }

    @Test
    fun anAlbumIsReorderedFromItsOwnOptionsSheetAndKeepsTheOrder() = compose.use {
        openTheAlbum()

        onNodeWithContentDescription("More options for this album").performClick()
        waitUntilAtLeastOneExists(hasText("Reorder songs") and isEnabled(), SCREEN_TIMEOUT_MILLIS)
        onNodeWithText("Reorder songs").performScrollTo().performClick()
        waitUntilAtLeastOneExists(hasContentDescription("Done reordering"), SCREEN_TIMEOUT_MILLIS)

        onNode(hasText(giveLifeBack.title) and inTheAlbum).performCustomAccessibilityActionWithLabel("Move down")
        waitUntil(SCREEN_TIMEOUT_MILLIS) { storedTrackIds() == listOf(802L, 801L, 803L) }
        onNodeWithContentDescription("Done reordering").performClick()

        waitUntilDoesNotExist(hasContentDescription("Done reordering"), SCREEN_TIMEOUT_MILLIS)
        assertThat(storedTrackIds()).containsExactly(802L, 801L, 803L).inOrder()
    }

    @Test
    fun anAlbumStartsReorderingFromATracksOptionsSheet() = compose.use {
        openTheAlbum()

        onAllNodes(hasContentDescription("More options") and inTheAlbum)[0].performClick()
        waitUntilAtLeastOneExists(hasText("Reorder songs") and isEnabled(), SCREEN_TIMEOUT_MILLIS)
        onNodeWithText("Reorder songs").performScrollTo().performClick()

        waitUntilAtLeastOneExists(hasContentDescription("Done reordering"), SCREEN_TIMEOUT_MILLIS)
        assertThat(onAllNodes(hasContentDescription("More options") and inTheAlbum).fetchSemanticsNodes()).isEmpty()
    }

    /** A landscape window leaves no room for results under the keyboard, so the search key closes it. */
    private fun ComposeContext.openTheAlbum() {
        waitUntilAtLeastOneExists(hasSetTextAction(), SCREEN_TIMEOUT_MILLIS)
        onNode(hasSetTextAction()).performTextInput("daft")
        onNode(hasSetTextAction()).performImeAction()
        waitUntilAtLeastOneExists(hasText(giveLifeBack.title), SCREEN_TIMEOUT_MILLIS)
        onAllNodesWithContentDescription("More options")[0].performClick()
        waitUntilAtLeastOneExists(hasText("View album") and isEnabled(), SCREEN_TIMEOUT_MILLIS)
        onNodeWithText("View album").performScrollTo().performClick()
        waitUntilAtLeastOneExists(hasContentDescription("Play the album"), SCREEN_TIMEOUT_MILLIS)
        waitUntilAtLeastOneExists(hasText(giorgio.title), SCREEN_TIMEOUT_MILLIS)
    }

    private fun storedTrackIds(): List<Long> = runBlocking {
        albums
            .observe(REORDER_ALBUM_ID)
            .first()
            ?.songs
            .orEmpty()
            .map { song -> song.id }
    }

    /**
     * On a window wide enough for two panes the search results stay beside the album, so a track's
     * title and its options button are drawn twice: in the list and in the album. Only the album's are
     * the ones this flow means.
     */
    private val inTheAlbum: SemanticsMatcher = hasAnyAncestor(hasTestTag(ALBUM_TRACKS_TAG))

    private companion object {
        const val SCREEN_TIMEOUT_MILLIS = 10_000L
        const val ALBUM_TRACKS_TAG = "album_tracks"
    }
}

private class ReorderCatalogRemoteDataSource : SongSearchRemoteDataSource {
    override suspend fun searchSongs(
        term: String,
        limit: Int,
        forceRefresh: Boolean,
    ): List<Song> = listOf(giveLifeBack)
}

private class ReorderAlbumRemoteDataSource : AlbumRemoteDataSource {
    private val gameOfLove = song(id = 802, title = "The Game of Love", albumId = REORDER_ALBUM_ID, trackNumber = 2)

    override suspend fun fetchAlbum(albumId: Long): Album? = album(
        id = albumId,
        songs = listOf(giveLifeBack, gameOfLove, giorgio),
    )
}
