package com.pierre.tunescout.feature.library.presentation.model

data class CreatePlaylistUiState(
    val name: String,
) {
    val canConfirm: Boolean
        get() = name.isNotBlank()
}
