package com.example.mangashelf.ui.uistates

import com.example.mangashelf.data.model.Manga

sealed class MangaDetailUiState {
    data object Loading : MangaDetailUiState()
    data class Success(val manga: Manga) : MangaDetailUiState()
    data class Error(val message: String) : MangaDetailUiState()
}
