package com.pierre.tunescout.core.playback.internal

import com.pierre.tunescout.core.model.QueueEntry

internal data class QueueTimeline(
    val entries: List<QueueEntry>,
    val startIndex: Int,
)
