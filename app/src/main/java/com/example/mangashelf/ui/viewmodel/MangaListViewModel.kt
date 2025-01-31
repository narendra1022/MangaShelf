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
import com.example.mangashelf.ui.FetchResult
import com.example.mangashelf.ui.MangaListUiState
import com.example.mangashelf.ui.SortType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MangaListViewModel @Inject constructor(
    private val repository: MangaRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(MangaListUiState())
    val uiState = _uiState.asStateFlow()

    // Sort and filter state
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
                    handleError(error)
                }
        }
        .cachedIn(viewModelScope)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = PagingData.empty()
        )

    // DiffUtil callback for manga comparison
    private val mangaDiffCallback = object : DiffUtil.ItemCallback<MangaWithYear>() {
        override fun areItemsTheSame(oldItem: MangaWithYear, newItem: MangaWithYear): Boolean =
            oldItem.manga.id == newItem.manga.id

        override fun areContentsTheSame(oldItem: MangaWithYear, newItem: MangaWithYear): Boolean =
            oldItem == newItem
    }

    // Efficient no-op callback for diff updates
    private val noopListUpdateCallback = object : ListUpdateCallback {
        override fun onInserted(position: Int, count: Int) {}
        override fun onRemoved(position: Int, count: Int) {}
        override fun onMoved(fromPosition: Int, toPosition: Int) {}
        override fun onChanged(position: Int, count: Int, payload: Any?) {}
    }

    init {
        initializeData()
    }

    // Initialize data and start observations
    private fun initializeData() {
        viewModelScope.launch {
            retry()
            observeAvailableYears()
        }
    }

    // Observe and collect available years from paging data
    private fun observeAvailableYears() {
        viewModelScope.launch(Dispatchers.Default) {
            val differ = AsyncPagingDataDiffer(
                diffCallback = mangaDiffCallback,
                updateCallback = noopListUpdateCallback,
                mainDispatcher = Dispatchers.Main.immediate,
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


    // Handle sorting changes
    fun sortBy(sortType: SortType) {
        viewModelScope.launch {
            _currentSort.value = sortType
            if (sortType != SortType.NONE) {
                _selectedYear.value = null
            }
        }
    }

    // Update selected year
    fun onYearSelected(year: Int) {
        viewModelScope.launch {
            _selectedYear.value = year
        }
    }

    // Handle scroll events and update selected year
    fun handleScroll(index: Int, pagingItems: LazyPagingItems<MangaWithYear>) {
        if (index in 0 until pagingItems.itemCount) {
            pagingItems[index]?.let { mangaWithYear ->
                onYearSelected(mangaWithYear.year)
            }
        }
    }

    // Retry failed operations
    fun retry() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            handleFetchResult(repository.fetchMangas())
        }
    }

    // Handle different fetch results
    private fun handleFetchResult(result: FetchResult) {
        val newState = when (result) {
            is FetchResult.Success -> _uiState.value.copy(
                isLoading = false,
                error = null,
                isOffline = false
            )

            is FetchResult.Error -> _uiState.value.copy(
                error = result.message,
                isLoading = false
            )

            is FetchResult.NetworkError -> _uiState.value.copy(
                error = "Network error. Please check your connection.",
                isLoading = false
            )

            is FetchResult.DatabaseOnly -> _uiState.value.copy(
                isOffline = true,
                isLoading = false
            )
        }
        _uiState.value = newState
    }

    // Handle errors from paging
    private fun handleError(error: Throwable) {
        _uiState.update {
            it.copy(
                error = error.message ?: "Unknown error occurred",
                isLoading = false
            )
        }
    }

}