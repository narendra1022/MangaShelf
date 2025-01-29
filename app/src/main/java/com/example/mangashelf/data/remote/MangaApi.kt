package com.example.mangashelf.data.remote

import com.example.mangashelf.data.model.Manga
import retrofit2.Response
import retrofit2.http.GET

interface MangaApi {
    @GET("b/KEJO")
    suspend fun getMangas(): Response<List<Manga>>
}