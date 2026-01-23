package com.melikenurozun.webtoapp.domain.model

data class NewsArticle(
    val id: String = "",
    val title: String,
    val description: String,
    val link: String,
    val pubDate: String,
    val imageUrl: String?,
    val category: String = "news",
    val isFavorite: Boolean = false,
    val isReadLater: Boolean = false
)
