package com.pierre.tunescout.core.audiosearch

/** The writing half of the audio search: whoever listened hands over what it heard. */
fun interface AudioSearchQueryPublisher {
    fun publish(query: String)
}
