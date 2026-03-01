package com.melikenurozun.webtoapp.data.remote

import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Path

interface BbcNewsService {
    @GET("{category}/rss.xml")
    suspend fun getNewsFeed(@Path("category", encoded = true) category: String = "news"): ResponseBody
}

// BBC News Kategorileri
object NewsCategories {
    const val NEWS = "news"
    const val WORLD = "news/world"
    const val BUSINESS = "news/business"
    const val TECHNOLOGY = "news/technology"
    const val SCIENCE = "news/science_and_environment"
    const val ENTERTAINMENT = "news/entertainment_and_arts"
    const val SPORT = "sport"

    val allCategories = listOf(
        Category(NEWS, "News", "📰"),
        Category(WORLD, "World", "🌍"),
        Category(BUSINESS, "Business", "💼"),
        Category(TECHNOLOGY, "Technology", "💻"),
        Category(SCIENCE, "Science", "🔬"),
        Category(ENTERTAINMENT, "Entertainment", "🎬"),
        Category(SPORT, "Sport", "⚽")
    )
}

data class Category(
    val id: String,
    val name: String,
    val icon: String
)
