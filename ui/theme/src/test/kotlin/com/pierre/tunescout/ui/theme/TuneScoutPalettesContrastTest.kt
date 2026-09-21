package com.pierre.tunescout.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.luminance
import com.google.common.truth.Truth.assertWithMessage
import org.junit.jupiter.api.Test

/**
 * WCAG 1.4.3 asks 4.5:1 of body text. The tokens below are the ones closest to that line, and each
 * is measured against every surface the app draws it on — a value that reads on the background can
 * still fail on a sheet, which is how the first palette slipped.
 */
class TuneScoutPalettesContrastTest {
    @Test
    fun `GIVEN the light palette WHEN measuring its text THEN reads on every surface it is drawn on`() {
        // When
        val ratios = lightColorPalette.getTextContrastRatios()

        // Then
        ratios.forEach { (pair, ratio) -> assertWithMessage(pair).that(ratio).isAtLeast(MIN_TEXT_CONTRAST) }
    }

    @Test
    fun `GIVEN the dark palette WHEN measuring its text THEN reads on every surface it is drawn on`() {
        // When
        val ratios = darkColorPalette.getTextContrastRatios()

        // Then
        ratios.forEach { (pair, ratio) -> assertWithMessage(pair).that(ratio).isAtLeast(MIN_TEXT_CONTRAST) }
    }

    /**
     * Muted text and the accent of a playing song's title sit on the background, on a sheet and on
     * a subtle surface over the background (the mini player, the notice bar). The error colour
     * labels a dialog's confirm button, on a sheet. A placeholder is only ever inside a field, which
     * is a subtle surface over the background or over a sheet.
     *
     * @return the contrast of each of those pairs, by a name that says which one failed.
     */
    private fun TuneScoutColorPalette.getTextContrastRatios(): Map<String, Double> {
        val subtleOverBackground = surfaceSubtle.compositeOver(background)
        val subtleOverSheet = surfaceSubtle.compositeOver(sheet)
        val everywhere = mapOf(
            "background" to background,
            "sheet" to sheet,
            "subtle over background" to subtleOverBackground,
        )
        val fields = mapOf("subtle over background" to subtleOverBackground, "subtle over sheet" to subtleOverSheet)
        val surfacesByText = mapOf(
            ("textSecondary" to textSecondary) to everywhere,
            ("textTertiary" to textTertiary) to everywhere,
            ("accent" to accent) to everywhere,
            ("error" to error) to mapOf("background" to background, "sheet" to sheet),
            ("textPlaceholder" to textPlaceholder) to fields,
        )
        return surfacesByText
            .flatMap { (text, surfaces) ->
                surfaces.map { (surfaceName, surface) ->
                    "${text.first} on $surfaceName" to getContrastRatio(foreground = text.second, background = surface)
                }
            }.toMap()
    }

    private fun getContrastRatio(
        foreground: Color,
        background: Color,
    ): Double {
        val opaqueForeground = foreground.compositeOver(background)
        val lighter = maxOf(opaqueForeground.luminance(), background.luminance())
        val darker = minOf(opaqueForeground.luminance(), background.luminance())
        return (lighter + LUMINANCE_OFFSET) / (darker + LUMINANCE_OFFSET)
    }

    private companion object {
        const val MIN_TEXT_CONTRAST = 4.5
        const val LUMINANCE_OFFSET = 0.05
    }
}
