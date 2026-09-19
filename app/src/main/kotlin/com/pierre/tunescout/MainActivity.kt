package com.pierre.tunescout

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.splashscreen.SplashScreenViewProvider
import com.pierre.tunescout.presentation.content.MainContent
import com.pierre.tunescout.ui.theme.TuneScoutTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen().setOnExitAnimationListener(SplashScreenViewProvider::remove)
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            TuneScoutTheme {
                MainContent()
            }
        }
    }
}
