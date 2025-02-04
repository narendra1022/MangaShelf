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
import kotlinx.coroutines.launch

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
    val coroutineScope = rememberCoroutineScope()

    // Track programmatic scrolling with longer timeout
    var isProgrammaticScroll by remember { mutableStateOf(false) }

    // Improved scroll handling
    LaunchedEffect(selectedYear, currentSort) {
        selectedYear?.let { year ->
            if (currentSort == SortType.YEAR_ASC) {
                yearPositions[year]?.let { targetIndex ->
                    isProgrammaticScroll = true
                    coroutineScope.launch {
                        listState.animateScrollToItem(targetIndex)
                        // Add a longer delay to ensure the scroll completes
                        delay(500)
                        isProgrammaticScroll = false
                    }
                }
            }
        }
    }

    // Improved year tracking
    LaunchedEffect(listState, currentSort) {
        if (currentSort == SortType.YEAR_ASC) {
            snapshotFlow {
                val firstIndex = listState.firstVisibleItemIndex
                mangas.getOrNull(firstIndex)?.year
            }
                .distinctUntilChanged()
                .filterNotNull()
                .collect { currentYear ->
                    if (!isProgrammaticScroll) {
                        onYearChanged(currentYear)
                    }
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