package com.example.mangashelf.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mangashelf.data.model.Manga
import com.example.mangashelf.data.model.MangaWithYear
import com.example.mangashelf.data.repository.FetchResult
import com.example.mangashelf.data.repository.MangaRepository
import com.example.mangashelf.ui.uistates.MangaListUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class MangaListViewModel @Inject constructor(
    private val repository: MangaRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MangaListUiState())
    val uiState = _uiState.asStateFlow()

    private val _currentSort = MutableStateFlow(SortType.YEAR_ASC)
    val currentSort = _currentSort.asStateFlow()

    private val _selectedYear = MutableStateFlow<Int?>(null)
    val selectedYear = _selectedYear.asStateFlow()

    private val _mangas = MutableStateFlow<List<MangaWithYear>>(emptyList())
    val mangas = _mangas.asStateFlow()

    private val _yearPositions = MutableStateFlow<Map<Int, Int>>(emptyMap()) // Map<Year, FirstIndex>
    val yearPositions = _yearPositions.asStateFlow()

    private val _isFavorite = MutableStateFlow<Map<String, Boolean>>(emptyMap())
    val isFavorite = _isFavorite.asStateFlow()

    init {
        initializeData()
        observeFavorites()
    }

    private fun initializeData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            when (val result = repository.fetchMangas()) {
                is FetchResult.Success -> loadMangas()
                is FetchResult.DatabaseOnly -> loadMangas()
                is FetchResult.Error -> handleError(result.message)
                is FetchResult.NetworkError -> handleError("Network error. Please check your connection.")
            }
        }
    }

    private fun loadMangas() {
        viewModelScope.launch {
            repository.getAllMangas()
                .collect { mangaList ->
                    val mangasWithYear = mangaList.map { manga ->
                        MangaWithYear(
                            manga = manga,
                            year = SimpleDateFormat("yyyy", Locale.getDefault())
                                .format(Date(manga.publishedChapterDate * 1000L))
                                .toInt()
                        )
                    }

                    // Group mangas by year and calculate the first index of each year
                    val yearPositionsMap = mutableMapOf<Int, Int>()
                    var currentIndex = 0
                    mangasWithYear.groupBy { it.year }
                        .toSortedMap()
                        .forEach { (year, mangasInYear) ->
                            yearPositionsMap[year] = currentIndex
                            currentIndex += mangasInYear.size
                        }
                    _yearPositions.value = yearPositionsMap

                    // Update mangas list based on sort
                    val sortedMangas = when (currentSort.value) {
                        SortType.YEAR_ASC -> mangasWithYear.sortedBy { it.year }
                        SortType.SCORE_DESC -> mangasWithYear.sortedByDescending { it.manga.score }
                        SortType.SCORE_ASC -> mangasWithYear.sortedBy { it.manga.score }
                        SortType.POPULARITY_DESC -> mangasWithYear.sortedByDescending { it.manga.popularity }
                        SortType.POPULARITY_ASC -> mangasWithYear.sortedBy { it.manga.popularity }
                    }
                    _mangas.value = sortedMangas

                    // Set initial selected year if not set
                    if (_selectedYear.value == null) {
                        _selectedYear.value = sortedMangas.firstOrNull()?.year
                    }

                    _uiState.update { it.copy(isLoading = false, error = null) }
                }
        }
    }

    private fun observeFavorites() {
        viewModelScope.launch {
            repository.favoriteMangas.collect { favorites ->
                _isFavorite.value = favorites.associateBy({ it.id }, { true })
            }
        }
    }

    fun toggleFavorite(manga: Manga) {
        viewModelScope.launch {
            repository.toggleFavorite(manga)
        }
    }

    fun sortBy(sortType: SortType) {
        viewModelScope.launch {
            _currentSort.value = sortType
            loadMangas()
        }
    }


    fun onYearSelected(year: Int) {
        viewModelScope.launch {
            _selectedYear.value = year
        }
    }
    fun retry() {
        initializeData()
    }

    private fun handleError(message: String) {
        _uiState.update { it.copy(error = message, isLoading = false) }
    }
}

enum class SortType {
    SCORE_ASC,
    SCORE_DESC,
    POPULARITY_ASC,
    POPULARITY_DESC,
    YEAR_ASC
}