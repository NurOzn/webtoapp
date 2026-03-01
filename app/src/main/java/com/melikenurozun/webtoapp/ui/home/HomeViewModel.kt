package com.melikenurozun.webtoapp.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.melikenurozun.webtoapp.data.remote.Category
import com.melikenurozun.webtoapp.data.remote.NewsCategories
import com.melikenurozun.webtoapp.domain.model.NewsArticle
import com.melikenurozun.webtoapp.domain.usecase.GetNewsUseCase
import com.melikenurozun.webtoapp.domain.usecase.ToggleFavoriteUseCase
import com.melikenurozun.webtoapp.domain.usecase.ToggleReadLaterUseCase
import com.melikenurozun.webtoapp.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class NewsState {
    object Loading : NewsState()
    data class Success(val news: List<NewsArticle>) : NewsState()
    data class Error(val message: String) : NewsState()
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getNewsUseCase: GetNewsUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase,
    private val toggleReadLaterUseCase: ToggleReadLaterUseCase
) : ViewModel() {

    private val _state = MutableStateFlow<NewsState>(NewsState.Loading)
    val state: StateFlow<NewsState> = _state.asStateFlow()

    private val _selectedCategory = MutableStateFlow(NewsCategories.allCategories.first())
    val selectedCategory: StateFlow<Category> = _selectedCategory.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val categories: List<Category> = NewsCategories.allCategories

    // Keep reference to cancel previous flow job when category changes
    private var newsJob: Job? = null

    init {
        getNews()
    }

    fun getNews() {
        newsJob?.cancel()
        
        val newsFlow = getNewsUseCase(_selectedCategory.value.id)
        
        newsJob = newsFlow.combine(_searchQuery) { result, query ->
            when (result) {
                is Resource.Success -> {
                    val news = result.data ?: emptyList()
                    val filteredNews = if (query.isBlank()) {
                        news
                    } else {
                        news.filter { article ->
                            article.title.contains(query, ignoreCase = true) ||
                            article.description.contains(query, ignoreCase = true)
                        }
                    }
                    NewsState.Success(filteredNews)
                }
                is Resource.Error -> {
                     val data = result.data
                     if (!data.isNullOrEmpty()) {
                         val filteredNews = if (query.isBlank()) {
                             data
                         } else {
                             data.filter { article ->
                                 article.title.contains(query, ignoreCase = true) ||
                                 article.description.contains(query, ignoreCase = true)
                             }
                         }
                         NewsState.Success(filteredNews)
                     } else {
                         NewsState.Error(result.message ?: "An unexpected error occurred")
                     }
                }
                is Resource.Loading -> {
                    val data = result.data
                    if (!data.isNullOrEmpty()) {
                         val filteredNews = if (query.isBlank()) {
                             data
                         } else {
                             data.filter { article ->
                                 article.title.contains(query, ignoreCase = true) ||
                                 article.description.contains(query, ignoreCase = true)
                             }
                         }
                         NewsState.Success(filteredNews)
                    } else {
                        NewsState.Loading
                    }
                }
            }
        }.onEach { newState ->
            _state.value = newState
        }.launchIn(viewModelScope)
    }

    fun selectCategory(category: Category) {
        _selectedCategory.value = category
        _searchQuery.value = ""
        _state.value = NewsState.Loading
        getNews()
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun toggleFavorite(article: NewsArticle) {
        viewModelScope.launch {
            toggleFavoriteUseCase(article.id, !article.isFavorite)
        }
    }

    fun toggleReadLater(article: NewsArticle) {
        viewModelScope.launch {
            toggleReadLaterUseCase(article.id, !article.isReadLater)
        }
    }
}
