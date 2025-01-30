package com.example.mangashelf.ui.viewmodel


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.AsyncPagingDataDiffer
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.compose.LazyPagingItems
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListUpdateCallback
import com.example.mangashelf.data.model.MangaWithYear
import com.example.mangashelf.data.repository.MangaRepository
import com.example.mangashelf.data.repository.MangaRepository.FetchResult
import com.example.mangashelf.ui.MangaListUiState
import com.example.mangashelf.ui.SortType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
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

    // Paging data with error handling
    @OptIn(ExperimentalCoroutinesApi::class)
    val mangaPager = _currentSort
        .flatMapLatest { sortType ->
            repository.getMangasPager(sortType)
                .catch { error ->
                    _uiState.update {
                        it.copy(
                            error = error.message ?: "Unknown error occurred",
                            isLoading = false
                        )
                    }
                }
        }
        .cachedIn(viewModelScope)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = PagingData.empty()
        )

    private val noopListUpdateCallback = object : ListUpdateCallback {
        override fun onInserted(position: Int, count: Int) {}
        override fun onRemoved(position: Int, count: Int) {}
        override fun onMoved(fromPosition: Int, toPosition: Int) {}
        override fun onChanged(position: Int, count: Int, payload: Any?) {}
    }

    private fun observeAvailableYears() {
        viewModelScope.launch {
            val differ = AsyncPagingDataDiffer(
                diffCallback = object : DiffUtil.ItemCallback<MangaWithYear>() {
                    override fun areItemsTheSame(
                        oldItem: MangaWithYear,
                        newItem: MangaWithYear
                    ): Boolean {
                        return oldItem.manga.id == newItem.manga.id
                    }

                    override fun areContentsTheSame(
                        oldItem: MangaWithYear,
                        newItem: MangaWithYear
                    ): Boolean {
                        return oldItem == newItem
                    }
                },
                updateCallback = noopListUpdateCallback,
                mainDispatcher = Dispatchers.Main,
                workerDispatcher = Dispatchers.Default
            )

            mangaPager.collectLatest { pagingData ->
                differ.submitData(pagingData)

                val years = mutableSetOf<Int>()
                for (i in 0 until differ.itemCount) {
                    differ.peek(i)?.year?.let { years.add(it) }
                }

            }
        }
    }

    fun sortBy(sortType: SortType) {
        _currentSort.value = sortType
        if (sortType != SortType.NONE) {
            _selectedYear.value = null
        }
    }

    fun onYearSelected(year: Int) {
        viewModelScope.launch {
            _selectedYear.value = year
        }
    }

    fun handleScroll(index: Int, pagingItems: LazyPagingItems<MangaWithYear>) {
        if (index >= 0 && index < pagingItems.itemCount) {
            pagingItems[index]?.let { mangaWithYear ->
                onYearSelected(mangaWithYear.year)
            }
        }
    }

    fun retry() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = repository.fetchMangas()) {
                is FetchResult.Success -> {
                    _uiState.update { it.copy(isLoading = false) }
                }

                is FetchResult.Error -> {
                    _uiState.update {
                        it.copy(
                            error = result.message,
                            isLoading = false
                        )
                    }
                }

                is FetchResult.NetworkError -> {
                    _uiState.update {
                        it.copy(
                            error = "Network error. Please check your connection.",
                            isLoading = false
                        )
                    }
                }

                is FetchResult.DatabaseOnly -> {
                    _uiState.update {
                        it.copy(
                            isOffline = true,
                            isLoading = false
                        )
                    }
                }
            }
        }
    }

    init {
        retry()
        fetchMangas()
        observeMangas()
        observeAvailableYears()
    }

    private fun observeMangas() {
        viewModelScope.launch {
            repository.mangas.collect()
        }
    }

    fun fetchMangas() {
        viewModelScope.launch {
            repository.fetchMangas()
        }
    }

}
