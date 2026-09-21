package com.pierre.tunescout.feature.songs.presentation.model

import androidx.annotation.StringRes

sealed interface SongsUiAction {
    data class ShowSnackBar(
        @StringRes val message: Int,
    ) : SongsUiAction

    data object RequestMicrophonePermission : SongsUiAction

    /**
     * The system no longer asks for the microphone, so the snackbar offers the one place left to
     * grant it.
     */
    data object ShowMicrophoneSettingsSnackBar : SongsUiAction
}
