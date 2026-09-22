package com.pierre.tunescout.feature.player.presentation.viewmodel

import androidx.lifecycle.viewModelScope
import com.pierre.tunescout.core.model.PlaybackContext
import com.pierre.tunescout.core.model.PlaybackState
import com.pierre.tunescout.core.model.Song
import com.pierre.tunescout.core.navigation.Navigator
import com.pierre.tunescout.core.navigation.route.QueueRoute
import com.pierre.tunescout.core.navigation.route.SongOptionsRoute
import com.pierre.tunescout.core.playback.ObservablePlayback
import com.pierre.tunescout.core.playback.PlayableSongs
import com.pierre.tunescout.core.playback.PlaybackStarter
import com.pierre.tunescout.core.playback.TransportControls
import com.pierre.tunescout.feature.player.domain.usecase.IsFavorite
import com.pierre.tunescout.feature.player.domain.usecase.ObserveSong
import com.pierre.tunescout.feature.player.domain.usecase.ToggleFavorite
import com.pierre.tunescout.feature.player.presentation.model.PlayerUiAction
import com.pierre.tunescout.feature.player.presentation.model.PlayerUiEvent
import com.pierre.tunescout.feature.player.presentation.model.PlayerUiState
import com.pierre.tunescout.ui.component.R
import com.pierre.tunescout.ui.utils.ActionViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * The player of one song, or of whatever is playing.
 *
 * @property songId the song the player was opened on, which it shows until another one plays; null
 * for the pane beside the tabs, which only follows what is playing and is empty while nothing is.
 * @property observablePlayback what is playing, and where.
 * @property playbackStarter starts the shown song when it is not the one playing.
 * @property playableSongs whether a song can be reached right now.
 * @property transportControls the controls of what is playing.
 * @property navigator opens the queue and the song's options, and closes the player.
 * @property toggleFavorite likes or unlikes the song shown right now.
 * @param observeSong the song the player was opened on, as the library knows it.
 * @param isFavorite whether the song shown right now is liked.
 */
class PlayerViewModel(
    private val songId: Long?,
    private val observablePlayback: ObservablePlayback,
    private val playbackStarter: PlaybackStarter,
    private val playableSongs: PlayableSongs,
    private val transportControls: TransportControls,
    private val navigator: Navigator,
    private val toggleFavorite: ToggleFavorite,
    observeSong: ObserveSong,
    isFavorite: IsFavorite,
) : ActionViewModel<PlayerUiAction>() {
    private val playback: PlaybackState
        get() = observablePlayback.observePlaybackState().value

    private val currentSong: Song?
        get() = playback.currentSong

    /** The song the player would move on to, and nothing while the queue ends on the current one. */
    private val nextSong: Song?
        get() = playback.upcomingEntries.firstOrNull()?.song

    /** The song the player would go back to, and nothing while it would start this one over. */
    private val previousSong: Song?
        get() = playback.previousEntry?.song

    /** The song shown right now: whatever is playing, or the one the player was opened on. */
    private val shownSongFlow: Flow<Song?> = combine(
        songId?.let(observeSong::invoke) ?: flowOf(null),
        observablePlayback.observePlaybackState(),
    ) { routeSong, playback -> playback.currentSong ?: routeSong }

    /** Re-checked only when the shown song itself changes, not on every playback update. */
    private val isShownSongFavorite: Flow<Boolean> = shownSongFlow
        .distinctUntilChanged { previous, next -> previous?.id == next?.id }
        .flatMapLatest { song -> song?.let { isFavorite(it.id) } ?: flowOf(false) }

    val uiState: StateFlow<PlayerUiState> = combine(
        shownSongFlow,
        observablePlayback.observePlaybackState(),
        isShownSongFavorite,
        ::toUiState,
    ).stateIn(viewModelScope, SharingStarted.WhileSubscribed(), PlayerUiState.Loading)

    fun onEvent(event: PlayerUiEvent) = when (event) {
        PlayerUiEvent.OnPlayPauseClicked -> togglePlayPause()
        is PlayerUiEvent.OnSeekFinished -> transportControls.seekTo(event.position)
        PlayerUiEvent.OnSkipNextClicked -> skipToNext()
        PlayerUiEvent.OnSkipPreviousClicked -> skipToPrevious()
        PlayerUiEvent.OnRepeatClicked -> transportControls.cycleRepeatMode()
        PlayerUiEvent.OnShuffleClicked -> transportControls.toggleShuffle()
        PlayerUiEvent.OnQueueClicked -> navigator.navigate(QueueRoute)
        PlayerUiEvent.OnBackClicked -> navigator.navigateBack()
        PlayerUiEvent.OnMoreClicked -> navigateToOptions()
        PlayerUiEvent.OnFavoriteClicked -> toggleShownSongFavorite()
    }

    /**
     * Pausing is always honoured; starting is not. A song the player cannot reach is refused with a
     * message, whether it would be started from the beginning or resumed where it stopped.
     */
    private fun togglePlayPause() {
        val shownSong = (uiState.value as? PlayerUiState.Loaded)?.song ?: return
        if (playback.isPlaying) return transportControls.togglePlayPause()
        if (!playableSongs.isPlayable(shownSong)) return showSongUnavailableOffline()
        if (currentSong?.id == shownSong.id) {
            transportControls.togglePlayPause()
        } else {
            playbackStarter.play(
                song = shownSong,
                songs = listOf(shownSong),
                context = PlaybackContext.SingleSong,
            )
        }
    }

    /** The next song was queued while the player could reach it, which it may no longer be able to. */
    private fun skipToNext() {
        val next = nextSong ?: return transportControls.skipToNext()
        if (!playableSongs.isPlayable(next)) return showSongUnavailableOffline()
        transportControls.skipToNext()
    }

    /**
     * Going back is refused on the same grounds as going on, and only when it would leave the
     * current song: starting the song the player is already on over asks nothing of the network.
     */
    private fun skipToPrevious() {
        val previous = previousSong ?: return transportControls.skipToPrevious()
        if (!playableSongs.isPlayable(previous)) return showSongUnavailableOffline()
        transportControls.skipToPrevious()
    }

    private fun showSongUnavailableOffline() {
        emitAction(PlayerUiAction.ShowSnackBar(R.string.ui_song_unavailable_offline))
    }

    private fun navigateToOptions() {
        val shownSongId = (uiState.value as? PlayerUiState.Loaded)?.song?.id ?: songId ?: return
        navigator.navigate(SongOptionsRoute(songId = shownSongId, hidesFavorite = true))
    }

    private fun toggleShownSongFavorite() {
        val state = uiState.value as? PlayerUiState.Loaded ?: return
        viewModelScope.launch {
            toggleFavorite(song = state.song, isFavorite = state.isFavorite)
        }
    }

    private fun toUiState(
        song: Song?,
        playback: PlaybackState,
        isFavorite: Boolean,
    ): PlayerUiState {
        if (song == null) {
            return when (songId) {
                null -> PlayerUiState.NothingPlaying
                else -> PlayerUiState.NotFound
            }
        }
        val isCurrent = playback.currentSong?.id == song.id
        return PlayerUiState.Loaded(
            song = song,
            status = playback.status,
            position = if (isCurrent) playback.position else PlaybackState.Idle.position,
            duration = if (isCurrent &&
                playback.duration > PlaybackState.Idle.duration
            ) {
                playback.duration
            } else {
                song.duration
            },
            repeatMode = playback.repeatMode,
            isShuffleEnabled = playback.isShuffleEnabled,
            hasPrevious = playback.hasPrevious,
            hasNext = playback.hasNext,
            isFavorite = isFavorite,
        )
    }
}
