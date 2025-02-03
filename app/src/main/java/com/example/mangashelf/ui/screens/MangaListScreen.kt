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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.mangashelf.R
import com.example.mangashelf.ui.components.EmptyScreen
import com.example.mangashelf.ui.components.ErrorScreen
import com.example.mangashelf.ui.components.LoadingScreen
import com.example.mangashelf.ui.components.MangaList
import com.example.mangashelf.ui.components.SortDropdownMenu
import com.example.mangashelf.ui.components.YearTabRow
import com.example.mangashelf.ui.viewmodel.MangaListViewModel
import com.example.mangashelf.ui.viewmodel.SortType


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MangaListScreen(
    onMangaClick: (String) -> Unit,
    viewModel: MangaListViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val mangas by viewModel.mangas.collectAsState()
    val currentSort by viewModel.currentSort.collectAsState()
    val selectedYear by viewModel.selectedYear.collectAsState()
    val yearPositions by viewModel.yearPositions.collectAsState()
    val favorites by viewModel.isFavorite.collectAsState()
    var showSortMenu by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = { Text("MangaShelf", fontWeight = FontWeight.Bold) },
                    actions = {
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

                AnimatedVisibility(
                    visible = currentSort == SortType.YEAR_ASC,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    val years = mangas.map { it.year }.distinct()
                    YearTabRow(
                        years = years,
                        selectedYear = selectedYear,
                        onYearSelected = viewModel::onYearSelected
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
                uiState.isLoading && mangas.isEmpty() -> LoadingScreen()
                uiState.error != null && mangas.isEmpty() -> {
                    ErrorScreen(
                        error = uiState.error!!,
                        onRetry = { viewModel.retry() }
                    )
                }
                mangas.isEmpty() -> EmptyScreen()
                else -> {
                    MangaList(
                        mangas = mangas,
                        currentSort = currentSort,
                        selectedYear = selectedYear,
                        yearPositions = yearPositions,
                        onYearChanged = viewModel::onYearSelected,
                        favorites = favorites,
                        onMangaClick = onMangaClick,
                        onFavoriteClick = { viewModel.toggleFavorite(it) }
                    )
                }
            }
        }
    }
}