package com.pierre.tunescout.feature.widget.presentation.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.glance.GlanceId
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent
import com.pierre.tunescout.feature.widget.domain.usecase.ObserveWidgetState
import kotlinx.coroutines.flow.map
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * What both widgets share: one subscription to the playback state, the artwork that goes with it,
 * and the Glance session that redraws whenever either changes. Glance keeps this session running
 * while the widget is on the home screen, so the flow is the update mechanism — subclasses only
 * decide what is drawn.
 */
internal abstract class NowPlayingGlanceWidget :
    GlanceAppWidget(),
    KoinComponent {
    private val observeWidgetState: ObserveWidgetState by inject()
    private val artworkLoader: WidgetArtworkLoader by inject()
    private val previewContentFactory: WidgetPreviewContentFactory by inject()

    final override suspend fun provideGlance(
        context: Context,
        id: GlanceId,
    ) {
        val contents = observeWidgetState().map { state -> artworkLoader.loadContent(state) }
        provideContent {
            val content by contents.collectAsState(WidgetContent.Empty)
            NowPlayingContent(content = content)
        }
    }

    /**
     * What the widget picker draws before the widget is placed: the real layout, filled with the
     * sample [WidgetPreviewContentFactory] builds.
     */
    final override suspend fun providePreview(
        context: Context,
        widgetCategory: Int,
    ) {
        val content = previewContentFactory.createContent(context)
        provideContent { NowPlayingContent(content = content) }
    }

    @Composable
    protected abstract fun NowPlayingContent(content: WidgetContent)
}
