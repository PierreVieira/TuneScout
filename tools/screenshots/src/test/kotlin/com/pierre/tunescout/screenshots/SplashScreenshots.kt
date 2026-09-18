package com.pierre.tunescout.screenshots

import com.pierre.tunescout.feature.splash.presentation.content.SplashContent
import org.junit.Test

internal class SplashScreenshots : ReadmeScreenshotsTest() {
    @Test
    fun splash() {
        capture(
            fileName = "splash",
            title = "Straight into the music",
            description = "The system splash continues into the app, so a cold start shows one screen",
        ) {
            SplashContent()
        }
    }
}
