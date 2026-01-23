package com.melikenurozun.webtoapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.melikenurozun.webtoapp.domain.model.NewsArticle

@Entity(tableName = "news_table")
data class NewsEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val description: String,
    val link: String,
    val pubDate: String,
    val imageUrl: String?,
    val category: String = "news",
    val isFavorite: Boolean = false,
    val isReadLater: Boolean = false
) {
    fun toDomainModel(): NewsArticle {
        return NewsArticle(
            id = id,
            title = title,
            description = description,
            link = link,
            pubDate = pubDate,
            imageUrl = imageUrl,
            category = category,
            isFavorite = isFavorite,
            isReadLater = isReadLater
        )
    }
}

fun NewsArticle.toEntity(): NewsEntity {
    return NewsEntity(
        id = if (id.isNotEmpty()) id else link.hashCode().toString(),
        title = title,
        description = description,
        link = link,
        pubDate = pubDate,
        imageUrl = imageUrl,
        category = category,
        isFavorite = isFavorite,
        isReadLater = isReadLater
    )
}
