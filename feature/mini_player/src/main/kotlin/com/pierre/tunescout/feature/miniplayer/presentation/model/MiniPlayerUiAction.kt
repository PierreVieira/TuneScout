package com.pierre.tunescout.feature.miniplayer.presentation.model

import androidx.annotation.StringRes

sealed interface MiniPlayerUiAction {
    data class ShowSnackBar(
        @StringRes val message: Int,
    ) : MiniPlayerUiAction
}
