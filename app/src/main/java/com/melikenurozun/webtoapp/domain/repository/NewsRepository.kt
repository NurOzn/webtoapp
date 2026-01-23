package com.melikenurozun.webtoapp.domain.repository

import com.melikenurozun.webtoapp.domain.model.NewsArticle
import kotlinx.coroutines.flow.Flow
import com.melikenurozun.webtoapp.util.Resource

interface NewsRepository {
    fun getNews(category: String = "news"): Flow<Resource<List<NewsArticle>>>
    fun getFavorites(): Flow<List<NewsArticle>>
    fun getReadLater(): Flow<List<NewsArticle>>
    suspend fun toggleFavorite(newsId: String, isFavorite: Boolean)
    suspend fun toggleReadLater(newsId: String, isReadLater: Boolean)
    suspend fun refreshNews(category: String = "news")
    suspend fun clearCache()
}
