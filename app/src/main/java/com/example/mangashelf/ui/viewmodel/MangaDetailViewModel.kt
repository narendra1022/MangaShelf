package com.example.mangashelf.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mangashelf.data.repository.MangaRepository
import com.example.mangashelf.ui.MangaDetailUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MangaDetailViewModel @Inject constructor(
    private val repository: MangaRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow<MangaDetailUiState>(MangaDetailUiState.Loading)
    val uiState: StateFlow<MangaDetailUiState> = _uiState.asStateFlow()

    private var currentMangaId: String? = null

    // Create a flow to observe manga updates
    private var mangaUpdateJob: Job? = null

    fun loadManga(id: String) {
        currentMangaId = id

        // Cancel any existing observation
        mangaUpdateJob?.cancel()

        // Start new observation
        mangaUpdateJob = viewModelScope.launch {
            // Observe the manga from the database
            repository.getMangaById(id).collect { manga ->
                _uiState.value = if (manga != null) {
                    MangaDetailUiState.Success(manga)
                } else {
                    MangaDetailUiState.Error("Manga not found")
                }
            }
        }
    }

    fun toggleFavorite() {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState is MangaDetailUiState.Success) {
                val manga = currentState.manga
                repository.toggleFavorite(manga)
            }
        }
    }

    fun toggleRead() {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState is MangaDetailUiState.Success) {
                val manga = currentState.manga
                repository.toggleRead(manga)
            }
        }
    }

    fun retry() {
        currentMangaId?.let { loadManga(it) }
    }

    override fun onCleared() {
        super.onCleared()
        mangaUpdateJob?.cancel()
    }
}

