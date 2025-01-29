package com.example.mangashelf.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "mangas")
data class Manga(
    @PrimaryKey
    @SerializedName("id")
    val id: String,
    @SerializedName("image")
    val image: String,
    @SerializedName("score")
    val score: Double,
    @SerializedName("popularity")
    val popularity: Int,
    @SerializedName("title")
    val title: String,
    @SerializedName("publishedChapterDate")
    val publishedChapterDate: Long,
    @SerializedName("category")
    val category: String,
    var isFavorite: Boolean = false,
    var isRead: Boolean = false
)