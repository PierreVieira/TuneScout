package com.pierre.tunescout.feature.audiosearch.presentation.model

sealed interface AudioSearchUiEvent {
    data object OnRetryClicked : AudioSearchUiEvent
}
