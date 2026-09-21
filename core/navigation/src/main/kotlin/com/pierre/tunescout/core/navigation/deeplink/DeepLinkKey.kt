package com.pierre.tunescout.core.navigation.deeplink

import androidx.navigation3.runtime.NavKey

/**
 * A route something outside the app can open directly.
 *
 * A deep link jumps straight to a screen, so the back stack it lands on is built by walking
 * [parent] up to the root — the synthetic back stack of the
 * [Navigation 3 deep link guide](https://github.com/android/nav3-recipes/blob/main/docs/deeplink-guide.md).
 * Back then leads where it would have led if the user had navigated there by hand, instead of
 * dropping them out of the app.
 *
 * Its [parent] is the route the user would most likely have seen before this one.
 */
interface DeepLinkKey : NavKey {
    val parent: NavKey
}
