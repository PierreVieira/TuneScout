package com.pierre.tunescout.feature.queue.presentation.model

import androidx.annotation.StringRes

sealed interface QueueUiAction {
    data class ShowSnackBar(
        @StringRes val message: Int,
    ) : QueueUiAction
}
