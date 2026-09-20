package com.pierre.tunescout.ui.component

import android.view.inputmethod.EditorInfo
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.requestFocus
import com.google.common.truth.Truth.assertThat
import de.mannodermaus.junit5.compose.createComposeExtension
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

@OptIn(ExperimentalTestApi::class)
class SearchFieldTest {
    @JvmField
    @RegisterExtension
    val compose = createComposeExtension()

    @Test
    fun theFieldAsksTheKeyboardToCapitalizeTheFirstLetter() = compose.use {
        var editorInfo: EditorInfo? = null
        setContent {
            CapturingEditorInfoBox(onEditorInfo = { editorInfo = it }) {
                SearchField(
                    query = "",
                    placeholder = PLACEHOLDER,
                    onQueryChange = {},
                    onClear = {},
                )
            }
        }

        onNode(hasSetTextAction()).requestFocus()

        waitUntil { editorInfo != null }
        assertThat(editorInfo?.capitalizesSentences).isTrue()
    }

    private companion object {
        const val PLACEHOLDER = "Songs, albums, artists"
    }
}
