package com.example.mangashelf.ui

import androidx.paging.compose.LazyPagingItems
import com.example.mangashelf.data.model.Manga
import com.example.mangashelf.data.model.MangaWithYear

// year index finding function
fun findFirstIndexForYear(
    mangaPager: LazyPagingItems<MangaWithYear>,
    targetYear: Int
): Int? {
    for (i in 0 until mangaPager.itemCount) {
        val item = mangaPager[i]
        if (item?.year == targetYear) {
            return i
        }
    }
    return null
}

sealed class MangaDetailUiState {
    data object Loading : MangaDetailUiState()
    data class Success(val manga: Manga) : MangaDetailUiState()
    data class Error(val message: String) : MangaDetailUiState()
}

enum class SortType {
    NONE,
    SCORE_ASC,
    SCORE_DESC,
    POPULARITY_ASC,
    POPULARITY_DESC,
    YEAR_ASC
}

data class MangaListUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val isOffline: Boolean = false
)

// Sealed class representing possible outcomes of manga fetch operations
sealed class FetchResult {
    data object Success : FetchResult()
    data class Error(val message: String) : FetchResult()
    data object NetworkError : FetchResult()
    // Case when only cached database data is available
    data object DatabaseOnly : FetchResult()
}
