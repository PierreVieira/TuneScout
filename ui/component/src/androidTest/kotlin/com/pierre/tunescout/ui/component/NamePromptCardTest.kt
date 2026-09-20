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
class NamePromptCardTest {
    @JvmField
    @RegisterExtension
    val compose = createComposeExtension()

    @Test
    fun theNameFieldAsksTheKeyboardToCapitalizeTheFirstLetter() = compose.use {
        var editorInfo: EditorInfo? = null
        setContent {
            CapturingEditorInfoBox(onEditorInfo = { editorInfo = it }) {
                NamePromptCard(
                    title = "New playlist",
                    placeholder = "Playlist name",
                    confirmLabel = "Create",
                    cancelLabel = "Cancel",
                    name = "",
                    canConfirm = false,
                    onNameChange = {},
                    onConfirm = {},
                    onCancel = {},
                )
            }
        }

        onNode(hasSetTextAction()).requestFocus()

        waitUntil { editorInfo != null }
        assertThat(editorInfo?.capitalizesSentences).isTrue()
    }
}
