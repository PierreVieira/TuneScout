package com.pierre.tunescout.core.playback

import kotlinx.coroutines.flow.Flow

/**
 * [PlayableSongs] followed over time: a new answer arrives every time the player's reach can have
 * changed — the connection came or went — so a list already on screen redraws instead of keeping
 * the answer it was built with. A screen asks for it to dim the songs a tap would refuse, while
 * [PlayableSongs] alone answers the tap itself.
 */
fun interface ObservablePlayableSongs {
    fun observePlayableSongs(): Flow<PlayableSongs>
}
