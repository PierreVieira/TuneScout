package com.pierre.tunescout.ui.component

import android.text.InputType
import android.view.inputmethod.EditorInfo
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.platform.InterceptPlatformTextInput
import kotlinx.coroutines.awaitCancellation

/**
 * Stands in for the platform keyboard so a test can read the [EditorInfo] a focused field hands it.
 * A field's capitalization never reaches the semantics tree, so this is the only place it shows.
 */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun CapturingEditorInfo(
    onEditorInfo: (EditorInfo) -> Unit,
    content: @Composable () -> Unit,
) {
    InterceptPlatformTextInput(
        interceptor = { request, _ ->
            onEditorInfo(EditorInfo().also(request::createInputConnection))
            awaitCancellation()
        },
        content = content,
    )
}

val EditorInfo.capitalizesSentences: Boolean
    get() = inputType and InputType.TYPE_TEXT_FLAG_CAP_SENTENCES != 0
