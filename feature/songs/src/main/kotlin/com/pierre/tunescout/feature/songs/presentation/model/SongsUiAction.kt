package com.pierre.tunescout.feature.songs.presentation.model

import androidx.annotation.StringRes

sealed interface SongsUiAction {
    data class ShowSnackBar(
        @StringRes val message: Int,
    ) : SongsUiAction
}
