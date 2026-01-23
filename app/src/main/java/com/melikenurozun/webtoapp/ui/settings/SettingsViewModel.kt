package com.melikenurozun.webtoapp.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.melikenurozun.webtoapp.data.local.DataStoreManager
import com.melikenurozun.webtoapp.domain.usecase.ClearCacheUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val dataStoreManager: DataStoreManager,
    private val clearCacheUseCase: ClearCacheUseCase
) : ViewModel() {

    val notificationsEnabled = dataStoreManager.notificationsEnabled.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), true
    )

    val amoledMode = dataStoreManager.amoledMode.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), false
    )

    val articlesReadCount = dataStoreManager.articlesReadCount.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), 0
    )

    val favoritesCount = dataStoreManager.favoritesCount.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), 0
    )

    val notificationCategories = dataStoreManager.notificationCategories.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), setOf("news", "world", "business", "technology")
    )

    fun setNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch { dataStoreManager.setNotificationsEnabled(enabled) }
    }

    fun setAmoledMode(enabled: Boolean) {
        viewModelScope.launch { dataStoreManager.setAmoledMode(enabled) }
    }

    fun toggleNotificationCategory(categoryId: String, enabled: Boolean) {
        viewModelScope.launch {
            val current = notificationCategories.value.toMutableSet()
            if (enabled) {
                current.add(categoryId)
            } else {
                current.remove(categoryId)
            }
            dataStoreManager.setNotificationCategories(current)
        }
    }

    fun clearCache() {
        viewModelScope.launch { clearCacheUseCase() }
    }
}
