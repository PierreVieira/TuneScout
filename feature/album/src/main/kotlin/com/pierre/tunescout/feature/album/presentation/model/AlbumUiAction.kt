package com.pierre.tunescout.feature.album.presentation.model

import androidx.annotation.StringRes

sealed interface AlbumUiAction {
    data class ShowSnackBar(
        @StringRes val message: Int,
    ) : AlbumUiAction
}
