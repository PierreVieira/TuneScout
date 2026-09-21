package com.pierre.tunescout.feature.player.presentation.model

import androidx.annotation.StringRes

sealed interface PlayerUiAction {
    data class ShowSnackBar(
        @StringRes val message: Int,
    ) : PlayerUiAction
}
