package com.pierre.tunescout.core.navigation.deeplink

/**
 * The URL space the app answers to. Whoever opens a deep link builds its URL here, and
 * [TuneScoutDeepLinkMatcher] reads the same URLs back into routes, so the two never drift apart.
 *
 * The scheme is the app's own and carries no `BROWSABLE` category: these links are for the home
 * screen widgets and the notification, not for the browser.
 */
object DeepLinkUrls {
    const val SCHEME: String = "tunescout"
    const val PLAYER_HOST: String = "player"

    /**
     * @return the URL that opens the player on the song [songId] belongs to.
     */
    fun createPlayerUrl(songId: Long): String = "$SCHEME://$PLAYER_HOST/$songId"
}
