package com.pierre.tunescout.feature.themeselection.data.mapper

import com.pierre.tunescout.ui.theme.Theme

/**
 * The stored value is spelled out rather than taken from [Theme.name], so renaming an enum entry
 * cannot silently reset everyone's preference to the default.
 */
class ThemePreferenceMapperImpl : ThemePreferenceMapper {
    override fun map(preference: String?): Theme = when (preference) {
        LIGHT -> Theme.LIGHT
        DARK -> Theme.DARK
        else -> Theme.SYSTEM
    }

    override fun toPreference(theme: Theme): String = when (theme) {
        Theme.LIGHT -> LIGHT
        Theme.DARK -> DARK
        Theme.SYSTEM -> SYSTEM
    }

    private companion object {
        const val LIGHT = "light"
        const val DARK = "dark"
        const val SYSTEM = "system"
    }
}
