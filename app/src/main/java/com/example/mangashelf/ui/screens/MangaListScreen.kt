package com.example.mangashelf.ui.screens


import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.compose.collectAsLazyPagingItems
import com.example.mangashelf.R
import com.example.mangashelf.ui.SortType
import com.example.mangashelf.ui.components.EmptyScreen
import com.example.mangashelf.ui.components.ErrorScreen
import com.example.mangashelf.ui.components.LoadingScreen
import com.example.mangashelf.ui.components.MangaList
import com.example.mangashelf.ui.components.SortDropdownMenu
import com.example.mangashelf.ui.components.YearTabRow
import com.example.mangashelf.ui.findFirstIndexForYear
import com.example.mangashelf.ui.viewmodel.MangaListViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, FlowPreview::class)
@Composable
fun MangaListScreen(
    onMangaClick: (String) -> Unit,
    viewModel: MangaListViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val mangaPager = viewModel.mangaPager.collectAsLazyPagingItems()
    val currentSort by viewModel.currentSort.collectAsState()
    val selectedYear by viewModel.selectedYear.collectAsState()
    val yearListState = rememberLazyListState()
    var showSortMenu by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    // scroll handling
    LaunchedEffect(yearListState, currentSort) {
        if (currentSort == SortType.NONE || currentSort == SortType.YEAR_ASC) {
            snapshotFlow {
                yearListState.firstVisibleItemIndex to yearListState.layoutInfo.visibleItemsInfo.firstOrNull()?.offset
            }
                .distinctUntilChanged()
                .debounce(300L)
                .collect { (index, _) ->
                    if (mangaPager.itemCount > 0) {
                        viewModel.handleScroll(index, mangaPager)
                    }
                }
        }
    }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = { Text("MangaShelf", fontWeight = FontWeight.Bold) },
                    actions = {
                        // menu
                        IconButton(onClick = { showSortMenu = true }) {
                            Icon(
                                painter = painterResource(R.drawable.sort),
                                contentDescription = "Sort"
                            )
                        }
                        SortDropdownMenu(
                            expanded = showSortMenu,
                            onDismiss = { showSortMenu = false },
                            onSortSelected = {
                                viewModel.sortBy(it)
                                showSortMenu = false
                            }
                        )
                    }
                )

                // Year tabs
                AnimatedVisibility(
                    visible = (currentSort == SortType.NONE || currentSort == SortType.YEAR_ASC)
                            && !uiState.isLoading,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    YearTabRow(
                        mangaPager = mangaPager,
                        selectedYear = selectedYear,
                        onYearSelected = { year ->
                            viewModel.onYearSelected(year)
                            coroutineScope.launch {
                                val targetIndex = findFirstIndexForYear(mangaPager, year)
                                targetIndex?.let { index ->
                                    yearListState.animateScrollToItem(index)
                                }
                            }
                        }
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                uiState.isLoading && mangaPager.itemCount == 0 -> {
                    LoadingScreen()
                }

                uiState.error != null && mangaPager.itemCount == 0 -> {
                    ErrorScreen(
                        error = uiState.error!!,
                        onRetry = { viewModel.retry() }
                    )
                }

                mangaPager.itemCount == 0 -> {
                    EmptyScreen()
                }

                else -> {
                    MangaList(
                        mangaPager = mangaPager,
                        currentSort = currentSort,
                        yearListState = yearListState,
                        onMangaClick = onMangaClick
                    )
                }
            }
        }
    }
}
