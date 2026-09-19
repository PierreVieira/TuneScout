package com.pierre.tunescout.core.model

data class QueueEntry(
    val id: String,
    val song: Song,
    val source: QueueSource,
)
