package com.pierre.tunescout.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * How both system bars are drawn over the edge-to-edge content.
 *
 * @property statusBar the bar at the top, always transparent over the app.
 * @property navigationBar the bar at the bottom.
 */
data class SystemBars(
    val statusBar: SystemBarSpec,
    val navigationBar: SystemBarSpec,
) {
    companion object {
        /**
         * @param isDark whether the app is drawn in the dark palette — [Theme.isDark], which also
         * resolves [Theme.SYSTEM], so the bars and the palette never disagree.
         * @return the bars that keep their icons readable over that palette.
         */
        fun of(isDark: Boolean): SystemBars {
            val navigationBarDarkScrim = Color(color = 0x801B1B1B)
            return if (isDark) {
                SystemBars(
                    statusBar = SystemBarSpec.Dark(scrim = Color.Transparent),
                    navigationBar = SystemBarSpec.Dark(scrim = navigationBarDarkScrim),
                )
            } else {
                SystemBars(
                    statusBar = SystemBarSpec.Light(scrim = Color.Transparent, darkScrim = Color.Transparent),
                    navigationBar = SystemBarSpec.Light(
                        scrim = Color(color = 0xE6FFFFFF),
                        darkScrim = navigationBarDarkScrim,
                    ),
                )
            }
        }
    }
}
