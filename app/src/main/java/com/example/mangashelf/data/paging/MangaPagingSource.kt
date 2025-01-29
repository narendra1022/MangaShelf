package com.example.mangashelf.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.mangashelf.data.local.MangaDao
import com.example.mangashelf.data.model.MangaWithYear
import com.example.mangashelf.ui.viewmodel.SortType
import java.util.Calendar

class MangaPagingSource(
    private val dao: MangaDao,
    private val sortType: SortType
) : PagingSource<Int, MangaWithYear>() {

    override fun getRefreshKey(state: PagingState<Int, MangaWithYear>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, MangaWithYear> {
        val page = params.key ?: 0
        val pageSize = params.loadSize

        return try {

            val mangas = when (sortType) {
                SortType.SCORE_ASC -> dao.getMangasByScoreAsc(pageSize, page * pageSize)
                SortType.SCORE_DESC -> dao.getMangasByScoreDesc(pageSize, page * pageSize)
                SortType.POPULARITY_ASC -> dao.getMangasByPopularityAsc(pageSize, page * pageSize)
                SortType.POPULARITY_DESC -> dao.getMangasByPopularityDesc(pageSize, page * pageSize)
                SortType.NONE -> dao.getMangasPaged(pageSize, page * pageSize)
            }

            val mangasWithYear = mangas.map { manga ->
                val year = Calendar.getInstance().apply {
                    timeInMillis = manga.publishedChapterDate * 1000L
                }.get(Calendar.YEAR)
                MangaWithYear(manga, year)
            }

            LoadResult.Page(
                data = mangasWithYear,
                prevKey = if (page == 0) null else page - 1,
                nextKey = if (mangas.size < pageSize) null else page + 1
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }
}