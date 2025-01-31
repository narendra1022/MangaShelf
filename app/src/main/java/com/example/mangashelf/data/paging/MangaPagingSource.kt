package com.example.mangashelf.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.mangashelf.data.local.MangaDao
import com.example.mangashelf.data.model.Manga
import com.example.mangashelf.data.model.MangaWithYear
import com.example.mangashelf.ui.SortType
import java.util.Calendar

class MangaPagingSource(
    private val dao: MangaDao,
    private val sortType: SortType
) : PagingSource<Int, MangaWithYear>() {

    // Calculates the key for refreshing the paging data
    override fun getRefreshKey(state: PagingState<Int, MangaWithYear>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            val closestPage = state.closestPageToPosition(anchorPosition)
            closestPage?.prevKey?.plus(1) ?: closestPage?.nextKey?.minus(1)
        }
    }

    // Loads a page of manga data based on the provided parameters
    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, MangaWithYear> {
        val page = params.key ?: 0
        val pageSize = params.loadSize

        return try {
            val mangas = loadMangasBasedOnSortType(sortType, pageSize, page)
            val mangasWithYear = mangas.map { it.toMangaWithYear() }

            LoadResult.Page(
                data = mangasWithYear,
                prevKey = if (page == 0) null else page - 1,
                nextKey = if (mangas.size < pageSize) null else page + 1
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    // Loads manga data from database based on sort type
    private suspend fun loadMangasBasedOnSortType(
        sortType: SortType,
        pageSize: Int,
        page: Int
    ): List<Manga> = when (sortType) {
        SortType.SCORE_ASC -> dao.getMangasByScoreAsc(pageSize, page * pageSize)
        SortType.SCORE_DESC -> dao.getMangasByScoreDesc(pageSize, page * pageSize)
        SortType.POPULARITY_ASC -> dao.getMangasByPopularityAsc(pageSize, page * pageSize)
        SortType.POPULARITY_DESC -> dao.getMangasByPopularityDesc(pageSize, page * pageSize)
        SortType.NONE, SortType.YEAR_ASC -> dao.getMangasByYearAsc(pageSize, page * pageSize)
    }

    // Converts Manga to MangaWithYear by extracting year from timestamp
    private fun Manga.toMangaWithYear(): MangaWithYear {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = publishedChapterDate * 1000L
        }
        return MangaWithYear(this, calendar.get(Calendar.YEAR))
    }
}