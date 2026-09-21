package com.pierre.tunescout.ui.utils.semantics

import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.paneTitle
import androidx.compose.ui.semantics.semantics

/**
 * Marks the root of a full screen as a pane named [title], so a screen reader says where the user
 * landed. Screens change with a fade and nothing else announces it: without a pane, focus is simply
 * somewhere new.
 *
 * A screen still loading its title passes none and stays unnamed, rather than being announced twice.
 * Sheets and dialogs do not need this — their window is already a pane of its own.
 *
 * @return this modifier, followed by the pane's semantics when there is a [title] to give it.
 */
fun Modifier.screenPane(title: String?): Modifier = if (title.isNullOrEmpty()) this else semantics { paneTitle = title }
