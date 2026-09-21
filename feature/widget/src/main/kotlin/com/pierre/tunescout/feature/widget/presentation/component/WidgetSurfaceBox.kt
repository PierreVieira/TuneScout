package com.pierre.tunescout.feature.widget.presentation.component

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.glance.GlanceModifier
import androidx.glance.LocalContext
import androidx.glance.action.clickable
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.cornerRadius
import androidx.glance.background
import androidx.glance.layout.Box
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import com.pierre.tunescout.core.navigation.deeplink.DeepLinkUrls

private val surfaceCornerRadius = 20.dp
private val surfacePadding = 12.dp

/**
 * The rounded panel both widgets sit on, and the tap that opens the app on the song it shows.
 *
 * @param songId the song the player opens on, or `null` when nothing has played yet — the widget
 * then just opens the app.
 */
@Composable
internal fun WidgetSurfaceBox(
    songId: Long?,
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(WidgetColors.background)
            .cornerRadius(surfaceCornerRadius)
            .padding(surfacePadding)
            .clickable(actionStartActivity(createOpenIntent(context = context, songId = songId))),
    ) {
        content()
    }
}

/**
 * The intent is kept inside the app by its package: the scheme is the app's own and carries no
 * `BROWSABLE` category, so nothing else answers it either way.
 *
 * @return the intent that opens the player on [songId], or the app's launcher screen without one.
 */
private fun createOpenIntent(
    context: Context,
    songId: Long?,
): Intent {
    val launchIntent = context.packageManager.getLaunchIntentForPackage(context.packageName) ?: Intent()
    if (songId == null) return launchIntent
    return Intent(Intent.ACTION_VIEW, DeepLinkUrls.createPlayerUrl(songId).toUri())
        .setPackage(context.packageName)
}
