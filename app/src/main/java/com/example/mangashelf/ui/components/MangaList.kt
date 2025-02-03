package com.example.mangashelf.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import com.example.mangashelf.data.model.Manga
import com.example.mangashelf.data.model.MangaWithYear
import com.example.mangashelf.ui.viewmodel.SortType
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MangaList(
    mangas: List<MangaWithYear>,
    currentSort: SortType,
    selectedYear: Int?,
    yearPositions: Map<Int, Int>,
    onYearChanged: (Int) -> Unit,
    onMangaClick: (String) -> Unit,
    onFavoriteClick: (Manga) -> Unit,
    favorites: Map<String, Boolean>,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()

    // Track programmatic scrolling
    var isProgrammaticScroll by remember { mutableStateOf(false) }

    // Handle scrolling to year when selected from tab
    LaunchedEffect(selectedYear) {
        selectedYear?.let { year ->
            if (currentSort == SortType.YEAR_ASC) {
                val targetIndex = yearPositions[year] ?: 0
                if (targetIndex != -1) {
                    isProgrammaticScroll = true
                    listState.animateScrollToItem(targetIndex)
                    delay(300)
                    isProgrammaticScroll = false
                }
            }
        }
    }

    LaunchedEffect(listState) {
        snapshotFlow { listState.firstVisibleItemIndex }
            .map { index -> mangas.getOrNull(index)?.year }
            .distinctUntilChanged() // Only update when year changes
            .filterNotNull()
            .collect { currentYear ->
                if (!isProgrammaticScroll && currentSort == SortType.YEAR_ASC) {
                    onYearChanged(currentYear)
                }
            }
    }


    LazyColumn(
        state = listState,
        modifier = modifier.fillMaxSize()
    ) {
        if (currentSort == SortType.YEAR_ASC) {
            val groupedMangas = mangas.groupBy { it.year }

            groupedMangas.forEach { (year, mangasInYear) ->
                stickyHeader(key = "header_$year") {
                    YearHeader(year = year)
                }

                items(
                    items = mangasInYear,
                    key = { it.manga.id }
                ) { mangaWithYear ->
                    MangaCard(
                        manga = mangaWithYear.manga,
                        isFavorite = favorites[mangaWithYear.manga.id] ?: false,
                        onFavoriteClick = { onFavoriteClick(mangaWithYear.manga) },
                        onClick = { onMangaClick(mangaWithYear.manga.id) }
                    )
                }
            }
        } else {
            items(
                items = mangas,
                key = { it.manga.id }
            ) { mangaWithYear ->
                MangaCard(
                    manga = mangaWithYear.manga,
                    isFavorite = favorites[mangaWithYear.manga.id] ?: false,
                    onFavoriteClick = { onFavoriteClick(mangaWithYear.manga) },
                    onClick = { onMangaClick(mangaWithYear.manga.id) }
                )
            }
        }
    }
}