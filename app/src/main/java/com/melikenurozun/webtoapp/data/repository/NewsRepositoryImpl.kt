package com.melikenurozun.webtoapp.data.repository

import com.melikenurozun.webtoapp.data.local.dao.NewsDao
import com.melikenurozun.webtoapp.data.local.entity.toEntity
import com.melikenurozun.webtoapp.data.remote.BbcNewsService
import com.melikenurozun.webtoapp.domain.model.NewsArticle
import com.melikenurozun.webtoapp.domain.repository.NewsRepository
import com.melikenurozun.webtoapp.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import java.io.IOException
import retrofit2.HttpException
import javax.inject.Inject

class NewsRepositoryImpl @Inject constructor(
    private val api: BbcNewsService,
    private val dao: NewsDao
) : NewsRepository {

    override fun getNews(category: String): Flow<Resource<List<NewsArticle>>> = flow {
        emit(Resource.Loading())

        try {
            val remoteFeed = api.getNewsFeed(category)
            val newsItems = remoteFeed.channel.items?.map { rssItem ->
                val link = rssItem.link ?: ""
                NewsArticle(
                    id = link.hashCode().toString(),
                    title = rssItem.title ?: "No Title",
                    description = rssItem.description ?: "No Description",
                    link = link,
                    pubDate = rssItem.pubDate ?: "",
                    imageUrl = rssItem.thumbnail?.url,
                    category = category
                )
            } ?: emptyList()

            // Insert new items (will be ignored if already exist)
            dao.insertNews(newsItems.map { it.toEntity() })
        } catch (e: HttpException) {
            emit(Resource.Error(message = "Server error: ${e.message}"))
            android.util.Log.e("NewsRepository", "HTTP Error", e)
        } catch (e: IOException) {
            emit(Resource.Error(message = "Network error: ${e.message}"))
            android.util.Log.e("NewsRepository", "IO Error", e)
        } catch (e: Exception) {
            emit(Resource.Error(message = "Error: ${e.message ?: "Unknown"}"))
            android.util.Log.e("NewsRepository", "Parse Error", e)
        }

        // Emit the latest data from database
        dao.getNewsByCategory(category).collect { entities ->
            emit(Resource.Success(entities.map { it.toDomainModel() }))
        }
    }

    override fun getFavorites(): Flow<List<NewsArticle>> {
        return dao.getFavorites().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override fun getReadLater(): Flow<List<NewsArticle>> {
        return dao.getReadLater().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override suspend fun toggleFavorite(newsId: String, isFavorite: Boolean) {
        dao.updateFavorite(newsId, isFavorite)
    }

    override suspend fun toggleReadLater(newsId: String, isReadLater: Boolean) {
        dao.updateReadLater(newsId, isReadLater)
    }

    override suspend fun refreshNews(category: String) {
        try {
            val remoteFeed = api.getNewsFeed(category)
            val newsItems = remoteFeed.channel.items?.map { rssItem ->
                val link = rssItem.link ?: ""
                NewsArticle(
                    id = link.hashCode().toString(),
                    title = rssItem.title ?: "No Title",
                    description = rssItem.description ?: "No Description",
                    link = link,
                    pubDate = rssItem.pubDate ?: "",
                    imageUrl = rssItem.thumbnail?.url,
                    category = category
                )
            } ?: emptyList()

            dao.insertNews(newsItems.map { it.toEntity() })
        } catch (e: Exception) {
            android.util.Log.e("NewsRepository", "Refresh Error", e)
        }
    }
    override suspend fun clearCache() {
        dao.clearAllNews()
    }
}
