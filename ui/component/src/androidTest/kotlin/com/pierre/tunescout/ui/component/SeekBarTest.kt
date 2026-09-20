package com.pierre.tunescout.ui.component

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performTouchInput
import com.google.common.truth.Truth.assertThat
import de.mannodermaus.junit5.compose.createComposeExtension
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

@OptIn(ExperimentalTestApi::class)
class SeekBarTest {
    @JvmField
    @RegisterExtension
    val compose = createComposeExtension()

    private val seeks = mutableListOf<Float>()

    @Test
    fun releasingTheHandleSeeksToWhereTheDragEnded() = compose.use {
        setContent {
            SeekBar(progress = 0f, contentKey = FIRST_CONTENT_KEY, onSeekFinished = seeks::add)
        }

        onNodeWithContentDescription(DESCRIPTION).performTouchInput {
            down(centerLeft)
            moveTo(center)
            up()
        }

        assertThat(seeks).hasSize(1)
        assertThat(seeks.single()).isWithin(HALF_TOLERANCE).of(0.5f)
    }

    @Test
    fun givenADragInProgressWhenTheContentChangesReleasingSeeksNothing() = compose.use {
        var contentKey by mutableStateOf(FIRST_CONTENT_KEY)
        setContent {
            SeekBar(progress = 0f, contentKey = contentKey, onSeekFinished = seeks::add)
        }

        onNodeWithContentDescription(DESCRIPTION).performTouchInput {
            down(centerLeft)
            moveTo(center)
        }
        contentKey = SECOND_CONTENT_KEY
        onNodeWithContentDescription(DESCRIPTION).performTouchInput {
            moveTo(centerLeft)
            up()
        }

        assertThat(seeks).isEmpty()
    }

    private companion object {
        const val DESCRIPTION = "Playback position"
        const val FIRST_CONTENT_KEY = 1L
        const val SECOND_CONTENT_KEY = 2L
        const val HALF_TOLERANCE = 0.1f
    }
}
