package com.pierre.tunescout.screenshotfixtures

import androidx.compose.runtime.Composable
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import kotlinx.coroutines.flow.flowOf

/**
 * A screen takes its paged results as [LazyPagingItems], which only a composition can produce.
 * The type is the screen's own, so the fixture stays out of the features.
 *
 * @return [items] as a single, already loaded page.
 */
@Composable
fun <T : Any> pagingItems(items: List<T>): LazyPagingItems<T> =
    flowOf(PagingData.from(items)).collectAsLazyPagingItems()

/** @return no results at all, which is what a screen shows before anything is searched for. */
@Composable
fun <T : Any> emptyPagingItems(): LazyPagingItems<T> = pagingItems(emptyList())
