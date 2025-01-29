package com.example.mangashelf.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.example.mangashelf.data.local.MangaDao
import com.example.mangashelf.data.model.Manga
import com.example.mangashelf.data.model.MangaWithYear
import com.example.mangashelf.data.paging.MangaPagingSource
import com.example.mangashelf.data.remote.MangaApi
import com.example.mangashelf.ui.viewmodel.SortType
import kotlinx.coroutines.flow.Flow
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class MangaRepository @Inject constructor(
    private val api: MangaApi,
    private val dao: MangaDao
) {

    val mangas: Flow<List<Manga>> = dao.getAllMangas()

    val favoriteMangas: Flow<List<Manga>> = dao.getFavoriteMangas()

    sealed class FetchResult {
        data object Success : FetchResult()
        data class Error(val message: String) : FetchResult()
        data object NetworkError : FetchResult()
        data object DatabaseOnly : FetchResult()
    }

    fun getMangasPager(sortType: SortType): Flow<PagingData<MangaWithYear>> {
        return Pager(
            config = PagingConfig(
                pageSize = 20,
                enablePlaceholders = false,
                prefetchDistance = 3
            ),
            pagingSourceFactory = { MangaPagingSource(dao, sortType) }
        ).flow
    }

    suspend fun fetchMangas(): FetchResult {

        // First check if we have cached data
        val cachedData = dao.getAllMangasOnce()

        return try {
            // Attempt to fetch from network
            val response = api.getMangas()
            if (response.isSuccessful) {
                response.body()?.let { newMangas ->
                    // Preserve user preferences from cached data
                    val updatedMangas = newMangas.map { newManga ->
                        cachedData.find { it.id == newManga.id }?.let { existingManga ->
                            newManga.copy(
                                isFavorite = existingManga.isFavorite,
                                isRead = existingManga.isRead
                            )
                        } ?: newManga
                    }

                    // Update database
                    dao.insertMangas(updatedMangas)
                    FetchResult.Success
                } ?: run {
                    if (cachedData.isNotEmpty()) {
                        FetchResult.DatabaseOnly
                    } else {
                        FetchResult.Error("Empty response from server")
                    }
                }
            } else {
                if (cachedData.isNotEmpty()) {
                    FetchResult.DatabaseOnly
                } else {
                    FetchResult.Error("Server error: ${response.code()}")
                }
            }
        } catch (e: IOException) {
            // Network error (no internet, timeout, etc.)
            if (cachedData.isNotEmpty()) {
                FetchResult.DatabaseOnly
            } else {
                FetchResult.NetworkError
            }
        } catch (e: HttpException) {
            if (cachedData.isNotEmpty()) {
                FetchResult.DatabaseOnly
            } else {
                FetchResult.Error("Network error: ${e.message}")
            }
        }
    }

    fun getMangaById(id: String): Flow<Manga?> = dao.observeMangaById(id)

    suspend fun toggleFavorite(manga: Manga) {
        val updatedManga = manga.copy(isFavorite = !manga.isFavorite)
        dao.updateManga(updatedManga)
    }

    suspend fun toggleRead(manga: Manga) {
        val updatedManga = manga.copy(isRead = !manga.isRead)
        dao.updateManga(updatedManga)
    }

}