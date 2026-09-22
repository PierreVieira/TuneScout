package com.pierre.tunescout.feature.player.presentation.model

import kotlin.time.Duration

sealed interface PlayerUiEvent {
    data object OnPlayPauseClicked : PlayerUiEvent

    data class OnSeekFinished(
        val position: Duration,
    ) : PlayerUiEvent

    data object OnSkipNextClicked : PlayerUiEvent

    data object OnSkipPreviousClicked : PlayerUiEvent

    data object OnRepeatClicked : PlayerUiEvent

    data object OnShuffleClicked : PlayerUiEvent

    data object OnQueueClicked : PlayerUiEvent

    data object OnBackClicked : PlayerUiEvent

    data object OnMoreClicked : PlayerUiEvent

    data object OnFavoriteClicked : PlayerUiEvent
}
