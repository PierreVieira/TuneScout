package com.quare.tunescout.feature.splash.presentation.content

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.quare.tunescout.feature.splash.R
import com.quare.tunescout.ui.theme.TuneScoutColors

private val noteSize = 100.dp
private const val GRADIENT_START_STOP = 0.34f

@Composable
fun SplashContent(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .drawWithCache {
                val brush = Brush.linearGradient(
                    colorStops = arrayOf(
                        GRADIENT_START_STOP to TuneScoutColors.background,
                        1f to TuneScoutColors.splashGradientEnd,
                    ),
                    start = Offset(0f, size.height),
                    end = Offset(size.width, 0f),
                )
                onDrawBehind { drawRect(brush) }
            },
        contentAlignment = Alignment.Center,
    ) {
        Image(
            painter = painterResource(R.drawable.splash_note),
            contentDescription = null,
            modifier = Modifier.size(noteSize),
        )
    }
}
