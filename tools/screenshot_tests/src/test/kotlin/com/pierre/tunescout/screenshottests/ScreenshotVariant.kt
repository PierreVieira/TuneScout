package com.pierre.tunescout.screenshottests

import com.pierre.tunescout.ui.theme.Theme

/**
 * One way of rendering a screen. Every screen is captured in [LIGHT] and [DARK]; the main ones
 * also in [LARGE_FONT_PT_BR], where Portuguese copy (longer than English) meets a 1.5x font scale —
 * the combination that truncates or wraps first — and in [LARGEST_FONT], the 2x the system's font
 * size setting goes up to, which is where a fixed height clips and a row stops fitting its controls.
 *
 * @property theme pinned rather than [Theme.SYSTEM], since Robolectric reports a light system theme.
 * @property locale the Robolectric qualifier for the language, or null for the default English. A
 * variant rendered after one that set a language names its own: qualifiers are added to the ones
 * in force, so null would keep the previous variant's.
 * @property fontScale how far the system font is scaled up, 1 being the device's default.
 * @property fileSuffix what ends the image's file name, so each variant has its own reference.
 */
internal enum class ScreenshotVariant(
    val theme: Theme,
    val locale: String?,
    val fontScale: Float,
    val fileSuffix: String,
) {
    LIGHT(theme = Theme.LIGHT, locale = null, fontScale = 1f, fileSuffix = "light"),
    DARK(theme = Theme.DARK, locale = null, fontScale = 1f, fileSuffix = "dark"),
    LARGE_FONT_PT_BR(
        theme = Theme.DARK,
        locale = "pt-rBR",
        fontScale = 1.5f,
        fileSuffix = "pt-BR_large-font",
    ),
    LARGEST_FONT(theme = Theme.LIGHT, locale = "en-rUS", fontScale = 2f, fileSuffix = "largest-font"),
    ;

    companion object {
        val themes: List<ScreenshotVariant> = listOf(LIGHT, DARK)
        val all: List<ScreenshotVariant> = entries
    }
}
