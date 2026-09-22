package com.pierre.tunescout.feature.queue.presentation.model

/**
 * What the queue says it is playing from. An album or a playlist is named by its own title, and the
 * lists the app names itself by a string resource, so it only resolves to text inside the
 * composable that draws it.
 */
sealed interface QueueContextTitle {
    data class Custom(
        val name: String,
    ) : QueueContextTitle

    data object LikedSongs : QueueContextTitle

    data object DownloadedSongs : QueueContextTitle

    data object RecentlyPlayed : QueueContextTitle
}
