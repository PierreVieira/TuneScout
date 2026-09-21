package com.pierre.tunescout.feature.songoptions.presentation.model

import androidx.annotation.StringRes

sealed interface SongOptionsUiAction {
    data class ShowSnackBar(
        @StringRes val message: Int,
    ) : SongOptionsUiAction
}
