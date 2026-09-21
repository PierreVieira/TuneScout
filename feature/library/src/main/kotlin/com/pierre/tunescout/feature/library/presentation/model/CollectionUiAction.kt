package com.pierre.tunescout.feature.library.presentation.model

import androidx.annotation.StringRes

sealed interface CollectionUiAction {
    data class ShowSnackBar(
        @StringRes val message: Int,
    ) : CollectionUiAction
}
