package com.pierre.tunescout.presentation.model

sealed interface MainUiAction {
    data object RequestNotificationPermission : MainUiAction
}
