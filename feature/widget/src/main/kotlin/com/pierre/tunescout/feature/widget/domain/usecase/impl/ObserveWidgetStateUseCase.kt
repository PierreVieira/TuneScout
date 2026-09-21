package com.pierre.tunescout.feature.widget.domain.usecase.impl

import com.pierre.tunescout.core.database.RecentlyPlayedLocalDataSource
import com.pierre.tunescout.core.model.PlaybackState
import com.pierre.tunescout.core.model.Song
import com.pierre.tunescout.core.playback.ObservablePlayback
import com.pierre.tunescout.feature.widget.domain.model.WidgetState
import com.pierre.tunescout.feature.widget.domain.usecase.ObserveWidgetState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged

class ObserveWidgetStateUseCase(
    private val observablePlayback: ObservablePlayback,
    private val recentlyPlayedLocalDataSource: RecentlyPlayedLocalDataSource,
) : ObserveWidgetState {
    /**
     * @return the widget state, without the repeats the position ticker would otherwise cause:
     * the state is mapped first and compared after, so only a change the widget can show reaches
     * the launcher.
     */
    override fun invoke(): Flow<WidgetState> = combine(
        observablePlayback.observePlaybackState(),
        recentlyPlayedLocalDataSource.observe(WidgetState.SHORTCUT_COUNT),
        ::toWidgetState,
    ).distinctUntilChanged()

    private fun toWidgetState(
        playback: PlaybackState,
        shortcuts: List<Song>,
    ): WidgetState = WidgetState(
        song = playback.currentSong,
        isPlaying = playback.isPlaying,
        hasPrevious = playback.hasPrevious,
        hasNext = playback.hasNext,
        shortcuts = shortcuts,
    )
}
