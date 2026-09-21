package com.pierre.tunescout.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.dp
import androidx.test.platform.app.InstrumentationRegistry
import coil3.ColorImage
import coil3.annotation.ExperimentalCoilApi
import coil3.compose.AsyncImagePainter
import coil3.compose.AsyncImagePreviewHandler
import coil3.compose.LocalAsyncImagePreviewHandler
import coil3.request.ErrorResult
import coil3.request.ImageRequest
import coil3.request.SuccessResult
import com.google.common.truth.Truth.assertThat
import com.pierre.tunescout.ui.theme.TuneScoutColors
import com.pierre.tunescout.ui.utils.network.LocalIsOffline
import de.mannodermaus.junit5.compose.createComposeExtension
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

@OptIn(ExperimentalTestApi::class, ExperimentalCoilApi::class)
class ArtworkTest {
    @JvmField
    @RegisterExtension
    val compose = createComposeExtension()

    private val artworkSize = 64.dp
    private val artworkCornerPercent = 15
    private val offlineDescription: String = InstrumentationRegistry
        .getInstrumentation()
        .targetContext
        .getString(R.string.ui_artwork_unavailable_offline)

    @Test
    fun whileTheImageLoadsTheBoxShimmersWithoutThePlaceholderIcon() = compose.use {
        setContent { Content(state = { AsyncImagePainter.State.Loading(painter = null) }) }

        val artwork = onNodeWithTag(ARTWORK_TAG).captureToImage()

        assertThat(getBrightPixelCount(artwork)).isEqualTo(0)
    }

    @Test
    fun whenTheImageFailsTheBoxFallsBackToThePlaceholderIcon() = compose.use {
        setContent {
            Content(state = { request -> AsyncImagePainter.State.Error(null, errorResult(request)) })
        }

        val artwork = onNodeWithTag(ARTWORK_TAG).captureToImage()

        assertThat(getBrightPixelCount(artwork)).isGreaterThan(0)
    }

    @Test
    fun givenNoUrlTheBoxShowsThePlaceholderIconWithoutShimmering() = compose.use {
        setContent {
            Content(url = "", state = { request -> AsyncImagePainter.State.Error(null, errorResult(request)) })
        }

        val artwork = onNodeWithTag(ARTWORK_TAG).captureToImage()

        assertThat(getBrightPixelCount(artwork)).isGreaterThan(0)
    }

    @Test
    fun whenTheImageLoadsItCoversTheBox() = compose.use {
        setContent {
            Content(
                state = { request ->
                    AsyncImagePainter.State.Success(ColorPainter(Color.Magenta), successResult(request))
                },
            )
        }

        val artwork = onNodeWithTag(ARTWORK_TAG).captureToImage()

        val pixels = artwork.toPixelMap()
        assertThat(pixels[pixels.width / 2, pixels.height / 2]).isEqualTo(Color.Magenta)
    }

    @Test
    fun givenASharedKeyButNoSharedTransitionTheBoxStillDrawsTheImage() = compose.use {
        setContent {
            Content(
                sharedKey = SongSharedKey(songId = 1, element = SongSharedElement.ARTWORK),
                state = { request ->
                    AsyncImagePainter.State.Success(ColorPainter(Color.Magenta), successResult(request))
                },
            )
        }

        val artwork = onNodeWithTag(ARTWORK_TAG).captureToImage()

        val pixels = artwork.toPixelMap()
        assertThat(pixels[pixels.width / 2, pixels.height / 2]).isEqualTo(Color.Magenta)
    }

    @Test
    fun whenTheImageFailsWhileOfflineTheBoxSaysTheArtworkIsOneConnectionAway() = compose.use {
        setContent {
            Content(
                isOffline = true,
                state = { request -> AsyncImagePainter.State.Error(null, errorResult(request)) },
            )
        }

        onNodeWithContentDescription(offlineDescription).assertIsDisplayed()
    }

    @Test
    fun whenTheImageFailsWhileOnlineTheBoxKeepsThePlaceholderIconSilent() = compose.use {
        setContent {
            Content(state = { request -> AsyncImagePainter.State.Error(null, errorResult(request)) })
        }

        onNodeWithContentDescription(offlineDescription).assertDoesNotExist()
    }

    @Test
    fun givenNoUrlWhileOfflineTheBoxKeepsThePlaceholderIconSilent() = compose.use {
        setContent {
            Content(
                url = "",
                isOffline = true,
                state = { request -> AsyncImagePainter.State.Error(null, errorResult(request)) },
            )
        }

        onNodeWithContentDescription(offlineDescription).assertDoesNotExist()
    }

    @Composable
    private fun Content(
        url: String = ARTWORK_URL,
        sharedKey: SongSharedKey? = null,
        isOffline: Boolean = false,
        state: (ImageRequest) -> AsyncImagePainter.State,
    ) {
        CompositionLocalProvider(
            LocalInspectionMode provides true,
            LocalIsOffline provides isOffline,
            LocalAsyncImagePreviewHandler provides AsyncImagePreviewHandler { _, request -> state(request) },
        ) {
            Box(modifier = Modifier.background(TuneScoutColors.background)) {
                Artwork(
                    url = url,
                    contentDescription = null,
                    cornerPercent = artworkCornerPercent,
                    sharedKey = sharedKey,
                    modifier = Modifier
                        .size(artworkSize)
                        .testTag(ARTWORK_TAG),
                )
            }
        }
    }

    private fun errorResult(request: ImageRequest): ErrorResult =
        ErrorResult(image = null, request = request, throwable = IllegalStateException("no artwork"))

    private fun successResult(request: ImageRequest): SuccessResult =
        SuccessResult(image = ColorImage(Color.Magenta.toArgb()), request = request)

    private fun getBrightPixelCount(image: ImageBitmap): Int {
        val pixels = image.toPixelMap()
        var count = 0
        for (y in 0 until pixels.height) {
            for (x in 0 until pixels.width) {
                val color = pixels[x, y]
                val isBright = color.red > PLACEHOLDER_MIN_CHANNEL &&
                    color.green > PLACEHOLDER_MIN_CHANNEL &&
                    color.blue > PLACEHOLDER_MIN_CHANNEL
                if (isBright) count++
            }
        }
        return count
    }

    private companion object {
        const val ARTWORK_TAG = "artwork"
        const val ARTWORK_URL = "https://example.com/art/1/100x100bb.jpg"
        const val PLACEHOLDER_MIN_CHANNEL = 0.55f
    }
}
