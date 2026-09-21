package com.pierre.tunescout.feature.library.presentation.viewmodel

import com.google.common.truth.Truth.assertThat
import com.pierre.tunescout.core.model.PlaybackContext
import com.pierre.tunescout.core.model.PlaybackState
import com.pierre.tunescout.core.model.PlaybackStatus
import com.pierre.tunescout.core.model.Playlist
import com.pierre.tunescout.core.model.Song
import com.pierre.tunescout.core.navigation.Navigator
import com.pierre.tunescout.core.navigation.route.FavoritesOptionsRoute
import com.pierre.tunescout.core.navigation.route.PlayerRoute
import com.pierre.tunescout.core.navigation.route.PlaylistOptionsRoute
import com.pierre.tunescout.core.navigation.route.SongOptionsRoute
import com.pierre.tunescout.core.playback.Enqueuer
import com.pierre.tunescout.core.playback.ObservablePlayback
import com.pierre.tunescout.core.playback.PlayableSongs
import com.pierre.tunescout.core.playback.PlaybackStarter
import com.pierre.tunescout.core.testing.extension.MainDispatcherExtension
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
    private lateinit var playbackStarter: PlaybackStarter
    private lateinit var enqueuer: Enqueuer
    private lateinit var removedFavoriteIds: MutableList<Long>
    private lateinit var removedFromPlaylist: MutableList<Pair<Long, Long>>

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
    fun `GIVEN a song WHEN clicking it THEN plays it alone and stays on the collection`() =
        runTest(mainDispatcher.dispatcher) {
            // Given
            prepareScenario(key = CollectionKey.Favorites, favorites = listOf(song(id = 1)))

            // When
            viewModel.onEvent(CollectionUiEvent.OnSongClicked(song(id = 1)))

            // Then
            verify {
                playbackStarter.play(
                    song = song(id = 1),
                    songs = listOf(song(id = 1)),
                    context = PlaybackContext.SingleSong,
                )
            }
            verify(exactly = 0) { navigator.navigate(any()) }
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
    fun `GIVEN the favourites WHEN playing them now THEN the whole list takes over the current song`() =
        runTest(mainDispatcher.dispatcher) {
            // Given
            val songs = listOf(song(id = 1), song(id = 2))
            prepareScenario(key = CollectionKey.Favorites, favorites = songs)

            // When
            viewModel.onEvent(CollectionUiEvent.OnPlayNowClicked)

            // Then
            verify { enqueuer.playNow(songs) }
        }

    @Test
    fun `GIVEN an empty collection WHEN playing it now THEN does nothing`() = runTest(mainDispatcher.dispatcher) {
        // Given
        prepareScenario(key = CollectionKey.Favorites)

        // When
        viewModel.onEvent(CollectionUiEvent.OnPlayNowClicked)

        // Then
        verify(exactly = 0) { enqueuer.playNow(any()) }
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
    fun `GIVEN the favourites WHEN swiping a song away THEN unlikes it without asking`() =
        runTest(mainDispatcher.dispatcher) {
            // Given
            prepareScenario(key = CollectionKey.Favorites, favorites = listOf(song(id = 1)))

            // When
            viewModel.onEvent(CollectionUiEvent.OnSongSwipedAway(song(id = 1)))
            runCurrent()

            // Then
            assertThat(removedFavoriteIds).containsExactly(1L)
            assertThat(removedFromPlaylist).isEmpty()
            assertThat(loadedState().songPendingRemoval).isNull()
        }

    @Test
    fun `GIVEN a playlist WHEN swiping a song away THEN only asks for confirmation`() =
        runTest(mainDispatcher.dispatcher) {
            // Given
            prepareScenario(
                key = CollectionKey.Playlist(playlistId = 7),
                playlist = playlist(id = 7),
                playlistSongs = listOf(song(id = 2)),
            )

            // When
            viewModel.onEvent(CollectionUiEvent.OnSongSwipedAway(song(id = 2)))
            runCurrent()

            // Then
            assertThat(loadedState().songPendingRemoval).isEqualTo(song(id = 2))
            assertThat(removedFromPlaylist).isEmpty()
        }

    @Test
    fun `GIVEN a pending removal WHEN confirming it THEN takes the song out of that playlist only`() =
        runTest(mainDispatcher.dispatcher) {
            // Given
            prepareScenario(
                key = CollectionKey.Playlist(playlistId = 7),
                playlist = playlist(id = 7),
                playlistSongs = listOf(song(id = 2)),
            )
            viewModel.onEvent(CollectionUiEvent.OnSongSwipedAway(song(id = 2)))

            // When
            viewModel.onEvent(CollectionUiEvent.OnRemovalConfirmed)
            runCurrent()

            // Then
            assertThat(removedFromPlaylist).containsExactly(7L to 2L)
            assertThat(removedFavoriteIds).isEmpty()
            assertThat(loadedState().songPendingRemoval).isNull()
        }

    @Test
    fun `GIVEN a pending removal WHEN dismissing it THEN the song stays in the playlist`() =
        runTest(mainDispatcher.dispatcher) {
            // Given
            prepareScenario(
                key = CollectionKey.Playlist(playlistId = 7),
                playlist = playlist(id = 7),
                playlistSongs = listOf(song(id = 2)),
            )
            viewModel.onEvent(CollectionUiEvent.OnSongSwipedAway(song(id = 2)))

            // When
            viewModel.onEvent(CollectionUiEvent.OnRemovalDismissed)
            runCurrent()

            // Then
            assertThat(loadedState().songPendingRemoval).isNull()
            assertThat(removedFromPlaylist).isEmpty()
        }

    @Test
    fun `GIVEN no pending removal WHEN confirming THEN does nothing`() = runTest(mainDispatcher.dispatcher) {
        // Given
        prepareScenario(
            key = CollectionKey.Playlist(playlistId = 7),
            playlist = playlist(id = 7),
            playlistSongs = listOf(song(id = 2)),
        )

        // When
        viewModel.onEvent(CollectionUiEvent.OnRemovalConfirmed)
        runCurrent()

        // Then
        assertThat(removedFromPlaylist).isEmpty()
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

            // When
            viewModel.onEvent(CollectionUiEvent.OnSongClicked(song(id = 1)))

            // Then
            assertThat(actions).containsExactly(CollectionUiAction.ShowSnackBar(R.string.ui_song_unavailable_offline))
            verify(exactly = 0) { playbackStarter.play(any(), any(), any()) }
        }

    @Test
    fun `GIVEN only one song the player can reach WHEN playing the collection now THEN plays that one alone`() =
        runTest(mainDispatcher.dispatcher) {
            // Given
            prepareScenario(
                key = CollectionKey.Favorites,
                favorites = listOf(song(id = 1), song(id = 2)),
                playableSongIds = setOf(2),
            )

            // When
            viewModel.onEvent(CollectionUiEvent.OnPlayNowClicked)

            // Then
            verify { enqueuer.playNow(listOf(song(id = 2))) }
        }

    @Test
    fun `GIVEN no song the player can reach WHEN playing the collection now THEN says so instead`() =
        runTest(mainDispatcher.dispatcher) {
            // Given
            prepareScenario(
                key = CollectionKey.Favorites,
                favorites = listOf(song(id = 1)),
                playableSongIds = emptySet(),
            )

            // When
            viewModel.onEvent(CollectionUiEvent.OnPlayNowClicked)

            // Then
            assertThat(actions).containsExactly(CollectionUiAction.ShowSnackBar(R.string.ui_song_unavailable_offline))
            verify(exactly = 0) { enqueuer.playNow(any()) }
        }

    @Test
    fun `GIVEN a playing song WHEN clicking its row THEN opens the player instead of starting it over`() =
        runTest(mainDispatcher.dispatcher) {
            // Given
            prepareScenario(
                key = CollectionKey.Favorites,
                favorites = listOf(song(id = 1)),
                playback = playbackState(songs = listOf(song(id = 1))),
            )

            // When
            viewModel.onEvent(CollectionUiEvent.OnSongClicked(song(id = 1)))

            // Then
            verify { navigator.navigate(PlayerRoute(songId = 1L)) }
            verify(exactly = 0) { playbackStarter.play(any(), any(), any()) }
        }

    @Test
    fun `GIVEN a paused song WHEN clicking its row THEN opens the player instead of starting it over`() =
        runTest(mainDispatcher.dispatcher) {
            // Given
            prepareScenario(
                key = CollectionKey.Favorites,
                favorites = listOf(song(id = 1)),
                playback = playbackState(songs = listOf(song(id = 1)), status = PlaybackStatus.Paused),
            )

            // When
            viewModel.onEvent(CollectionUiEvent.OnSongClicked(song(id = 1)))

            // Then
            verify { navigator.navigate(PlayerRoute(songId = 1L)) }
            verify(exactly = 0) { playbackStarter.play(any(), any(), any()) }
        }

    @Test
    fun `GIVEN a song that ended WHEN clicking its row THEN plays it again from the start`() =
        runTest(mainDispatcher.dispatcher) {
            // Given
            prepareScenario(
                key = CollectionKey.Favorites,
                favorites = listOf(song(id = 1)),
                playback = playbackState(songs = listOf(song(id = 1)), status = PlaybackStatus.Ended),
            )

            // When
            viewModel.onEvent(CollectionUiEvent.OnSongClicked(song(id = 1)))

            // Then
            verify {
                playbackStarter.play(
                    song = song(id = 1),
                    songs = listOf(song(id = 1)),
                    context = PlaybackContext.SingleSong,
                )
            }
            verify(exactly = 0) { navigator.navigate(any()) }
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

    private fun TestScope.prepareScenario(
        key: CollectionKey,
        favorites: List<Song> = emptyList(),
        playlist: Playlist? = null,
        playlistSongs: List<Song> = emptyList(),
        playableSongIds: Set<Long>? = null,
        playback: PlaybackState = PlaybackState.Idle,
    ) {
        removedFavoriteIds = mutableListOf()
        actions = mutableListOf()
        removedFromPlaylist = mutableListOf()
        navigator = mockk(relaxUnitFun = true)
        playbackStarter = mockk(relaxUnitFun = true)
        enqueuer = mockk(relaxUnitFun = true)
        val useCases = CollectionUseCases(
            observePlaylist = { flowOf(playlist) },
            observePlaylistSongs = { flowOf(playlistSongs) },
            observeFavorites = { flowOf(favorites) },
            removeSongFromPlaylist = { playlistId, songId -> removedFromPlaylist += playlistId to songId },
            removeFavorite = { songId -> removedFavoriteIds += songId },
            deletePlaylist = { },
        )
        val playableSongs = PlayableSongs { song -> playableSongIds?.contains(song.id) ?: true }
        viewModel = CollectionViewModel(
            key = key,
            useCases = useCases,
            collectionStreams = CollectionStreams(useCases),
            observablePlayback = ObservablePlayback { MutableStateFlow(playback) },
            playbackStarter = playbackStarter,
            enqueuer = enqueuer,
            playableSongs = playableSongs,
            navigator = navigator,
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
