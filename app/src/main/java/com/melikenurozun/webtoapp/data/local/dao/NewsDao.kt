package com.melikenurozun.webtoapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.melikenurozun.webtoapp.data.local.entity.NewsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NewsDao {
    @Query("SELECT * FROM news_table WHERE category = :category ORDER BY id DESC")
    fun getNewsByCategory(category: String): Flow<List<NewsEntity>>

    @Query("SELECT * FROM news_table ORDER BY id DESC")
    fun getAllNews(): Flow<List<NewsEntity>>

    @Query("SELECT * FROM news_table WHERE isFavorite = 1 ORDER BY id DESC")
    fun getFavorites(): Flow<List<NewsEntity>>

    @Query("SELECT * FROM news_table WHERE isReadLater = 1 ORDER BY id DESC")
    fun getReadLater(): Flow<List<NewsEntity>>

    @Query("SELECT * FROM news_table WHERE id = :id LIMIT 1")
    suspend fun getNewsById(id: String): NewsEntity?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertNews(news: List<NewsEntity>)

    @Update
    suspend fun updateNews(news: NewsEntity)

    @Query("UPDATE news_table SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun updateFavorite(id: String, isFavorite: Boolean)

    @Query("UPDATE news_table SET isReadLater = :isReadLater WHERE id = :id")
    suspend fun updateReadLater(id: String, isReadLater: Boolean)

    @Query("DELETE FROM news_table WHERE category = :category AND isFavorite = 0 AND isReadLater = 0")
    suspend fun clearNewsByCategory(category: String)

    @Query("DELETE FROM news_table WHERE isFavorite = 0 AND isReadLater = 0")
    suspend fun clearAllNews()
}
