package com.pierre.tunescout.core.audiosearch

import kotlinx.coroutines.flow.Flow

/**
 * The reading half of the audio search: the screen that opened the sheet learns what was said
 * without knowing who listened.
 */
fun interface ObservableAudioSearchQueries {
    /**
     * @return every query spoken from the moment collection starts. Nothing is replayed, so a
     * query is searched once, by whoever was on screen when it was said.
     */
    fun observeQueries(): Flow<String>
}
