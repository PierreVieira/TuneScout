package com.pierre.tunescout.feature.widget.presentation.widget

import androidx.compose.runtime.Composable
import com.pierre.tunescout.feature.widget.presentation.content.NowPlayingShortcutsWidgetContent

internal class NowPlayingShortcutsWidget : NowPlayingGlanceWidget() {
    @Composable
    override fun NowPlayingContent(content: WidgetContent) {
        NowPlayingShortcutsWidgetContent(content = content)
    }
}
