package com.example.mangashelf.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mangashelf.data.model.Manga
import com.example.mangashelf.data.repository.MangaRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FavoriteMangaViewModel @Inject constructor(
    private val repository: MangaRepository
) : ViewModel() {

    val favoriteMangas = repository.favoriteMangas
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun toggleFavorite(manga: Manga) {
        viewModelScope.launch {
            repository.toggleFavorite(manga)
        }
    }
}