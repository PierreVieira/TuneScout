package com.pierre.tunescout.core.navigation.deeplink

import androidx.navigation3.runtime.NavKey
import com.pierre.tunescout.core.navigation.route.PlayerRoute

/**
 * Reads a [DeepLinkUrls] URL back into the route it names.
 *
 * The URL is taken apart as a string rather than as an `android.net.Uri`: the shapes are the app's
 * own and this way the matcher — the piece that decides where a tap lands — is covered by plain
 * JVM tests.
 */
class TuneScoutDeepLinkMatcher : DeepLinkMatcher {
    override fun findRouteOrNull(url: String?): NavKey? {
        val segments = getPathSegments(url)
        return when (segments.firstOrNull()) {
            DeepLinkUrls.PLAYER_HOST -> segments.getOrNull(1)?.toLongOrNull()?.let(::PlayerRoute)
            else -> null
        }
    }

    /**
     * @return what [url] holds after the app's scheme, or nothing at all when it is not one of
     * the app's URLs.
     */
    private fun getPathSegments(url: String?): List<String> {
        val prefix = "${DeepLinkUrls.SCHEME}://"
        if (url == null || !url.startsWith(prefix)) return emptyList()
        return url.removePrefix(prefix).split("/").filter(String::isNotEmpty)
    }
}
