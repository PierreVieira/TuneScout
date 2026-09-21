package com.pierre.tunescout.feature.album.presentation.model

import androidx.annotation.StringRes

sealed interface AlbumOptionsUiAction {
    data class ShowSnackBar(
        @StringRes val message: Int,
    ) : AlbumOptionsUiAction
}
