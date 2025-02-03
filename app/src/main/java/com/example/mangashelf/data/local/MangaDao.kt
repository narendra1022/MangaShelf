package com.example.mangashelf.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.mangashelf.data.model.Manga
import kotlinx.coroutines.flow.Flow


@Dao
interface MangaDao {

    @Query("SELECT * FROM mangas ORDER BY publishedChapterDate ASC")
    fun getAllMangas(): Flow<List<Manga>>

    @Query("SELECT * FROM mangas WHERE id = :mangaId")
    fun observeMangaById(mangaId: String): Flow<Manga?>

    @Query("SELECT * FROM mangas WHERE isFavorite = 1")
    fun getFavoriteMangas(): Flow<List<Manga>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMangas(mangas: List<Manga>)

    @Update
    suspend fun updateManga(manga: Manga)

    @Query("SELECT * FROM mangas ORDER BY publishedChapterDate ASC")
    suspend fun getAllMangasOnce(): List<Manga>
}
