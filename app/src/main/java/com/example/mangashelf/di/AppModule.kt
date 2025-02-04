package com.example.mangashelf.di

import android.content.Context
import androidx.room.Room
import com.example.mangashelf.data.local.MangaDatabase
import com.example.mangashelf.data.remote.MangaApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideMangaApi(): MangaApi {

        return Retrofit.Builder()
            .baseUrl("https://www.jsonkeeper.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(MangaApi::class.java)

    }

    @Provides
    @Singleton
    fun provideMangaDatabase(
        @ApplicationContext context: Context
    ): MangaDatabase {
        return Room.databaseBuilder(
            context,
            MangaDatabase::class.java,
            "manga_database"
        ).build()
    }

    @Provides
    @Singleton
    fun provideMangaDao(database: MangaDatabase) = database.mangaDao()
}