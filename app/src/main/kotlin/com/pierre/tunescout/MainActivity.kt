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
    private val transparentScrim = Color.Transparent.toArgb()
    private val navigationBarLightScrim = Color(color = 0xE6FFFFFF).toArgb()
    private val navigationBarDarkScrim = Color(color = 0x801B1B1B).toArgb()

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
            enableEdgeToEdge(
                statusBarStyle = getStatusBarStyle(isDark),
                navigationBarStyle = getNavigationBarStyle(isDark),
            )
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

    private fun getStatusBarStyle(isDark: Boolean): SystemBarStyle = if (isDark) {
        SystemBarStyle.dark(transparentScrim)
    } else {
        SystemBarStyle.light(transparentScrim, transparentScrim)
    }

    private fun getNavigationBarStyle(isDark: Boolean): SystemBarStyle = if (isDark) {
        SystemBarStyle.dark(navigationBarDarkScrim)
    } else {
        SystemBarStyle.light(navigationBarLightScrim, navigationBarDarkScrim)
    }
}
