package com.pierre.tunescout.feature.library.presentation.model

import androidx.annotation.StringRes

sealed interface CollectionOptionsUiAction {
    data class ShowSnackBar(
        @StringRes val message: Int,
    ) : CollectionOptionsUiAction
}
