package com.quare.tunescout

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.quare.tunescout.core.designsystem.theme.TuneScoutTheme
import com.quare.tunescout.navigation.TuneScoutNavDisplay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            TuneScoutTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    TuneScoutNavDisplay()
                }
            }
        }
    }
}
