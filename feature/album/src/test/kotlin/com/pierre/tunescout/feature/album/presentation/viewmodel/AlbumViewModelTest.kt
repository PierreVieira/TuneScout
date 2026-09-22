package com.pierre.tunescout.feature.album.presentation.viewmodel

import com.google.common.truth.Truth.assertThat
import com.pierre.tunescout.core.model.Album
import com.pierre.tunescout.core.model.CollectionDownloadState
import com.pierre.tunescout.core.model.LibraryItemKey
import com.pierre.tunescout.core.model.NowPlaying
import com.pierre.tunescout.core.model.PlaybackContext
import com.pierre.tunescout.core.model.PlaybackState
import com.pierre.tunescout.core.model.PlaybackStatus
import com.pierre.tunescout.core.model.QueueSource
import com.pierre.tunescout.core.model.Song
import com.pierre.tunescout.core.model.SongDownloadStatus
import com.pierre.tunescout.core.navigation.Navigator
import com.pierre.tunescout.core.navigation.reorder.ReorderTarget
import com.pierre.tunescout.core.navigation.reorder.SharedFlowReorderRequests
import com.pierre.tunescout.core.navigation.route.AlbumOptionsRoute
import com.pierre.tunescout.core.navigation.route.AlbumRoute
import com.pierre.tunescout.core.navigation.route.PlayerRoute
import com.pierre.tunescout.core.navigation.route.SongOptionsRoute
import com.pierre.tunescout.core.playback.Enqueuer
import com.pierre.tunescout.core.playback.PlayableSongs
import com.pierre.tunescout.core.playback.SongPlayOutcome
import com.pierre.tunescout.core.playback.TransportControls
import com.pierre.tunescout.core.testing.extension.MainDispatcherExtension
import com.pierre.tunescout.core.testing.fake.FakeSongPlayback
import com.pierre.tunescout.core.testing.fake.SongPlayRequest
import com.pierre.tunescout.core.testing.fixture.album
import com.pierre.tunescout.core.testing.fixture.playbackState
import com.pierre.tunescout.core.testing.fixture.queueEntry
import com.pierre.tunescout.core.testing.fixture.song
import com.pierre.tunescout.feature.album.domain.usecase.AlbumUseCases
import com.pierre.tunescout.feature.album.presentation.model.AlbumUiAction
import com.pierre.tunescout.feature.album.presentation.model.AlbumUiEvent
import com.pierre.tunescout.feature.album.presentation.model.AlbumUiState
import com.pierre.tunescout.ui.component.R
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

class AlbumViewModelTest {
    private lateinit var viewModel: AlbumViewModel
    private lateinit var localAlbum: MutableStateFlow<Album?>
    private lateinit var songPlayback: FakeSongPlayback
    private lateinit var contextStarts: MutableList<Pair<List<Song>, PlaybackContext>>
    private lateinit var transportControls: TransportControls
    private lateinit var navigator: Navigator
    private lateinit var refreshCalls: MutableList<Long>

    /** What the next refresh returns, or null for one that never finishes. */
    private var refreshResults: Result<Unit>? = Result.success(Unit)
    private lateinit var favoriteToggles: MutableList<Pair<Long, Boolean>>
    private lateinit var isOnline: MutableStateFlow<Boolean>
    private lateinit var actions: MutableList<AlbumUiAction>
    private lateinit var enqueuer: Enqueuer
    private lateinit var favoriteSongIds: MutableStateFlow<Set<Long>>
    private lateinit var reorderRequests: SharedFlowReorderRequests
    private lateinit var savedTrackOrders: MutableList<Pair<Long, List<Long>>>
    private lateinit var downloadToggles: MutableList<DownloadToggle>

    @Test
    fun `GIVEN an album asked for with one track on the device WHEN observing THEN it is downloading, one of two`() =
        runTest(mainDispatcher.dispatcher) {
            // Given
            val statuses = mapOf(1L to SongDownloadStatus.Downloaded, 2L to SongDownloadStatus.Downloading)
            prepareScenario(
                cached = album(id = 10),
                downloadStatuses = statuses,
                downloadedCollections = setOf(LibraryItemKey.Album(albumId = 10)),
            )

            // When
            val state = viewModel.uiState.value as AlbumUiState.Loaded

            // Then
            assertThat(
                state.download,
            ).isEqualTo(CollectionDownloadState.Downloading(downloadedCount = 1, totalCount = 2))
            assertThat(state.downloadStatuses).isEqualTo(statuses)
        }

    @Test
    fun `GIVEN every track downloaded on its own WHEN observing THEN the album itself is not downloaded`() =
        runTest(mainDispatcher.dispatcher) {
            // Given
            prepareScenario(
                cached = album(id = 10),
                downloadStatuses = mapOf(1L to SongDownloadStatus.Downloaded, 2L to SongDownloadStatus.Downloaded),
                downloadedCollections = setOf(LibraryItemKey.Album(albumId = 99)),
            )

            // When
            val state = viewModel.uiState.value as AlbumUiState.Loaded

            // Then
            assertThat(state.download).isEqualTo(CollectionDownloadState.NotDownloaded)
        }

    @Test
    fun `GIVEN an album not downloaded WHEN tapping download THEN it is asked for, with whether it is liked`() =
        runTest(mainDispatcher.dispatcher) {
            // Given
            prepareScenario(cached = album(id = 10), isFavorite = true)

            // When
            viewModel.onEvent(AlbumUiEvent.OnDownloadClicked)
            runCurrent()

            // Then
            assertThat(
                downloadToggles,
            ).containsExactly(DownloadToggle(albumId = 10, isDownloaded = false, isFavorite = true))
        }

    @Test
    fun `GIVEN an album still downloading WHEN tapping download THEN the request is taken back`() =
        runTest(mainDispatcher.dispatcher) {
            // Given
            prepareScenario(
                cached = album(id = 10),
                downloadedCollections = setOf(LibraryItemKey.Album(albumId = 10)),
            )

            // When
            viewModel.onEvent(AlbumUiEvent.OnDownloadClicked)
            runCurrent()

            // Then
            assertThat(
                downloadToggles,
            ).containsExactly(DownloadToggle(albumId = 10, isDownloaded = true, isFavorite = false))
        }

    @Test
    fun `GIVEN no cached album WHEN starting THEN refreshes it and shows loading meanwhile`() =
        runTest(mainDispatcher.dispatcher) {
            // Given
            prepareScenario(cached = null, refreshResult = Result.success(Unit))

            // When
            val state = viewModel.uiState.value

            // Then
            assertThat(state).isEqualTo(AlbumUiState.Loading)
            assertThat(refreshCalls).containsExactly(10L)
        }

    @Test
    fun `GIVEN a cached album WHEN observing THEN shows it with the playback highlight`() =
        runTest(mainDispatcher.dispatcher) {
            // Given
            prepareScenario(
                cached = album(id = 10),
                playback = playbackState(songs = listOf(song(id = 2))),
            )

            // When
            val state = viewModel.uiState.value as AlbumUiState.Loaded

            // Then
            assertThat(state.album.id).isEqualTo(10L)
            assertThat(state.nowPlaying).isEqualTo(NowPlaying(songId = 2L, isPlaying = true))
        }

    @Test
    fun `GIVEN no cache and a failed refresh WHEN observing THEN shows the error`() =
        runTest(mainDispatcher.dispatcher) {
            // Given
            prepareScenario(cached = null, refreshResult = Result.failure(IllegalStateException("offline")))

            // When
            val state = viewModel.uiState.value

            // Then
            assertThat(state).isEqualTo(AlbumUiState.Error)
        }

    @Test
    fun `GIVEN a cached album and a failed refresh WHEN observing THEN keeps the cache and marks it stale`() =
        runTest(mainDispatcher.dispatcher) {
            // Given
            prepareScenario(cached = album(id = 10), refreshResult = Result.failure(IllegalStateException("offline")))

            // When
            val state = viewModel.uiState.value

            // Then
            assertThat(state).isInstanceOf(AlbumUiState.Loaded::class.java)
            assertThat((state as AlbumUiState.Loaded).isStale).isTrue()
        }

    @Test
    fun `GIVEN a cached album and a successful refresh WHEN observing THEN does not mark it stale`() =
        runTest(mainDispatcher.dispatcher) {
            // Given
            prepareScenario(cached = album(id = 10))

            // When
            val state = viewModel.uiState.value as AlbumUiState.Loaded

            // Then
            assertThat(state.isStale).isFalse()
        }

    @Test
    fun `GIVEN a stale album WHEN retrying successfully THEN stops marking it stale`() =
        runTest(mainDispatcher.dispatcher) {
            // Given
            prepareScenario(cached = album(id = 10), refreshResult = Result.failure(IllegalStateException("offline")))
            refreshResults = Result.success(Unit)

            // When
            viewModel.onEvent(AlbumUiEvent.OnRetryClicked)

            // Then
            assertThat((viewModel.uiState.value as AlbumUiState.Loaded).isStale).isFalse()
        }

    @Test
    fun `GIVEN only the saved tracks of the album WHEN the refresh is still running THEN shows loading`() =
        runTest(mainDispatcher.dispatcher) {
            // Given
            prepareScenario(cached = album(id = 10, isComplete = false), refreshResult = null)

            // When
            val state = viewModel.uiState.value

            // Then
            assertThat(state).isEqualTo(AlbumUiState.Loading)
        }

    @Test
    fun `GIVEN only the saved tracks of the album and a failed refresh WHEN observing THEN shows them`() =
        runTest(mainDispatcher.dispatcher) {
            // Given
            val partial = album(id = 10, songs = listOf(song(id = 1)), isComplete = false)
            prepareScenario(cached = partial, refreshResult = Result.failure(IllegalStateException("offline")))

            // When
            val state = viewModel.uiState.value as AlbumUiState.Loaded

            // Then
            assertThat(state.album).isEqualTo(partial)
        }

    @Test
    fun `GIVEN a partial album WHEN the connection comes back THEN refreshes it and keeps the tracks meanwhile`() =
        runTest(mainDispatcher.dispatcher) {
            // Given
            prepareScenario(
                cached = album(id = 10, isComplete = false),
                refreshResult = Result.failure(IllegalStateException("offline")),
                isOnlineAtStart = false,
            )
            refreshResults = null

            // When
            isOnline.value = true
            runCurrent()

            // Then
            assertThat(refreshCalls).containsExactly(10L, 10L)
            assertThat(viewModel.uiState.value).isInstanceOf(AlbumUiState.Loaded::class.java)
        }

    @Test
    fun `GIVEN a partial album WHEN the whole album arrives after reconnecting THEN shows the complete one`() =
        runTest(mainDispatcher.dispatcher) {
            // Given
            prepareScenario(
                cached = album(id = 10, isComplete = false),
                refreshResult = Result.failure(IllegalStateException("offline")),
                isOnlineAtStart = false,
            )
            refreshResults = Result.success(Unit)

            // When
            isOnline.value = true
            runCurrent()
            localAlbum.value = album(id = 10)

            // Then
            val state = viewModel.uiState.value as AlbumUiState.Loaded
            assertThat(state.album.isComplete).isTrue()
            assertThat(state.isStale).isFalse()
        }

    @Test
    fun `GIVEN a successful refresh WHEN the connection comes back THEN does not ask again`() =
        runTest(mainDispatcher.dispatcher) {
            // Given
            prepareScenario(cached = album(id = 10), isOnlineAtStart = false)

            // When
            isOnline.value = true
            runCurrent()

            // Then
            assertThat(refreshCalls).containsExactly(10L)
        }

    @Test
    fun `GIVEN a failed refresh WHEN the monitor reports the state the screen opened on THEN does not ask again`() =
        runTest(mainDispatcher.dispatcher) {
            // Given
            prepareScenario(cached = null, refreshResult = Result.failure(IllegalStateException("offline")))

            // When
            val state = viewModel.uiState.value

            // Then
            assertThat(state).isEqualTo(AlbumUiState.Error)
            assertThat(refreshCalls).containsExactly(10L)
        }

    @Test
    fun `GIVEN a partial album WHEN clicking the heart THEN does not store it`() = runTest(mainDispatcher.dispatcher) {
        // Given
        prepareScenario(
            cached = album(id = 10, isComplete = false),
            refreshResult = Result.failure(IllegalStateException("offline")),
        )

        // When
        viewModel.onEvent(AlbumUiEvent.OnFavoriteClicked)
        runCurrent()

        // Then
        assertThat(favoriteToggles).isEmpty()
    }

    @Test
    fun `GIVEN an error WHEN retrying THEN refreshes again`() = runTest(mainDispatcher.dispatcher) {
        // Given
        prepareScenario(cached = null, refreshResult = Result.failure(IllegalStateException("offline")))

        // When
        viewModel.onEvent(AlbumUiEvent.OnRetryClicked)

        // Then
        assertThat(refreshCalls).containsExactly(10L, 10L)
    }

    @Test
    fun `GIVEN a loaded album WHEN clicking a track THEN asks for it with the album behind it`() =
        runTest(mainDispatcher.dispatcher) {
            // Given
            val album = album(id = 10)
            prepareScenario(cached = album, playback = playbackState(songs = listOf(album.songs[0])))

            // When
            viewModel.onEvent(AlbumUiEvent.OnSongClicked(song = album.songs[1]))

            // Then
            assertThat(songPlayback.requests).containsExactly(
                SongPlayRequest(
                    song = album.songs[1],
                    nowPlaying = NowPlaying(songId = album.songs[0].id, isPlaying = true),
                    queue = album.songs,
                    context = PlaybackContext.Album(id = album.id, title = album.title),
                ),
            )
            verify(exactly = 0) { navigator.navigate(any()) }
        }

    @Test
    fun `GIVEN the track the player is already on WHEN clicking its row THEN opens the player`() =
        runTest(mainDispatcher.dispatcher) {
            // Given
            val album = album(id = 10)
            prepareScenario(cached = album, playback = playbackState(songs = listOf(album.songs[1])))
            songPlayback.outcome = SongPlayOutcome.AlreadyPlaying

            // When
            viewModel.onEvent(AlbumUiEvent.OnSongClicked(song = album.songs[1]))

            // Then
            verify { navigator.navigate(PlayerRoute(songId = album.songs[1].id)) }
            assertThat(actions).isEmpty()
        }

    @Test
    fun `GIVEN a track the player cannot reach WHEN clicking it THEN shows a message instead of playing`() =
        runTest(mainDispatcher.dispatcher) {
            // Given
            val album = album(id = 10)
            prepareScenario(cached = album)
            songPlayback.outcome = SongPlayOutcome.Unavailable

            // When
            viewModel.onEvent(AlbumUiEvent.OnSongClicked(song = album.songs[1]))
            runCurrent()

            // Then
            assertThat(actions).containsExactly(AlbumUiAction.ShowSnackBar(R.string.ui_song_unavailable_offline))
            verify(exactly = 0) { navigator.navigate(any()) }
        }

    @Test
    fun `GIVEN a loaded album WHEN swiping a track toward the end THEN adds it to the queue and says so`() =
        runTest(mainDispatcher.dispatcher) {
            // Given
            val album = album(id = 10)
            prepareScenario(cached = album)

            // When
            viewModel.onEvent(AlbumUiEvent.OnSongSwipedToQueue(album.songs[1]))

            // Then
            verify { enqueuer.addToQueue(listOf(album.songs[1])) }
            assertThat(actions).containsExactly(AlbumUiAction.ShowSnackBar(R.string.ui_added_to_queue))
        }

    @Test
    fun `GIVEN the user already queued the track WHEN swiping it toward the end THEN asks before adding it again`() =
        runTest(mainDispatcher.dispatcher) {
            // Given
            val album = album(id = 10)
            prepareScenario(
                cached = album,
                playback = playbackState(
                    entries = listOf(
                        queueEntry(song = song(id = 99)),
                        queueEntry(song = album.songs[1], source = QueueSource.UserQueue),
                    ),
                ),
            )

            // When
            viewModel.onEvent(AlbumUiEvent.OnSongSwipedToQueue(album.songs[1]))
            runCurrent()

            // Then
            assertThat((viewModel.uiState.value as AlbumUiState.Loaded).songAlreadyQueued).isEqualTo(album.songs[1])
            verify(exactly = 0) { enqueuer.addToQueue(any()) }
            assertThat(actions).isEmpty()
        }

    @Test
    fun `GIVEN the user already queued the track WHEN confirming THEN it is queued again`() =
        runTest(mainDispatcher.dispatcher) {
            // Given
            val album = album(id = 10)
            prepareScenario(
                cached = album,
                playback = playbackState(
                    entries = listOf(
                        queueEntry(song = song(id = 99)),
                        queueEntry(song = album.songs[1], source = QueueSource.UserQueue),
                    ),
                ),
            )
            viewModel.onEvent(AlbumUiEvent.OnSongSwipedToQueue(album.songs[1]))
            runCurrent()

            // When
            viewModel.onEvent(AlbumUiEvent.OnDuplicateInQueueConfirmed)
            runCurrent()

            // Then
            assertThat((viewModel.uiState.value as AlbumUiState.Loaded).songAlreadyQueued).isNull()
            verify { enqueuer.addToQueue(listOf(album.songs[1])) }
            assertThat(actions).containsExactly(AlbumUiAction.ShowSnackBar(R.string.ui_added_to_queue))
        }

    @Test
    fun `GIVEN the user already queued the track WHEN cancelling THEN the queue is left alone`() =
        runTest(mainDispatcher.dispatcher) {
            // Given
            val album = album(id = 10)
            prepareScenario(
                cached = album,
                playback = playbackState(
                    entries = listOf(
                        queueEntry(song = song(id = 99)),
                        queueEntry(song = album.songs[1], source = QueueSource.UserQueue),
                    ),
                ),
            )
            viewModel.onEvent(AlbumUiEvent.OnSongSwipedToQueue(album.songs[1]))
            runCurrent()

            // When
            viewModel.onEvent(AlbumUiEvent.OnDuplicateInQueueDismissed)
            runCurrent()

            // Then
            assertThat((viewModel.uiState.value as AlbumUiState.Loaded).songAlreadyQueued).isNull()
            verify(exactly = 0) { enqueuer.addToQueue(any()) }
        }

    @Test
    fun `GIVEN a track the player cannot reach WHEN swiping it toward the end THEN leaves the queue alone`() =
        runTest(mainDispatcher.dispatcher) {
            // Given
            val album = album(id = 10)
            prepareScenario(cached = album, isOnlineAtStart = false)

            // When
            viewModel.onEvent(AlbumUiEvent.OnSongSwipedToQueue(album.songs[1]))

            // Then
            verify(exactly = 0) { enqueuer.addToQueue(any()) }
            assertThat(actions).containsExactly(AlbumUiAction.ShowSnackBar(R.string.ui_song_unavailable_offline))
        }

    @Test
    fun `GIVEN a loaded album WHEN swiping a track toward the start THEN likes it`() =
        runTest(mainDispatcher.dispatcher) {
            // Given
            val album = album(id = 10)
            prepareScenario(cached = album)

            // When
            viewModel.onEvent(AlbumUiEvent.OnSongSwipedToFavorite(album.songs[1]))
            runCurrent()

            // Then
            val state = viewModel.uiState.value as AlbumUiState.Loaded
            assertThat(state.favoriteSongIds).containsExactly(album.songs[1].id)
            assertThat(actions).containsExactly(AlbumUiAction.ShowSnackBar(R.string.ui_added_to_favorites))
        }

    @Test
    fun `GIVEN a liked track WHEN swiping it toward the start THEN takes the like back`() =
        runTest(mainDispatcher.dispatcher) {
            // Given
            val album = album(id = 10)
            prepareScenario(cached = album, likedSongIds = setOf(album.songs[1].id))

            // When
            viewModel.onEvent(AlbumUiEvent.OnSongSwipedToFavorite(album.songs[1]))
            runCurrent()

            // Then
            val state = viewModel.uiState.value as AlbumUiState.Loaded
            assertThat(state.favoriteSongIds).isEmpty()
            assertThat(actions).containsExactly(AlbumUiAction.ShowSnackBar(R.string.ui_removed_from_favorites))
        }

    @Test
    fun `GIVEN an album that is not liked WHEN clicking the heart THEN stores it`() =
        runTest(mainDispatcher.dispatcher) {
            // Given
            val album = album(id = 10)
            prepareScenario(cached = album, isFavorite = false)

            // When
            viewModel.onEvent(AlbumUiEvent.OnFavoriteClicked)
            runCurrent()

            // Then
            assertThat(favoriteToggles).containsExactly(10L to false)
        }

    @Test
    fun `GIVEN a liked album WHEN clicking the heart THEN passes the current state through`() =
        runTest(mainDispatcher.dispatcher) {
            // Given
            prepareScenario(cached = album(id = 10), isFavorite = true)

            // When
            viewModel.onEvent(AlbumUiEvent.OnFavoriteClicked)
            runCurrent()

            // Then
            assertThat(favoriteToggles).containsExactly(10L to true)
            assertThat((viewModel.uiState.value as AlbumUiState.Loaded).isFavorite).isTrue()
        }

    @Test
    fun `GIVEN the album has not loaded WHEN clicking the heart THEN does nothing`() =
        runTest(mainDispatcher.dispatcher) {
            // Given
            prepareScenario(cached = null)

            // When
            viewModel.onEvent(AlbumUiEvent.OnFavoriteClicked)
            runCurrent()

            // Then
            assertThat(favoriteToggles).isEmpty()
        }

    @Test
    fun `GIVEN nothing is playing WHEN pressing the album's play button THEN it starts from the top as the context`() =
        runTest(mainDispatcher.dispatcher) {
            // Given
            val album = album(id = 10)
            prepareScenario(cached = album)

            // When
            viewModel.onEvent(AlbumUiEvent.OnPlayPauseClicked)

            // Then
            assertThat(contextStarts).containsExactly(album.songs to album.asContext())
            verify(exactly = 0) { transportControls.togglePlayPause() }
        }

    @Test
    fun `GIVEN no connection WHEN pressing the album's play button THEN starts only the saved tracks`() =
        runTest(mainDispatcher.dispatcher) {
            // Given
            val album = album(id = 10)
            prepareScenario(cached = album, isOnlineAtStart = false, cachedPreviews = setOf(2L))

            // When
            viewModel.onEvent(AlbumUiEvent.OnPlayPauseClicked)

            // Then
            assertThat(contextStarts).containsExactly(listOf(album.songs[1]) to album.asContext())
        }

    @Test
    fun `GIVEN no connection and no saved track WHEN pressing the album's play button THEN shows a message instead`() =
        runTest(mainDispatcher.dispatcher) {
            // Given
            prepareScenario(cached = album(id = 10), isOnlineAtStart = false, cachedPreviews = emptySet())

            // When
            viewModel.onEvent(AlbumUiEvent.OnPlayPauseClicked)
            runCurrent()

            // Then
            assertThat(actions).containsExactly(AlbumUiAction.ShowSnackBar(R.string.ui_song_unavailable_offline))
            assertThat(contextStarts).isEmpty()
        }

    @Test
    fun `GIVEN the album has not loaded WHEN pressing its play button THEN does nothing`() =
        runTest(mainDispatcher.dispatcher) {
            // Given
            prepareScenario(cached = null)

            // When
            viewModel.onEvent(AlbumUiEvent.OnPlayPauseClicked)

            // Then
            assertThat(contextStarts).isEmpty()
            verify(exactly = 0) { transportControls.togglePlayPause() }
        }

    @Test
    fun `GIVEN the album is playing WHEN pressing its button THEN it pauses instead of starting over`() =
        runTest(mainDispatcher.dispatcher) {
            // Given
            val album = album(id = 10)
            prepareScenario(cached = album, playback = playbackState(songs = album.songs, context = album.asContext()))

            // When
            val state = viewModel.uiState.value as AlbumUiState.Loaded
            viewModel.onEvent(AlbumUiEvent.OnPlayPauseClicked)

            // Then
            assertThat(state.isPlaying).isTrue()
            verify { transportControls.togglePlayPause() }
            assertThat(contextStarts).isEmpty()
        }

    @Test
    fun `GIVEN the album is paused WHEN pressing its button THEN it resumes where it stopped`() =
        runTest(mainDispatcher.dispatcher) {
            // Given
            val album = album(id = 10)
            prepareScenario(
                cached = album,
                playback = playbackState(
                    songs = album.songs,
                    context = album.asContext(),
                    status = PlaybackStatus.Paused,
                ),
            )

            // When
            val state = viewModel.uiState.value as AlbumUiState.Loaded
            viewModel.onEvent(AlbumUiEvent.OnPlayPauseClicked)

            // Then
            assertThat(state.isPlaying).isFalse()
            verify { transportControls.togglePlayPause() }
            assertThat(contextStarts).isEmpty()
        }

    @Test
    fun `GIVEN the album is paused on a track the player cannot reach WHEN pressing its button THEN shows a message`() =
        runTest(mainDispatcher.dispatcher) {
            // Given
            val album = album(id = 10)
            prepareScenario(
                cached = album,
                playback = playbackState(
                    songs = album.songs,
                    context = album.asContext(),
                    status = PlaybackStatus.Paused,
                ),
                isOnlineAtStart = false,
                cachedPreviews = setOf(2L),
            )

            // When
            viewModel.onEvent(AlbumUiEvent.OnPlayPauseClicked)
            runCurrent()

            // Then
            assertThat(actions).containsExactly(AlbumUiAction.ShowSnackBar(R.string.ui_song_unavailable_offline))
            verify(exactly = 0) { transportControls.togglePlayPause() }
        }

    @Test
    fun `GIVEN the album played to its end WHEN pressing its button THEN it starts over from the top`() =
        runTest(mainDispatcher.dispatcher) {
            // Given
            val album = album(id = 10)
            prepareScenario(
                cached = album,
                playback = playbackState(
                    songs = album.songs,
                    context = album.asContext(),
                    status = PlaybackStatus.Ended,
                ),
            )

            // When
            viewModel.onEvent(AlbumUiEvent.OnPlayPauseClicked)

            // Then
            assertThat(contextStarts).containsExactly(album.songs to album.asContext())
        }

    @Test
    fun `GIVEN another album is playing WHEN pressing this one's button THEN this album takes over`() =
        runTest(mainDispatcher.dispatcher) {
            // Given
            val album = album(id = 10)
            prepareScenario(
                cached = album,
                playback = playbackState(
                    songs = listOf(song(id = 50)),
                    context = PlaybackContext.Album(id = 20, title = "Discovery"),
                ),
            )

            // When
            val state = viewModel.uiState.value as AlbumUiState.Loaded
            viewModel.onEvent(AlbumUiEvent.OnPlayPauseClicked)

            // Then
            assertThat(state.isPlaying).isFalse()
            assertThat(contextStarts).containsExactly(album.songs to album.asContext())
        }

    @Test
    fun `GIVEN shuffle is on WHEN observing the album THEN its shuffle button shows it and a tap turns it off`() =
        runTest(mainDispatcher.dispatcher) {
            // Given
            prepareScenario(cached = album(id = 10), playback = playbackState(isShuffleEnabled = true))

            // When
            val state = viewModel.uiState.value as AlbumUiState.Loaded
            viewModel.onEvent(AlbumUiEvent.OnShuffleClicked)

            // Then
            assertThat(state.isShuffleEnabled).isTrue()
            verify { transportControls.toggleShuffle() }
        }

    @Test
    fun `GIVEN a loaded album WHEN clicking the overflow THEN opens the album options`() =
        runTest(mainDispatcher.dispatcher) {
            // Given
            prepareScenario(cached = album(id = 10))

            // When
            viewModel.onEvent(AlbumUiEvent.OnMoreClicked)

            // Then
            verify { navigator.navigate(AlbumOptionsRoute(albumId = 10)) }
        }

    @Test
    fun `GIVEN a track WHEN clicking its options THEN opens the same sheet the other lists open`() =
        runTest(mainDispatcher.dispatcher) {
            // Given
            val album = album(id = 10)
            prepareScenario(cached = album)

            // When
            viewModel.onEvent(AlbumUiEvent.OnSongOptionsClicked(album.songs.first()))

            // Then
            verify {
                navigator.navigate(
                    SongOptionsRoute(
                        songId = album.songs.first().id,
                        reorderTarget = ReorderTarget.Album(albumId = 10),
                    ),
                )
            }
        }

    @Test
    fun `GIVEN an album WHEN a sheet asks to reorder it THEN its rows turn into ones to drag`() =
        runTest(mainDispatcher.dispatcher) {
            // Given
            prepareScenario(cached = album(id = 10))

            // When
            reorderRequests.request(ReorderTarget.Album(albumId = 10))
            runCurrent()

            // Then
            assertThat((viewModel.uiState.value as AlbumUiState.Loaded).isReordering).isTrue()
        }

    @Test
    fun `GIVEN an album WHEN a sheet asks to reorder another THEN stays as it is`() =
        runTest(mainDispatcher.dispatcher) {
            // Given
            prepareScenario(cached = album(id = 10))

            // When
            reorderRequests.request(ReorderTarget.Album(albumId = 11))
            reorderRequests.request(ReorderTarget.Playlist(playlistId = 10))
            runCurrent()

            // Then
            assertThat((viewModel.uiState.value as AlbumUiState.Loaded).isReordering).isFalse()
        }

    @Test
    fun `GIVEN an album being reordered WHEN moving a track THEN the rows follow and the order is stored`() =
        runTest(mainDispatcher.dispatcher) {
            // Given
            prepareScenario(cached = album(id = 10))
            viewModel.onEvent(AlbumUiEvent.OnReorderStarted)

            // When
            viewModel.onEvent(AlbumUiEvent.OnSongMoved(fromSongId = 2, toSongId = 1))
            runCurrent()

            // Then
            val state = viewModel.uiState.value as AlbumUiState.Loaded
            assertThat(state.isReordering).isTrue()
            assertThat(state.album.songs.map { song -> song.id }).containsExactly(2L, 1L).inOrder()
            assertThat(savedTrackOrders).containsExactly(10L to listOf(2L, 1L))
        }

    @Test
    fun `GIVEN a reordered album WHEN pressing play THEN starts it in the new order`() =
        runTest(mainDispatcher.dispatcher) {
            // Given
            prepareScenario(cached = album(id = 10))
            viewModel.onEvent(AlbumUiEvent.OnSongMoved(fromSongId = 2, toSongId = 1))
            runCurrent()

            // When
            viewModel.onEvent(AlbumUiEvent.OnPlayPauseClicked)

            // Then
            assertThat(contextStarts.single().first.map { song -> song.id }).containsExactly(2L, 1L).inOrder()
        }

    @Test
    fun `GIVEN an album being reordered WHEN pressing back THEN finishes reordering and stays`() =
        runTest(mainDispatcher.dispatcher) {
            // Given
            prepareScenario(cached = album(id = 10))
            viewModel.onEvent(AlbumUiEvent.OnReorderStarted)

            // When
            viewModel.onEvent(AlbumUiEvent.OnBackClicked)

            // Then
            assertThat((viewModel.uiState.value as AlbumUiState.Loaded).isReordering).isFalse()
            verify(exactly = 0) { navigator.navigateBack() }
        }

    @Test
    fun `GIVEN an album being reordered WHEN clicking done THEN the rows go back to normal`() =
        runTest(mainDispatcher.dispatcher) {
            // Given
            prepareScenario(cached = album(id = 10))
            viewModel.onEvent(AlbumUiEvent.OnReorderStarted)

            // When
            viewModel.onEvent(AlbumUiEvent.OnReorderFinished)

            // Then
            assertThat((viewModel.uiState.value as AlbumUiState.Loaded).isReordering).isFalse()
        }

    @Test
    fun `WHEN clicking back THEN navigates back`() = runTest(mainDispatcher.dispatcher) {
        // Given
        prepareScenario(cached = album(id = 10))

        // When
        viewModel.onEvent(AlbumUiEvent.OnBackClicked)

        // Then
        verify { navigator.navigateBack() }
    }

    @Test
    fun `GIVEN a track not on the device WHEN the connection drops THEN marks its row`() =
        runTest(mainDispatcher.dispatcher) {
            // Given
            prepareScenario(cached = album(id = 10), cachedPreviews = setOf(1L))

            // When
            isOnline.value = false
            runCurrent()

            // Then
            val state = viewModel.uiState.value as AlbumUiState.Loaded
            assertThat(state.unplayableSongIds).containsExactly(2L)
        }

    private fun TestScope.prepareScenario(
        cached: Album?,
        refreshResult: Result<Unit>? = Result.success(Unit),
        playback: PlaybackState = PlaybackState.Idle,
        isFavorite: Boolean = false,
        isOnlineAtStart: Boolean = true,
        cachedPreviews: Set<Long> = emptySet(),
        likedSongIds: Set<Long> = emptySet(),
        downloadStatuses: Map<Long, SongDownloadStatus> = emptyMap(),
        downloadedCollections: Set<LibraryItemKey> = emptySet(),
    ) {
        localAlbum = MutableStateFlow(cached)
        downloadToggles = mutableListOf()
        refreshResults = refreshResult
        refreshCalls = mutableListOf()
        favoriteToggles = mutableListOf()
        isOnline = MutableStateFlow(isOnlineAtStart)
        actions = mutableListOf()
        val playbackStateFlow = MutableStateFlow(playback)
        songPlayback = FakeSongPlayback()
        contextStarts = mutableListOf()
        transportControls = mockk(relaxUnitFun = true)
        navigator = mockk(relaxUnitFun = true)
        enqueuer = mockk(relaxUnitFun = true)
        favoriteSongIds = MutableStateFlow(likedSongIds)
        reorderRequests = SharedFlowReorderRequests()
        savedTrackOrders = mutableListOf()
        viewModel = AlbumViewModel(
            route = AlbumRoute(albumId = 10),
            useCases = AlbumUseCases(
                observeAlbum = { localAlbum },
                refreshAlbum = { albumId ->
                    refreshCalls += albumId
                    refreshResults ?: awaitCancellation()
                },
                isAlbumFavorite = { flowOf(isFavorite) },
                toggleAlbumFavorite = { album, wasFavorite -> favoriteToggles += album.id to wasFavorite },
                observeIsOnline = { isOnline },
                observeFavoriteSongIds = { favoriteSongIds },
                toggleSongFavorite = { song, isFavorite ->
                    favoriteSongIds.value = if (isFavorite) {
                        favoriteSongIds.value - song.id
                    } else {
                        favoriteSongIds.value + song.id
                    }
                },
                saveTrackOrder = { albumId, songIds -> savedTrackOrders += albumId to songIds },
                observeCollectionDownloads = { flowOf(downloadedCollections) },
                toggleAlbumDownload = { album, isDownloaded, isFavorite ->
                    downloadToggles +=
                        DownloadToggle(albumId = album.id, isDownloaded = isDownloaded, isFavorite = isFavorite)
                },
            ),
            observablePlayback = { playbackStateFlow },
            songPlayback = songPlayback,
            contextStarter = { songs, context -> contextStarts += songs to context },
            enqueuer = enqueuer,
            transportControls = transportControls,
            playableSongs = PlayableSongs { song -> isOnline.value || song.id in cachedPreviews },
            navigator = navigator,
            reorderRequests = reorderRequests,
            observablePlayableSongs = {
                isOnline.map { isOnline -> PlayableSongs { song -> isOnline || song.id in cachedPreviews } }
            },
            observableDownloads = { flowOf(downloadStatuses) },
        )
        backgroundScope.launch { viewModel.uiState.collect {} }
        backgroundScope.launch { viewModel.uiAction.collect { action -> actions += action } }
        runCurrent()
    }

    private fun Album.asContext(): PlaybackContext = PlaybackContext.Album(id = id, title = title)

    private data class DownloadToggle(
        val albumId: Long,
        val isDownloaded: Boolean,
        val isFavorite: Boolean,
    )

    companion object {
        @JvmField
        @RegisterExtension
        val mainDispatcher = MainDispatcherExtension()
    }
}
