package com.pierre.tunescout.core.navigation.deeplink

import androidx.navigation3.runtime.NavKey

fun interface DeepLinkMatcher {
    /**
     * @return the route [url] names, or `null` when it names none — the caller then opens the app
     * where it normally starts.
     */
    fun findRouteOrNull(url: String?): NavKey?
}
