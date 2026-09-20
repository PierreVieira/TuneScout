package com.pierre.tunescout.feature.library.presentation.model

import com.pierre.tunescout.core.model.Song

data class CollectionOptionsUiState(
    val title: CollectionTitle?,
    val songs: List<Song>,
    val isDeletable: Boolean,
)
