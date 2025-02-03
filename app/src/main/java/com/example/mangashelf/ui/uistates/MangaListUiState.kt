package com.example.mangashelf.ui.uistates

data class MangaListUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val isOffline: Boolean = false
)