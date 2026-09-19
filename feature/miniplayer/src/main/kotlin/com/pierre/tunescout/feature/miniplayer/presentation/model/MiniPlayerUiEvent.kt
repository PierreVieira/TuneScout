package com.pierre.tunescout.feature.miniplayer.presentation.model

sealed interface MiniPlayerUiEvent {
    data object OnClicked : MiniPlayerUiEvent

    data object OnPlayPauseClicked : MiniPlayerUiEvent
}
