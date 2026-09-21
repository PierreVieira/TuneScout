package com.pierre.tunescout.screenshotfixtures

import androidx.compose.runtime.Composable
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.pierre.tunescout.core.model.Song
import kotlinx.coroutines.flow.flowOf

@Composable
fun pagingItems(songs: List<Song>): LazyPagingItems<Song> = flowOf(PagingData.from(songs)).collectAsLazyPagingItems()

@Composable
fun emptyPagingItems(): LazyPagingItems<Song> = pagingItems(emptyList())
