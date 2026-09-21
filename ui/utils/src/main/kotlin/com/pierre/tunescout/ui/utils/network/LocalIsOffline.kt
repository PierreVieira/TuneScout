package com.pierre.tunescout.ui.utils.network

import androidx.compose.runtime.compositionLocalOf

/**
 * Whether the device is without a network right now, provided once at the root of the app.
 *
 * `:ui:*` cannot reach the network monitor, and a component this deep — the artwork of a song row —
 * has no ViewModel of its own to read it from, so the answer comes down the composition instead.
 * It defaults to online: a preview, or a screenshot test, draws what the user normally sees.
 */
val LocalIsOffline = compositionLocalOf { false }
