package com.pierre.tunescout.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalContext

@Composable
fun TuneScoutTheme(
    theme: Theme = Theme.SYSTEM,
    isDynamicColorEnabled: Boolean = false,
    content: @Composable () -> Unit,
) {
    val isDark = theme.isDark()
    val dynamicColorScheme = dynamicColorSchemeOrNull(isDark = isDark, isDynamicColorEnabled = isDynamicColorEnabled)
    val palette = dynamicColorScheme?.toColorPalette() ?: getStaticColorPalette(isDark)
    CompositionLocalProvider(LocalTuneScoutColorPalette provides palette) {
        MaterialTheme(
            colorScheme = dynamicColorScheme ?: palette.toColorScheme(isDark),
            typography = tuneScoutTypography,
            content = content,
        )
    }
}

@Composable
fun Theme.isDark(): Boolean = when (this) {
    Theme.LIGHT -> false
    Theme.DARK -> true
    Theme.SYSTEM -> isSystemInDarkTheme()
}

/**
 * The palette a theme would render with, independent of the one in force. It is what draws a
 * preview of a theme the user has not picked yet.
 */
@Composable
fun colorPalette(
    isDark: Boolean,
    isDynamicColorEnabled: Boolean,
): TuneScoutColorPalette =
    dynamicColorSchemeOrNull(isDark = isDark, isDynamicColorEnabled = isDynamicColorEnabled)?.toColorPalette()
        ?: getStaticColorPalette(isDark)

@Composable
private fun dynamicColorSchemeOrNull(
    isDark: Boolean,
    isDynamicColorEnabled: Boolean,
): ColorScheme? = if (isDynamicColorEnabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
    val context = LocalContext.current
    if (isDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
} else {
    null
}

private fun getStaticColorPalette(isDark: Boolean): TuneScoutColorPalette =
    if (isDark) darkColorPalette else lightColorPalette
