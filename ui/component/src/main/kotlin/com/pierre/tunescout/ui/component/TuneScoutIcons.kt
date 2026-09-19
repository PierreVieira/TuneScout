package com.pierre.tunescout.ui.component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.PlaylistAdd
import androidx.compose.material.icons.automirrored.rounded.PlaylistPlay
import androidx.compose.material.icons.automirrored.rounded.QueueMusic
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DragHandle
import androidx.compose.material.icons.rounded.LibraryMusic
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Repeat
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.ui.graphics.vector.ImageVector

object TuneScoutIcons {
    val search: ImageVector get() = Icons.Rounded.Search

    val clear: ImageVector get() = Icons.Rounded.Close

    val moreMenu: ImageVector get() = Icons.Rounded.MoreVert

    val arrowBack: ImageVector get() = Icons.AutoMirrored.Rounded.ArrowBack

    val play: ImageVector get() = Icons.Rounded.PlayArrow

    val pause: ImageVector get() = Icons.Rounded.Pause

    val skipPrevious: ImageVector get() = Icons.Rounded.SkipPrevious

    val skipNext: ImageVector get() = Icons.Rounded.SkipNext

    val repeat: ImageVector get() = Icons.Rounded.Repeat

    val musicList: ImageVector get() = Icons.AutoMirrored.Rounded.QueueMusic

    val viewAlbum: ImageVector get() = Icons.Rounded.LibraryMusic

    val queueNext: ImageVector get() = Icons.AutoMirrored.Rounded.PlaylistPlay

    val addToQueue: ImageVector get() = Icons.AutoMirrored.Rounded.PlaylistAdd

    val removeFromQueue: ImageVector get() = Icons.Rounded.Close

    val dragHandle: ImageVector get() = Icons.Rounded.DragHandle
}
