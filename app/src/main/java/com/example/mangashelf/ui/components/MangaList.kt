package com.example.mangashelf.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import com.example.mangashelf.data.model.MangaWithYear
import com.example.mangashelf.ui.SortType

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MangaList(
    mangaPager: LazyPagingItems<MangaWithYear>,
    currentSort: SortType,
    yearListState: LazyListState,
    onMangaClick: (String) -> Unit
) {
    LazyColumn(
        state = yearListState,
        modifier = Modifier.fillMaxSize()
    ) {
        if (currentSort == SortType.NONE || currentSort == SortType.YEAR_ASC) {
            // Grouped by year with sticky headers
            var currentYear: Int? = null
            var items = mutableListOf<MangaWithYear>()

            for (i in 0 until mangaPager.itemCount) {
                val item = mangaPager[i] ?: continue

                if (currentYear != item.year && items.isNotEmpty()) {
                    val year = currentYear!!
                    val yearItems = items.toList()

                    stickyHeader(key = "header_$year") {
                        YearHeader(year = year)
                    }

                    items(
                        items = yearItems,
                        key = { it.manga.id }
                    ) { mangaWithYear ->
                        MangaCard(
                            manga = mangaWithYear.manga,
                            onClick = { onMangaClick(mangaWithYear.manga.id) }
                        )
                    }

                    items.clear()
                }

                currentYear = item.year
                items.add(item)
            }

            // Handle last group
            if (items.isNotEmpty() && currentYear != null) {
                stickyHeader(key = "header_$currentYear") {
                    YearHeader(year = currentYear)
                }

                items(
                    items = items,
                    key = { it.manga.id }
                ) { mangaWithYear ->
                    MangaCard(
                        manga = mangaWithYear.manga,
                        onClick = { onMangaClick(mangaWithYear.manga.id) }
                    )
                }
            }
        } else {
            // Simple list for other sort types
            items(
                count = mangaPager.itemCount,
                key = { index -> mangaPager[index]?.manga?.id ?: index }
            ) { index ->
                mangaPager[index]?.let { item ->
                    MangaCard(
                        manga = item.manga,
                        onClick = { onMangaClick(item.manga.id) }
                    )
                }
            }
        }

        // Loading and error states remain the same
        when (mangaPager.loadState.append) {
            is LoadState.Loading -> {
                item { LoadingItem() }
            }

            is LoadState.Error -> {
                item {
                    ErrorItem(
                        message = "Error loading more items",
                        onRetry = { mangaPager.retry() }
                    )
                }
            }

            else -> {}
        }
    }
}

