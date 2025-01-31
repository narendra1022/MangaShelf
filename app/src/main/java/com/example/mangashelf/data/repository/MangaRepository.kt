package com.example.mangashelf.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.example.mangashelf.data.local.MangaDao
import com.example.mangashelf.data.model.Manga
import com.example.mangashelf.data.model.MangaWithYear
import com.example.mangashelf.data.paging.MangaPagingSource
import com.example.mangashelf.data.remote.MangaApi
import com.example.mangashelf.ui.FetchResult
import com.example.mangashelf.ui.SortType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException
import javax.inject.Inject

class MangaRepository @Inject constructor(
    private val api: MangaApi,
    private val dao: MangaDao
) {

    val favoriteMangas: Flow<List<Manga>> = dao.getFavoriteMangas()

    fun getMangaById(id: String): Flow<Manga?> = dao.observeMangaById(id)

    suspend fun toggleFavorite(manga: Manga) {
        updateMangaFlag(manga) { it.copy(isFavorite = !it.isFavorite) }
    }

    suspend fun toggleRead(manga: Manga) {
        updateMangaFlag(manga) { it.copy(isRead = !it.isRead) }
    }

    // Creates paging source for manga data with specified sorting
    // focused on delivering paged data from the database.
    fun getMangasPager(sortType: SortType): Flow<PagingData<MangaWithYear>> {
        return Pager(
            config = PagingConfig(
                pageSize = PAGE_SIZE,
                enablePlaceholders = false,
                prefetchDistance = PREFETCH_DISTANCE
            ),
            pagingSourceFactory = { MangaPagingSource(dao, sortType) }
        ).flow
    }

    // Fetches manga data from network and updates local database
    // handles network operations and updates the local database
    suspend fun fetchMangas(): FetchResult = withContext(Dispatchers.IO) {
        val cachedData = dao.getAllMangasOnce()

        return@withContext try {
            handleApiResponse(
                apiCall = { api.getMangas() },
                cachedData = cachedData,
                onSuccess = { newMangas -> updateDatabase(cachedData, newMangas) }
            )
        } catch (e: Exception) {
            handleNetworkError(e, cachedData)
        }
    }

    // Updates database with new manga data while preserving user preferences
    private suspend fun updateDatabase(cachedData: List<Manga>, newMangas: List<Manga>) {
        val updatedMangas = newMangas.map { newManga ->
            cachedData.find { it.id == newManga.id }?.let { existingManga ->
                newManga.copy(
                    isFavorite = existingManga.isFavorite,
                    isRead = existingManga.isRead
                )
            } ?: newManga
        }
        dao.insertMangas(updatedMangas)
    }

    // Handles network errors and determines appropriate response
    private fun handleNetworkError(error: Exception, cachedData: List<Manga>): FetchResult {
        return when {
            cachedData.isNotEmpty() -> FetchResult.DatabaseOnly
            error is IOException -> FetchResult.NetworkError
            error is HttpException -> FetchResult.Error("Network error: ${error.message}")
            else -> FetchResult.Error("Unknown error: ${error.message}")
        }
    }

    // Processes API response and handles different response scenarios
    private suspend fun handleApiResponse(
        apiCall: suspend () -> Response<List<Manga>>,
        cachedData: List<Manga>,
        onSuccess: suspend (List<Manga>) -> Unit
    ): FetchResult {
        val response = apiCall()
        return when {
            response.isSuccessful && response.body() != null -> {
                onSuccess(response.body()!!)
                FetchResult.Success
            }

            cachedData.isNotEmpty() -> FetchResult.DatabaseOnly
            else -> FetchResult.Error("Server error: ${response.code()}")
        }
    }

    private suspend fun updateMangaFlag(manga: Manga, update: (Manga) -> Manga) {
        dao.updateManga(update(manga))
    }

    companion object {
        // Default page size for pagination
        const val PAGE_SIZE = 20

        // Number of items to prefetch
        const val PREFETCH_DISTANCE = 3
    }
}