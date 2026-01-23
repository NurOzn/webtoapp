package com.melikenurozun.webtoapp.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.melikenurozun.webtoapp.data.local.dao.NewsDao
import com.melikenurozun.webtoapp.data.local.entity.NewsEntity

@Database(entities = [NewsEntity::class], version = 2, exportSchema = false)
abstract class NewsDatabase : RoomDatabase() {
    abstract fun newsDao(): NewsDao

    companion object {
        const val DATABASE_NAME = "news_db"
    }
}
