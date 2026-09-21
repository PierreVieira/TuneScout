package com.pierre.tunescout.screenshots

import android.view.View
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import org.junit.Test
import com.pierre.tunescout.ui.theme.R as ThemeR

private val noteSize = 100.dp

internal class SplashScreenshots : ReadmeScreenshotsTest() {
    @Test
    fun splash() {
        capture(
            fileName = "splash",
            title = "Straight into the music",
            description = "One splash, the system's own, that leaves through the gradient of the design",
        ) {
            SplashBox()
        }
    }
}

/**
 * The splash is a system window and an exit animation, not a composable, so this is the one shot
 * assembled here: the two drawables the app shows, at the moment the gradient is fully in.
 */
@Composable
private fun SplashBox() {
    Box(contentAlignment = Alignment.Center) {
        AndroidView(
            factory = { context -> View(context).apply { setBackgroundResource(ThemeR.drawable.splash_gradient) } },
            modifier = Modifier.fillMaxSize(),
        )
        Image(
            painter = painterResource(ThemeR.drawable.splash_note),
            contentDescription = null,
            modifier = Modifier.size(noteSize),
        )
    }
}
