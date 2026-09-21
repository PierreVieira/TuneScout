package com.pierre.tunescout.core.playback

import com.pierre.tunescout.core.model.Song

/**
 * Which songs the player can reach right now: every one while the device is online, and only those
 * whose whole preview is on the device while it is not. A screen asks before handing songs to the
 * player, so a tap it cannot honour is refused with a message instead of leaving the player stuck
 * on a preview it will never load.
 */
fun interface PlayableSongs {
    fun isPlayable(song: Song): Boolean

    /**
     * A queue built offline keeps only what can play, so it does not stall on the next track.
     *
     * @return the songs of [songs] that [isPlayable], in the same order.
     */
    fun filterPlayable(songs: List<Song>): List<Song> = songs.filter(::isPlayable)

    /**
     * The whole of a list handed to the player at once — a "play now" — is worth nothing when the
     * player can reach none of it, and the screen says so instead of queuing silence.
     *
     * @return the songs of [songs] that [isPlayable], or null when that is none of them.
     */
    fun findPlayableOrNull(songs: List<Song>): List<Song>? = filterPlayable(songs).ifEmpty { null }

    /**
     * A list draws the songs it cannot play dimmer, so the ones a tap would refuse are told apart
     * before the tap.
     *
     * @return the ids of the songs of [songs] that are not [isPlayable].
     */
    fun findUnplayableIds(songs: List<Song>): Set<Long> = songs.filterNot(::isPlayable).mapTo(mutableSetOf(), Song::id)
}
