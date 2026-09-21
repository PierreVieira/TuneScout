package com.pierre.tunescout.feature.widget.presentation.widget

import androidx.compose.runtime.Composable
import com.pierre.tunescout.feature.widget.presentation.content.NowPlayingWidgetContent

internal class NowPlayingWidget : NowPlayingGlanceWidget() {
    @Composable
    override fun NowPlayingContent(content: WidgetContent) {
        NowPlayingWidgetContent(content = content)
    }
}
