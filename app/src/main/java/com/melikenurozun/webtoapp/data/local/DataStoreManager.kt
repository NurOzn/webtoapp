package com.melikenurozun.webtoapp.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Singleton
class DataStoreManager @Inject constructor(@ApplicationContext private val context: Context) {
    
    companion object {
        // Existing keys
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        val TEXT_SIZE = stringPreferencesKey("text_size")
        
        // AMOLED Mode
        val AMOLED_MODE = booleanPreferencesKey("amoled_mode")
        
        // Reading Statistics
        val ARTICLES_READ_COUNT = intPreferencesKey("articles_read_count")
        val FAVORITES_COUNT = intPreferencesKey("favorites_count")
        
        // Notification Category Filters
        val NOTIFICATION_CATEGORIES = stringSetPreferencesKey("notification_categories")
    }

    // ===== Existing Settings =====
    val notificationsEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[NOTIFICATIONS_ENABLED] ?: true
    }

    val textSize: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[TEXT_SIZE] ?: "Medium"
    }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[NOTIFICATIONS_ENABLED] = enabled
        }
    }

    suspend fun setTextSize(size: String) {
        context.dataStore.edit { preferences ->
            preferences[TEXT_SIZE] = size
        }
    }

    // ===== AMOLED Mode =====
    val amoledMode: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[AMOLED_MODE] ?: false
    }

    suspend fun setAmoledMode(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[AMOLED_MODE] = enabled
        }
    }

    // ===== Reading Statistics =====
    val articlesReadCount: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[ARTICLES_READ_COUNT] ?: 0
    }

    val favoritesCount: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[FAVORITES_COUNT] ?: 0
    }

    suspend fun incrementArticlesRead() {
        context.dataStore.edit { preferences ->
            val current = preferences[ARTICLES_READ_COUNT] ?: 0
            preferences[ARTICLES_READ_COUNT] = current + 1
        }
    }

    suspend fun updateFavoritesCount(count: Int) {
        context.dataStore.edit { preferences ->
            preferences[FAVORITES_COUNT] = count
        }
    }

    // ===== Notification Category Filters =====
    val notificationCategories: Flow<Set<String>> = context.dataStore.data.map { preferences ->
        preferences[NOTIFICATION_CATEGORIES] ?: setOf("news", "world", "business", "technology")
    }

    suspend fun setNotificationCategories(categories: Set<String>) {
        context.dataStore.edit { preferences ->
            preferences[NOTIFICATION_CATEGORIES] = categories
        }
    }
}
