package com.pierre.tunescout.core.playback

/**
 * What came of a screen asking for a song. The three answers are the same everywhere a row starts
 * playback; what a screen does with each one is its own — where it takes the user, and how it says
 * that nothing happened.
 */
enum class SongPlayOutcome {
    /** The player was already on that song, so nothing was restarted. */
    AlreadyPlaying,

    /** The player cannot reach the song right now, so nothing was started. */
    Unavailable,

    /** The player left what it was on and started the song. */
    Started,
}
