package com.pierre.tunescout.feature.widget.presentation.component

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.glance.color.DayNightColorProvider
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test
import org.w3c.dom.Element
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory

/**
 * The picker's preview layouts cannot read the Compose palette, so they draw with a copy of it in
 * `res/values/colors.xml` and `res/values-night/colors.xml`. These tests are what keeps that copy
 * from drifting.
 */
class WidgetPreviewColorsTest {
    private val colorsByResourceName: Map<String, DayNightColorProvider> = mapOf(
        "widget_preview_background" to WidgetColors.background,
        "widget_preview_surface" to WidgetColors.surface,
        "widget_preview_text_primary" to WidgetColors.textPrimary,
        "widget_preview_text_secondary" to WidgetColors.textSecondary,
        "widget_preview_element" to WidgetColors.element,
        "widget_preview_element_disabled" to WidgetColors.elementDisabled,
        "widget_preview_accent" to WidgetColors.accent,
    ).mapValues { (_, provider) -> provider as DayNightColorProvider }

    @Test
    fun `GIVEN the day colors WHEN reading them THEN match the light palette`() {
        // Given
        val expected = colorsByResourceName.mapValues { (_, provider) -> provider.day.toHex() }

        // When
        val actual = readColors("values")

        // Then
        assertThat(actual).containsExactlyEntriesIn(expected)
    }

    @Test
    fun `GIVEN the night colors WHEN reading them THEN match the dark palette`() {
        // Given
        val expected = colorsByResourceName.mapValues { (_, provider) -> provider.night.toHex() }

        // When
        val actual = readColors("values-night")

        // Then
        assertThat(actual).containsExactlyEntriesIn(expected)
    }

    /**
     * Gradle runs a module's unit tests from the module's directory, so the path is relative to it.
     *
     * @return every `<color>` in [folder]'s `colors.xml`, by name, in `#AARRGGBB` form.
     */
    private fun readColors(folder: String): Map<String, String> {
        val document = DocumentBuilderFactory
            .newInstance()
            .newDocumentBuilder()
            .parse(File("src/main/res/$folder/colors.xml"))
        val nodes = document.getElementsByTagName("color")
        return (0 until nodes.length)
            .map { index -> nodes.item(index) as Element }
            .associate { element -> element.getAttribute("name") to element.textContent.trim().uppercase() }
    }

    private fun Color.toHex(): String = "#%08X".format(toArgb())
}
