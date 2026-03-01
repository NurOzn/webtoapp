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
import android.util.Log
import android.util.Xml
import org.xmlpull.v1.XmlPullParser
import com.melikenurozun.webtoapp.data.remote.model.RssFeed
import com.melikenurozun.webtoapp.data.remote.model.RssChannel
import com.melikenurozun.webtoapp.data.remote.model.RssItem
import com.melikenurozun.webtoapp.data.remote.model.RssImage

class NewsRepositoryImpl @Inject constructor(
    private val api: BbcNewsService,
    private val dao: NewsDao
) : NewsRepository {

    override fun getNews(category: String): Flow<Resource<List<NewsArticle>>> = flow {
        emit(Resource.Loading())

        try {
            val responseBody = api.getNewsFeed(category)
            val xmlString = responseBody.string()
            val remoteFeed = parseRss(xmlString)
            
            val newsItems = remoteFeed.channel.items.map { rssItem ->
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
            }

            // Insert new items (will be ignored if already exist)
            dao.insertNews(newsItems.map { it.toEntity() })
        } catch (e: HttpException) {
            emit(Resource.Error(message = "Server error: ${e.message}"))
            Log.e("NewsRepository", "HTTP Error", e)
        } catch (e: IOException) {
            emit(Resource.Error(message = "Network error: ${e.message}"))
            Log.e("NewsRepository", "IO Error", e)
        } catch (e: Exception) {
            emit(Resource.Error(message = "Error: ${e.message ?: "Unknown"}"))
            Log.e("NewsRepository", "Parse Error", e)
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
            val responseBody = api.getNewsFeed(category)
            val xmlString = responseBody.string()
            val remoteFeed = parseRss(xmlString)
            
            val newsItems = remoteFeed.channel.items.map { rssItem ->
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
            }

            dao.insertNews(newsItems.map { it.toEntity() })
        } catch (e: Exception) {
            Log.e("NewsRepository", "Refresh Error", e)
        }
    }

    override suspend fun clearCache() {
        dao.clearAllNews()
    }

    private fun parseRss(xml: String): RssFeed {
        val items = mutableListOf<RssItem>()
        var feedTitle: String? = null
        var feedDesc: String? = null

        val parser = Xml.newPullParser()
        parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
        parser.setInput(java.io.StringReader(xml))

        var eventType = parser.eventType
        var currentItem: RssItem? = null
        var inTitle = false
        var inLink = false
        var inDescription = false
        var inPubDate = false

        while (eventType != XmlPullParser.END_DOCUMENT) {
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    when (parser.name) {
                        "item" -> currentItem = RssItem()
                        "title" -> inTitle = true
                        "link" -> inLink = true
                        "description" -> inDescription = true
                        "pubDate" -> inPubDate = true
                        "thumbnail", "media:thumbnail" -> {
                            val url = parser.getAttributeValue(null, "url")
                            if (url != null) {
                                currentItem?.thumbnail = RssImage(url)
                            }
                        }
                    }
                }
                XmlPullParser.TEXT -> {
                    val text = parser.text.trim()
                    if (text.isNotEmpty()) {
                        if (currentItem != null) {
                            if (inTitle) currentItem.title = text
                            else if (inLink) currentItem.link = text
                            else if (inDescription) currentItem.description = text
                            else if (inPubDate) currentItem.pubDate = text
                        } else {
                            if (inTitle) feedTitle = text
                            else if (inDescription) feedDesc = text
                        }
                    }
                }
                XmlPullParser.END_TAG -> {
                    when (parser.name) {
                        "item" -> {
                            currentItem?.let { items.add(it) }
                            currentItem = null
                        }
                        "title" -> inTitle = false
                        "link" -> inLink = false
                        "description" -> inDescription = false
                        "pubDate" -> inPubDate = false
                    }
                }
            }
            eventType = parser.next()
        }

        return RssFeed(
            channel = RssChannel(
                title = feedTitle,
                description = feedDesc,
                items = items
            )
        )
    }
}
