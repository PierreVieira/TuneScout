package com.pierre.tunescout.feature.audiosearch.data.query

import com.pierre.tunescout.core.audiosearch.AudioSearchQueryPublisher
import com.pierre.tunescout.core.audiosearch.ObservableAudioSearchQueries
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow

/**
 * Carries a spoken query from the sheet to the screen that opened it. The two never meet: a
 * feature may not depend on a feature, so each one sees only its half of this, through
 * `:core:audio_search`.
 */
internal class AudioSearchQueryBus :
    ObservableAudioSearchQueries,
    AudioSearchQueryPublisher {
    /**
     * Buffered so [publish] stays non-suspending, and without replay: a screen that opens later
     * must not search for something said before it existed.
     */
    private val queries = MutableSharedFlow<String>(extraBufferCapacity = 1)

    override fun observeQueries(): Flow<String> = queries

    override fun publish(query: String) {
        queries.tryEmit(query)
    }
}
