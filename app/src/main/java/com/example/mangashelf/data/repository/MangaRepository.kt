package com.example.mangashelf.data.repository

import com.example.mangashelf.data.local.MangaDao
import com.example.mangashelf.data.model.Manga
import com.example.mangashelf.data.remote.MangaApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.io.IOException
import javax.inject.Inject

class MangaRepository @Inject constructor(
    private val api: MangaApi,
    private val dao: MangaDao
) {

    suspend fun toggleRead(manga: Manga) {
        updateMangaFlag(manga) { it.copy(isRead = !it.isRead) }
    }

    private suspend fun updateMangaFlag(manga: Manga, update: (Manga) -> Manga) {
        dao.updateManga(update(manga))
    }

    fun getAllMangas(): Flow<List<Manga>> = dao.getAllMangas()
    val favoriteMangas: Flow<List<Manga>> = dao.getFavoriteMangas()
    fun getMangaById(id: String): Flow<Manga?> = dao.observeMangaById(id)

    suspend fun toggleFavorite(manga: Manga) {
        dao.updateManga(manga.copy(isFavorite = !manga.isFavorite))
    }

    suspend fun fetchMangas(): FetchResult = withContext(Dispatchers.IO) {
        val cachedData = dao.getAllMangasOnce()
        return@withContext try {
            val response = api.getMangas()
            if (response.isSuccessful && response.body() != null) {
                val newMangas = response.body()!!
                val updatedMangas = newMangas.map { newManga ->
                    cachedData.find { it.id == newManga.id }?.let { existingManga ->
                        newManga.copy(isFavorite = existingManga.isFavorite)
                    } ?: newManga
                }
                dao.insertMangas(updatedMangas)
                FetchResult.Success
            } else if (cachedData.isNotEmpty()) {
                FetchResult.DatabaseOnly
            } else {
                FetchResult.Error("Server error: ${response.code()}")
            }
        } catch (e: Exception) {
            when {
                cachedData.isNotEmpty() -> FetchResult.DatabaseOnly
                e is IOException -> FetchResult.NetworkError
                else -> FetchResult.Error(e.message ?: "Unknown error occurred")
            }
        }
    }
}

sealed class FetchResult {
    data object Success : FetchResult()
    data class Error(val message: String) : FetchResult()
    data object NetworkError : FetchResult()
    // Case when only cached database data is available
    data object DatabaseOnly : FetchResult()
}
