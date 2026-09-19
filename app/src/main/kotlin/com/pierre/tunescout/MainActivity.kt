package com.pierre.tunescout

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.splashscreen.SplashScreenViewProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pierre.tunescout.presentation.content.MainContent
import com.pierre.tunescout.presentation.model.MainUiState
import com.pierre.tunescout.presentation.viewmodel.MainViewModel
import com.pierre.tunescout.ui.theme.TuneScoutTheme
import com.pierre.tunescout.ui.theme.isDark
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen().apply {
            setKeepOnScreenCondition { viewModel.uiState.value is MainUiState.Loading }
            setOnExitAnimationListener(SplashScreenViewProvider::remove)
        }
        super.onCreate(savedInstanceState)
        setContent {
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            when (val state = uiState) {
                MainUiState.Loading -> Unit
                is MainUiState.Ready -> ThemedContent(state)
            }
        }
    }

    @Composable
    private fun ThemedContent(state: MainUiState.Ready) {
        val isDark = state.theme.isDark()
        LaunchedEffect(isDark) {
            enableEdgeToEdge(statusBarStyle = getSystemBarStyle(isDark))
        }
        TuneScoutTheme(
            theme = state.theme,
            isDynamicColorEnabled = state.isDynamicColorEnabled,
        ) {
            MainContent(
                requestNotificationPermissionsUiAction = viewModel.requestNotificationPermissionsUiAction,
            )
        }
    }

    private fun getSystemBarStyle(isDark: Boolean): SystemBarStyle {
        val transparent = Color.Transparent.toArgb()
        return if (isDark) {
            SystemBarStyle.dark(transparent)
        } else {
            SystemBarStyle.light(transparent, transparent)
        }
    }
}
