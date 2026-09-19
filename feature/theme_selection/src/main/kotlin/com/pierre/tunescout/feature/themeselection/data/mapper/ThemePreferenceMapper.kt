package com.pierre.tunescout.feature.themeselection.data.mapper

import com.pierre.tunescout.ui.theme.Theme

interface ThemePreferenceMapper {
    fun map(preference: String?): Theme

    fun toPreference(theme: Theme): String
}
