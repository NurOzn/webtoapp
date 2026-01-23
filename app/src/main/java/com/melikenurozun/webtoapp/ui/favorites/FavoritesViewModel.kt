package com.melikenurozun.webtoapp.ui.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.melikenurozun.webtoapp.domain.model.NewsArticle
import com.melikenurozun.webtoapp.domain.usecase.GetFavoritesUseCase
import com.melikenurozun.webtoapp.domain.usecase.ToggleFavoriteUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FavoritesViewModel @Inject constructor(
    private val getFavoritesUseCase: GetFavoritesUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase
) : ViewModel() {

    private val _favorites = MutableStateFlow<List<NewsArticle>>(emptyList())
    val favorites: StateFlow<List<NewsArticle>> = _favorites.asStateFlow()

    init {
        loadFavorites()
    }

    private fun loadFavorites() {
        viewModelScope.launch {
            getFavoritesUseCase().collect { articles ->
                _favorites.value = articles
            }
        }
    }

    fun removeFavorite(article: NewsArticle) {
        viewModelScope.launch {
            toggleFavoriteUseCase(article.id, false)
        }
    }
}
