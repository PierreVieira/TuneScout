package com.pierre.tunescout.feature.library.presentation.viewmodel

import com.google.common.truth.Truth.assertThat
import com.pierre.tunescout.core.model.NowPlaying
import com.pierre.tunescout.core.model.PlaybackContext
import com.pierre.tunescout.core.model.PlaybackState
import com.pierre.tunescout.core.model.PlaybackStatus
import com.pierre.tunescout.core.model.Playlist
import com.pierre.tunescout.core.model.Song
import com.pierre.tunescout.core.navigation.Navigator
import com.pierre.tunescout.core.navigation.reorder.ReorderTarget
import com.pierre.tunescout.core.navigation.reorder.SharedFlowReorderRequests
import com.pierre.tunescout.core.navigation.route.FavoritesOptionsRoute
import com.pierre.tunescout.core.navigation.route.PlayerRoute
import com.pierre.tunescout.core.navigation.route.PlaylistOptionsRoute
import com.pierre.tunescout.core.navigation.route.SongOptionsRoute
import com.pierre.tunescout.core.playback.Enqueuer
import com.pierre.tunescout.core.playback.ObservablePlayback
import com.pierre.tunescout.core.playback.PlayableSongs
import com.pierre.tunescout.core.playback.SongPlayOutcome
import com.pierre.tunescout.core.playback.TransportControls
import com.pierre.tunescout.core.testing.extension.MainDispatcherExtension
import com.pierre.tunescout.core.testing.fake.FakeSongPlayback
import com.pierre.tunescout.core.testing.fake.SongPlayRequest
import com.pierre.tunescout.core.testing.fixture.playbackState
import com.pierre.tunescout.core.testing.fixture.playlist
import com.pierre.tunescout.core.testing.fixture.song
import com.pierre.tunescout.feature.library.domain.model.CollectionKey
import com.pierre.tunescout.feature.library.domain.usecase.CollectionUseCases
import com.pierre.tunescout.feature.library.presentation.mapper.CollectionStreams
import com.pierre.tunescout.feature.library.presentation.model.CollectionTitle
import com.pierre.tunescout.feature.library.presentation.model.CollectionUiAction
import com.pierre.tunescout.feature.library.presentation.model.CollectionUiEvent
import com.pierre.tunescout.feature.library.presentation.model.CollectionUiState
import com.pierre.tunescout.ui.component.R
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

class CollectionViewModelTest {
    private lateinit var viewModel: CollectionViewModel
    private lateinit var navigator: Navigator
    private lateinit var actions: MutableList<CollectionUiAction>
    private lateinit var songPlayback: FakeSongPlayback
    private lateinit var enqueuer: Enqueuer
    private lateinit var contextStarts: MutableList<Pair<List<Song>, PlaybackContext>>
    private lateinit var transportControls: TransportControls
    private lateinit var favoriteSongs: MutableStateFlow<List<Song>>
    private lateinit var reorderRequests: SharedFlowReorderRequests
    private lateinit var playlistReorders: MutableList<Pair<Long, List<Long>>>

    @Test
    fun `GIVEN the favourites WHEN observing THEN titles itself with the liked songs and cannot be deleted`() =
        runTest(mainDispatcher.dispatcher) {
            // Given
            prepareScenario(
                key = CollectionKey.Favorites,
                favorites = listOf(song(id = 1)),
                playback = playbackState(songs = listOf(song(id = 1))),
            )

            // When
            val state = viewModel.uiState.value as CollectionUiState.Loaded

            // Then
            assertThat(state.title).isEqualTo(CollectionTitle.Favorites)
            assertThat(state.songs).hasSize(1)
            assertThat(state.nowPlaying?.isPlaying).isTrue()
            assertThat(state.isDeletable).isFalse()
        }

    @Test
    fun `GIVEN a playlist WHEN observing THEN takes its name and can be deleted`() =
        runTest(mainDispatcher.dispatcher) {
            // Given
            prepareScenario(
                key = CollectionKey.Playlist(playlistId = 7),
                playlist = playlist(id = 7, name = "Road trip"),
                playlistSongs = listOf(song(id = 2)),
            )

            // When
            val state = viewModel.uiState.value as CollectionUiState.Loaded

            // Then
            assertThat(state.title).isEqualTo(CollectionTitle.Custom(name = "Road trip"))
            assertThat(state.songs.map(Song::id)).containsExactly(2L)
            assertThat(state.isDeletable).isTrue()
        }

    @Test
    fun `GIVEN a playlist that no longer exists WHEN observing THEN stays loading`() =
        runTest(mainDispatcher.dispatcher) {
            // Given
            prepareScenario(key = CollectionKey.Playlist(playlistId = 7), playlist = null)

            // When
            val state = viewModel.uiState.value

            // Then
            assertThat(state).isEqualTo(CollectionUiState.Loading)
        }

    @Test
    fun `GIVEN a liked song WHEN clicking it THEN asks for it with the liked songs behind it`() =
        runTest(mainDispatcher.dispatcher) {
            // Given
            val songs = listOf(song(id = 1), song(id = 2), song(id = 3))
            prepareScenario(
                key = CollectionKey.Favorites,
                favorites = songs,
                playback = playbackState(songs = listOf(song(id = 9))),
            )

            // When
            viewModel.onEvent(CollectionUiEvent.OnSongClicked(song(id = 2)))

            // Then
            assertThat(songPlayback.requests).containsExactly(
                SongPlayRequest(
                    song = song(id = 2),
                    nowPlaying = NowPlaying(songId = 9, isPlaying = true),
                    queue = songs,
                    context = PlaybackContext.LikedSongs,
                ),
            )
            verify(exactly = 0) { navigator.navigate(any()) }
        }

    @Test
    fun `GIVEN a playlist song WHEN clicking it THEN asks for it with the playlist behind it`() =
        runTest(mainDispatcher.dispatcher) {
            // Given
            val songs = listOf(song(id = 1), song(id = 2))
            prepareScenario(
                key = CollectionKey.Playlist(playlistId = 7),
                playlist = playlist(id = 7, name = "Road trip"),
                playlistSongs = songs,
            )

            // When
            viewModel.onEvent(CollectionUiEvent.OnSongClicked(song(id = 1)))

            // Then
            assertThat(songPlayback.requests.single().queue).isEqualTo(songs)
            assertThat(songPlayback.requests.single().context)
                .isEqualTo(PlaybackContext.Playlist(id = 7, title = "Road trip"))
        }

    @Test
    fun `GIVEN a song WHEN clicking its options THEN opens the sheet`() = runTest(mainDispatcher.dispatcher) {
        // Given
        prepareScenario(key = CollectionKey.Favorites, favorites = listOf(song(id = 1)))

        // When
        viewModel.onEvent(CollectionUiEvent.OnSongOptionsClicked(song(id = 1)))

        // Then
        verify { navigator.navigate(SongOptionsRoute(songId = 1)) }
    }

    @Test
    fun `GIVEN the favourites WHEN pressing play THEN they start as the liked songs`() =
        runTest(mainDispatcher.dispatcher) {
            // Given
            val songs = listOf(song(id = 1), song(id = 2))
            prepareScenario(key = CollectionKey.Favorites, favorites = songs)

            // When
            viewModel.onEvent(CollectionUiEvent.OnPlayPauseClicked)

            // Then
            assertThat(contextStarts).containsExactly(songs to PlaybackContext.LikedSongs)
            verify(exactly = 0) { enqueuer.playNow(any()) }
        }

    @Test
    fun `GIVEN a playlist WHEN pressing play THEN it starts as its own context`() = runTest(mainDispatcher.dispatcher) {
        // Given
        val songs = listOf(song(id = 1), song(id = 2))
        prepareScenario(
            key = CollectionKey.Playlist(playlistId = 7),
            playlist = playlist(id = 7, name = "Road trip"),
            playlistSongs = songs,
        )

        // When
        viewModel.onEvent(CollectionUiEvent.OnPlayPauseClicked)

        // Then
        assertThat(contextStarts).containsExactly(songs to PlaybackContext.Playlist(id = 7, title = "Road trip"))
    }

    @Test
    fun `GIVEN one of the favourites is playing from an album WHEN pressing play THEN starts the liked songs`() =
        runTest(mainDispatcher.dispatcher) {
            // Given
            val songs = listOf(song(id = 1), song(id = 2))
            prepareScenario(
                key = CollectionKey.Favorites,
                favorites = songs,
                playback = playbackState(
                    songs = listOf(song(id = 2)),
                    context = PlaybackContext.Album(id = 10, title = "Discovery"),
                ),
            )

            // When
            val state = loadedState()
            viewModel.onEvent(CollectionUiEvent.OnPlayPauseClicked)

            // Then
            assertThat(state.isPlaying).isFalse()
            assertThat(contextStarts).containsExactly(songs to PlaybackContext.LikedSongs)
            verify(exactly = 0) { transportControls.togglePlayPause() }
        }

    @Test
    fun `GIVEN a playlist renamed since it started WHEN observing THEN it is still the one playing`() =
        runTest(mainDispatcher.dispatcher) {
            // Given
            prepareScenario(
                key = CollectionKey.Playlist(playlistId = 7),
                playlist = playlist(id = 7, name = "Road trip"),
                playlistSongs = listOf(song(id = 1)),
                playback = playbackState(
                    songs = listOf(song(id = 1)),
                    context = PlaybackContext.Playlist(id = 7, title = "Summer"),
                ),
            )

            // When
            val state = loadedState()

            // Then
            assertThat(state.isPlaying).isTrue()
        }

    @Test
    fun `GIVEN the liked songs played to their end WHEN pressing play THEN they start over`() =
        runTest(mainDispatcher.dispatcher) {
            // Given
            val songs = listOf(song(id = 1), song(id = 2))
            prepareScenario(
                key = CollectionKey.Favorites,
                favorites = songs,
                playback = playbackState(
                    songs = listOf(song(id = 2)),
                    status = PlaybackStatus.Ended,
                    context = PlaybackContext.LikedSongs,
                ),
            )

            // When
            viewModel.onEvent(CollectionUiEvent.OnPlayPauseClicked)

            // Then
            assertThat(contextStarts).containsExactly(songs to PlaybackContext.LikedSongs)
            verify(exactly = 0) { transportControls.togglePlayPause() }
        }

    @Test
    fun `GIVEN an empty collection WHEN pressing play THEN does nothing`() = runTest(mainDispatcher.dispatcher) {
        // Given
        prepareScenario(key = CollectionKey.Favorites)

        // When
        viewModel.onEvent(CollectionUiEvent.OnPlayPauseClicked)

        // Then
        assertThat(contextStarts).isEmpty()
    }

    @Test
    fun `GIVEN one of the favourites is playing WHEN pressing its button THEN the player pauses`() =
        runTest(mainDispatcher.dispatcher) {
            // Given
            val songs = listOf(song(id = 1), song(id = 2))
            prepareScenario(
                key = CollectionKey.Favorites,
                favorites = songs,
                playback = playbackState(songs = listOf(song(id = 2)), context = PlaybackContext.LikedSongs),
            )

            // When
            val state = loadedState()
            viewModel.onEvent(CollectionUiEvent.OnPlayPauseClicked)

            // Then
            assertThat(state.isPlaying).isTrue()
            verify { transportControls.togglePlayPause() }
            assertThat(contextStarts).isEmpty()
        }

    @Test
    fun `GIVEN one of the favourites is paused WHEN pressing its button THEN the player resumes it`() =
        runTest(mainDispatcher.dispatcher) {
            // Given
            prepareScenario(
                key = CollectionKey.Favorites,
                favorites = listOf(song(id = 1)),
                playback = playbackState(
                    songs = listOf(song(id = 1)),
                    status = PlaybackStatus.Paused,
                    context = PlaybackContext.LikedSongs,
                ),
            )

            // When
            val state = loadedState()
            viewModel.onEvent(CollectionUiEvent.OnPlayPauseClicked)

            // Then
            assertThat(state.isPlaying).isFalse()
            verify { transportControls.togglePlayPause() }
        }

    @Test
    fun `GIVEN one of the favourites is paused offline WHEN pressing its button THEN says it cannot be reached`() =
        runTest(mainDispatcher.dispatcher) {
            // Given
            prepareScenario(
                key = CollectionKey.Favorites,
                favorites = listOf(song(id = 1), song(id = 2)),
                playableSongIds = setOf(2L),
                playback = playbackState(
                    songs = listOf(song(id = 1)),
                    status = PlaybackStatus.Paused,
                    context = PlaybackContext.LikedSongs,
                ),
            )

            // When
            viewModel.onEvent(CollectionUiEvent.OnPlayPauseClicked)
            runCurrent()

            // Then
            assertThat(actions).containsExactly(CollectionUiAction.ShowSnackBar(R.string.ui_song_unavailable_offline))
            verify(exactly = 0) { transportControls.togglePlayPause() }
        }

    @Test
    fun `GIVEN shuffle is on WHEN pressing play THEN the collection is handed over in its own order`() =
        runTest(mainDispatcher.dispatcher) {
            // Given
            val songs = (1L..20L).map { id -> song(id = id) }
            prepareScenario(
                key = CollectionKey.Favorites,
                favorites = songs,
                playback = playbackState(songs = listOf(song(id = 99)), isShuffleEnabled = true),
            )

            // When
            viewModel.onEvent(CollectionUiEvent.OnPlayPauseClicked)

            // Then
            assertThat(loadedState().isShuffleEnabled).isTrue()
            assertThat(contextStarts).containsExactly(songs to PlaybackContext.LikedSongs)
        }

    @Test
    fun `WHEN clicking shuffle THEN the player's shuffle mode is toggled`() = runTest(mainDispatcher.dispatcher) {
        // Given
        prepareScenario(key = CollectionKey.Favorites, favorites = listOf(song(id = 1)))

        // When
        viewModel.onEvent(CollectionUiEvent.OnShuffleClicked)

        // Then
        verify { transportControls.toggleShuffle() }
    }

    @Test
    fun `GIVEN the favourites WHEN clicking the overflow THEN opens their options sheet`() =
        runTest(mainDispatcher.dispatcher) {
            // Given
            prepareScenario(key = CollectionKey.Favorites, favorites = listOf(song(id = 1)))

            // When
            viewModel.onEvent(CollectionUiEvent.OnMoreClicked)

            // Then
            verify { navigator.navigate(FavoritesOptionsRoute) }
        }

    @Test
    fun `GIVEN a playlist WHEN clicking the overflow THEN opens its own options sheet`() =
        runTest(mainDispatcher.dispatcher) {
            // Given
            prepareScenario(
                key = CollectionKey.Playlist(playlistId = 7),
                playlist = playlist(id = 7),
                playlistSongs = listOf(song(id = 2)),
            )

            // When
            viewModel.onEvent(CollectionUiEvent.OnMoreClicked)

            // Then
            verify { navigator.navigate(PlaylistOptionsRoute(playlistId = 7)) }
        }

    @Test
    fun `GIVEN a playlist WHEN clicking a song's options THEN opens the sheet able to take it out of the playlist`() =
        runTest(mainDispatcher.dispatcher) {
            // Given
            prepareScenario(
                key = CollectionKey.Playlist(playlistId = 7),
                playlist = playlist(id = 7),
                playlistSongs = listOf(song(id = 2)),
            )

            // When
            viewModel.onEvent(CollectionUiEvent.OnSongOptionsClicked(song(id = 2)))

            // Then
            verify {
                navigator.navigate(
                    SongOptionsRoute(
                        songId = 2,
                        playlistId = 7,
                        reorderTarget = ReorderTarget.Playlist(playlistId = 7),
                    ),
                )
            }
        }

    @Test
    fun `GIVEN a song WHEN swiping it toward the end THEN adds it to the queue and says so`() =
        runTest(mainDispatcher.dispatcher) {
            // Given
            prepareScenario(
                key = CollectionKey.Playlist(playlistId = 7),
                playlist = playlist(id = 7),
                playlistSongs = listOf(song(id = 2)),
            )

            // When
            viewModel.onEvent(CollectionUiEvent.OnSongSwipedToQueue(song(id = 2)))

            // Then
            verify { enqueuer.addToQueue(listOf(song(id = 2))) }
            assertThat(actions).containsExactly(CollectionUiAction.ShowSnackBar(R.string.ui_added_to_queue))
        }

    @Test
    fun `GIVEN a song the player cannot reach WHEN swiping it toward the end THEN leaves the queue alone`() =
        runTest(mainDispatcher.dispatcher) {
            // Given
            prepareScenario(
                key = CollectionKey.Playlist(playlistId = 7),
                playlist = playlist(id = 7),
                playlistSongs = listOf(song(id = 2)),
                playableSongIds = emptySet(),
            )

            // When
            viewModel.onEvent(CollectionUiEvent.OnSongSwipedToQueue(song(id = 2)))

            // Then
            verify(exactly = 0) { enqueuer.addToQueue(any()) }
            assertThat(actions).containsExactly(CollectionUiAction.ShowSnackBar(R.string.ui_song_unavailable_offline))
        }

    @Test
    fun `GIVEN a playlist WHEN swiping a song toward the start THEN likes it and keeps it in the playlist`() =
        runTest(mainDispatcher.dispatcher) {
            // Given
            prepareScenario(
                key = CollectionKey.Playlist(playlistId = 7),
                playlist = playlist(id = 7),
                playlistSongs = listOf(song(id = 2)),
            )

            // When
            viewModel.onEvent(CollectionUiEvent.OnSongSwipedToFavorite(song(id = 2)))
            runCurrent()

            // Then
            assertThat(loadedState().favoriteSongIds).containsExactly(2L)
            assertThat(loadedState().songs).containsExactly(song(id = 2))
            assertThat(actions).containsExactly(CollectionUiAction.ShowSnackBar(R.string.ui_added_to_favorites))
        }

    @Test
    fun `GIVEN the favourites WHEN swiping a song toward the start THEN takes the like back and it leaves the list`() =
        runTest(mainDispatcher.dispatcher) {
            // Given
            prepareScenario(key = CollectionKey.Favorites, favorites = listOf(song(id = 1)))

            // When
            viewModel.onEvent(CollectionUiEvent.OnSongSwipedToFavorite(song(id = 1)))
            runCurrent()

            // Then
            assertThat(loadedState().songs).isEmpty()
            assertThat(actions).containsExactly(
                CollectionUiAction.ShowSnackBar(R.string.ui_removed_from_favorites),
            )
        }

    @Test
    fun `GIVEN a song the player cannot reach WHEN clicking it THEN says so instead of playing it`() =
        runTest(mainDispatcher.dispatcher) {
            // Given
            prepareScenario(
                key = CollectionKey.Favorites,
                favorites = listOf(song(id = 1)),
                playableSongIds = emptySet(),
            )
            songPlayback.outcome = SongPlayOutcome.Unavailable

            // When
            viewModel.onEvent(CollectionUiEvent.OnSongClicked(song(id = 1)))

            // Then
            assertThat(actions).containsExactly(CollectionUiAction.ShowSnackBar(R.string.ui_song_unavailable_offline))
            verify(exactly = 0) { navigator.navigate(any()) }
        }

    @Test
    fun `GIVEN only one song the player can reach WHEN pressing play THEN plays that one alone`() =
        runTest(mainDispatcher.dispatcher) {
            // Given
            prepareScenario(
                key = CollectionKey.Favorites,
                favorites = listOf(song(id = 1), song(id = 2)),
                playableSongIds = setOf(2),
            )

            // When
            viewModel.onEvent(CollectionUiEvent.OnPlayPauseClicked)

            // Then
            assertThat(contextStarts).containsExactly(listOf(song(id = 2)) to PlaybackContext.LikedSongs)
        }

    @Test
    fun `GIVEN no song the player can reach WHEN pressing play THEN says so instead`() =
        runTest(mainDispatcher.dispatcher) {
            // Given
            prepareScenario(
                key = CollectionKey.Favorites,
                favorites = listOf(song(id = 1)),
                playableSongIds = emptySet(),
            )

            // When
            viewModel.onEvent(CollectionUiEvent.OnPlayPauseClicked)

            // Then
            assertThat(actions).containsExactly(CollectionUiAction.ShowSnackBar(R.string.ui_song_unavailable_offline))
            assertThat(contextStarts).isEmpty()
        }

    @Test
    fun `GIVEN the song the player is already on WHEN clicking its row THEN opens the player`() =
        runTest(mainDispatcher.dispatcher) {
            // Given
            prepareScenario(
                key = CollectionKey.Favorites,
                favorites = listOf(song(id = 1)),
                playback = playbackState(songs = listOf(song(id = 1))),
            )
            songPlayback.outcome = SongPlayOutcome.AlreadyPlaying

            // When
            viewModel.onEvent(CollectionUiEvent.OnSongClicked(song(id = 1)))

            // Then
            verify { navigator.navigate(PlayerRoute(songId = 1L)) }
            assertThat(actions).isEmpty()
        }

    private fun loadedState(): CollectionUiState.Loaded = viewModel.uiState.value as CollectionUiState.Loaded

    @Test
    fun `GIVEN songs the player cannot reach WHEN observing THEN marks their rows`() =
        runTest(mainDispatcher.dispatcher) {
            // Given
            prepareScenario(
                key = CollectionKey.Favorites,
                favorites = listOf(song(id = 1), song(id = 2)),
                playableSongIds = setOf(1L),
            )

            // When
            val state = viewModel.uiState.value as CollectionUiState.Loaded

            // Then
            assertThat(state.unplayableSongIds).containsExactly(2L)
        }

    @Test
    fun `GIVEN a playlist WHEN a sheet asks to reorder it THEN its rows turn into ones to drag`() =
        runTest(mainDispatcher.dispatcher) {
            // Given
            prepareScenario(key = CollectionKey.Playlist(playlistId = 7), playlist = playlist(id = 7))

            // When
            reorderRequests.request(ReorderTarget.Playlist(playlistId = 7))
            runCurrent()

            // Then
            assertThat(loadedState().isReorderable).isTrue()
            assertThat(loadedState().isReordering).isTrue()
        }

    @Test
    fun `GIVEN a playlist WHEN a sheet asks to reorder another THEN stays as it is`() =
        runTest(mainDispatcher.dispatcher) {
            // Given
            prepareScenario(key = CollectionKey.Playlist(playlistId = 7), playlist = playlist(id = 7))

            // When
            reorderRequests.request(ReorderTarget.Playlist(playlistId = 8))
            runCurrent()

            // Then
            assertThat(loadedState().isReordering).isFalse()
        }

    @Test
    fun `GIVEN the favourites WHEN a long press starts a drag THEN they cannot be reordered`() =
        runTest(mainDispatcher.dispatcher) {
            // Given
            prepareScenario(key = CollectionKey.Favorites, favorites = listOf(song(id = 1), song(id = 2)))

            // When
            viewModel.onEvent(CollectionUiEvent.OnReorderStarted)
            viewModel.onEvent(CollectionUiEvent.OnSongMoved(fromSongId = 1, toSongId = 2))
            runCurrent()

            // Then
            assertThat(loadedState().isReorderable).isFalse()
            assertThat(loadedState().isReordering).isFalse()
            assertThat(loadedState().songs.map { song -> song.id }).containsExactly(1L, 2L).inOrder()
            assertThat(playlistReorders).isEmpty()
        }

    @Test
    fun `GIVEN a playlist being reordered WHEN moving a song THEN the rows follow and the order is stored`() =
        runTest(mainDispatcher.dispatcher) {
            // Given
            prepareScenario(
                key = CollectionKey.Playlist(playlistId = 7),
                playlist = playlist(id = 7),
                playlistSongs = listOf(song(id = 1), song(id = 2), song(id = 3)),
            )
            viewModel.onEvent(CollectionUiEvent.OnReorderStarted)

            // When
            viewModel.onEvent(CollectionUiEvent.OnSongMoved(fromSongId = 1, toSongId = 3))
            runCurrent()

            // Then
            assertThat(loadedState().songs.map { song -> song.id }).containsExactly(2L, 3L, 1L).inOrder()
            assertThat(playlistReorders).containsExactly(7L to listOf(2L, 3L, 1L))
        }

    @Test
    fun `GIVEN a playlist being reordered WHEN pressing back THEN finishes reordering and stays`() =
        runTest(mainDispatcher.dispatcher) {
            // Given
            prepareScenario(key = CollectionKey.Playlist(playlistId = 7), playlist = playlist(id = 7))
            viewModel.onEvent(CollectionUiEvent.OnReorderStarted)

            // When
            viewModel.onEvent(CollectionUiEvent.OnBackClicked)

            // Then
            assertThat(loadedState().isReordering).isFalse()
            verify(exactly = 0) { navigator.navigateBack() }
        }

    @Test
    fun `GIVEN a playlist being reordered WHEN clicking done THEN the rows go back to normal`() =
        runTest(mainDispatcher.dispatcher) {
            // Given
            prepareScenario(key = CollectionKey.Playlist(playlistId = 7), playlist = playlist(id = 7))
            viewModel.onEvent(CollectionUiEvent.OnReorderStarted)

            // When
            viewModel.onEvent(CollectionUiEvent.OnReorderFinished)

            // Then
            assertThat(loadedState().isReordering).isFalse()
        }

    @Test
    fun `GIVEN a playlist not being reordered WHEN pressing back THEN leaves it`() =
        runTest(mainDispatcher.dispatcher) {
            // Given
            prepareScenario(key = CollectionKey.Playlist(playlistId = 7), playlist = playlist(id = 7))

            // When
            viewModel.onEvent(CollectionUiEvent.OnBackClicked)

            // Then
            verify { navigator.navigateBack() }
        }

    private fun TestScope.prepareScenario(
        key: CollectionKey,
        favorites: List<Song> = emptyList(),
        playlist: Playlist? = null,
        playlistSongs: List<Song> = emptyList(),
        playableSongIds: Set<Long>? = null,
        playback: PlaybackState = PlaybackState.Idle,
    ) {
        favoriteSongs = MutableStateFlow(favorites)
        reorderRequests = SharedFlowReorderRequests()
        playlistReorders = mutableListOf()
        actions = mutableListOf()
        navigator = mockk(relaxUnitFun = true)
        songPlayback = FakeSongPlayback()
        enqueuer = mockk(relaxUnitFun = true)
        contextStarts = mutableListOf()
        transportControls = mockk(relaxUnitFun = true)
        val useCases = CollectionUseCases(
            observePlaylist = { flowOf(playlist) },
            observePlaylistSongs = { flowOf(playlistSongs) },
            observeFavorites = { favoriteSongs },
            toggleSongFavorite = { song, isFavorite ->
                favoriteSongs.value = if (isFavorite) {
                    favoriteSongs.value.filterNot { favorite -> favorite.id == song.id }
                } else {
                    favoriteSongs.value + song
                }
            },
            deletePlaylist = { },
            reorderPlaylistSongs = { playlistId, songIds -> playlistReorders += playlistId to songIds },
        )
        val playableSongs = PlayableSongs { song -> playableSongIds?.contains(song.id) ?: true }
        viewModel = CollectionViewModel(
            key = key,
            useCases = useCases,
            collectionStreams = CollectionStreams(useCases),
            observablePlayback = ObservablePlayback { MutableStateFlow(playback) },
            songPlayback = songPlayback,
            contextStarter = { songs, context -> contextStarts += songs to context },
            enqueuer = enqueuer,
            transportControls = transportControls,
            playableSongs = playableSongs,
            navigator = navigator,
            reorderRequests = reorderRequests,
            observablePlayableSongs = { flowOf(playableSongs) },
        )
        backgroundScope.launch { viewModel.uiState.collect {} }
        backgroundScope.launch { viewModel.uiAction.collect { action -> actions += action } }
        runCurrent()
    }

    companion object {
        @JvmField
        @RegisterExtension
        val mainDispatcher = MainDispatcherExtension()
    }
}
