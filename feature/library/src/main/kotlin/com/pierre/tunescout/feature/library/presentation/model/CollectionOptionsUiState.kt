package com.pierre.tunescout.feature.library.presentation.model

import com.pierre.tunescout.core.model.Song

/**
 * @property title what the collection is called, once it is known.
 * @property songs the songs it holds.
 * @property isDeletable whether the collection itself can be deleted — a playlist can, the liked
 * songs cannot.
 * @property isReorderable whether the collection can be put in an order of the user's own — a
 * playlist can, the liked songs cannot.
 * @property isConfirmingDelete whether the sheet is asking the user to confirm deleting it.
 */
data class CollectionOptionsUiState(
    val title: CollectionTitle?,
    val songs: List<Song>,
    val isDeletable: Boolean,
    val isReorderable: Boolean,
    val isConfirmingDelete: Boolean,
)
