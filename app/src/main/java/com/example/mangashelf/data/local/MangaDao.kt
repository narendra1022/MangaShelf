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

    @Query("SELECT * FROM mangas order by publishedChapterDate asc")
    fun getAllMangas(): Flow<List<Manga>>

    @Query("SELECT * FROM mangas WHERE id = :mangaId")
    fun observeMangaById(mangaId: String): Flow<Manga?>

    @Query("SELECT * FROM mangas WHERE isFavorite = 1")
    fun getFavoriteMangas(): Flow<List<Manga>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMangas(mangas: List<Manga>)

    @Update
    suspend fun updateManga(manga: Manga)

    @Query("SELECT * FROM mangas ORDER BY score ASC LIMIT :limit OFFSET :offset")
    suspend fun getMangasByScoreAsc(limit: Int, offset: Int): List<Manga>

    @Query("SELECT * FROM mangas ORDER BY score DESC LIMIT :limit OFFSET :offset")
    suspend fun getMangasByScoreDesc(limit: Int, offset: Int): List<Manga>

    @Query("SELECT * FROM mangas ORDER BY popularity ASC LIMIT :limit OFFSET :offset")
    suspend fun getMangasByPopularityAsc(limit: Int, offset: Int): List<Manga>

    @Query("SELECT * FROM mangas ORDER BY popularity DESC LIMIT :limit OFFSET :offset")
    suspend fun getMangasByPopularityDesc(limit: Int, offset: Int): List<Manga>

    @Query("SELECT * FROM mangas ORDER BY publishedChapterDate ASC LIMIT :limit OFFSET :offset")
    suspend fun getMangasByYearAsc(limit: Int, offset: Int): List<Manga>

    @Query("SELECT * FROM mangas order by publishedChapterDate asc")
    suspend fun getAllMangasOnce(): List<Manga>

}