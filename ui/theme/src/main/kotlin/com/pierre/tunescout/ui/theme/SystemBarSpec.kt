package com.pierre.tunescout.ui.theme

import androidx.activity.SystemBarStyle
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb

/**
 * One system bar, described with the arguments [SystemBarStyle] is built from. It is kept as plain
 * data because [SystemBarStyle] has no `equals` and hides its fields, so it could not be checked in
 * a test. The style is built from that data on read, and being a getter it stays out of the
 * generated `equals`.
 *
 * There is no case for `SystemBarStyle.auto`: besides reading the device's night mode, it drops the
 * scrims and turns on the platform's navigation bar contrast from API 29 — see `docs/architecture/theming.md`.
 */
sealed interface SystemBarSpec {
    /** What `enableEdgeToEdge` is handed for this bar. */
    val style: SystemBarStyle

    /**
     * Dark icons over a light bar.
     *
     * @property scrim the bar's background.
     * @property darkScrim the background used instead on devices that cannot draw dark icons.
     */
    data class Light(
        val scrim: Color,
        val darkScrim: Color,
    ) : SystemBarSpec {
        override val style: SystemBarStyle
            get() = SystemBarStyle.light(scrim.toArgb(), darkScrim.toArgb())
    }

    /**
     * Light icons over a dark bar.
     *
     * @property scrim the bar's background.
     */
    data class Dark(
        val scrim: Color,
    ) : SystemBarSpec {
        override val style: SystemBarStyle
            get() = SystemBarStyle.dark(scrim.toArgb())
    }
}
