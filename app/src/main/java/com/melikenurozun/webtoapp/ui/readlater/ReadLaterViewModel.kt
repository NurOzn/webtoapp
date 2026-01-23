package com.melikenurozun.webtoapp.ui.readlater

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.melikenurozun.webtoapp.domain.model.NewsArticle
import com.melikenurozun.webtoapp.domain.usecase.GetReadLaterUseCase
import com.melikenurozun.webtoapp.domain.usecase.ToggleReadLaterUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReadLaterViewModel @Inject constructor(
    private val getReadLaterUseCase: GetReadLaterUseCase,
    private val toggleReadLaterUseCase: ToggleReadLaterUseCase
) : ViewModel() {

    private val _readLaterList = MutableStateFlow<List<NewsArticle>>(emptyList())
    val readLaterList: StateFlow<List<NewsArticle>> = _readLaterList.asStateFlow()

    init {
        loadReadLater()
    }

    private fun loadReadLater() {
        viewModelScope.launch {
            getReadLaterUseCase().collect { articles ->
                _readLaterList.value = articles
            }
        }
    }

    fun removeFromReadLater(article: NewsArticle) {
        viewModelScope.launch {
            toggleReadLaterUseCase(article.id, false)
        }
    }
}
