package com.melikenurozun.webtoapp.domain.usecase

import com.melikenurozun.webtoapp.domain.repository.NewsRepository
import javax.inject.Inject

class ToggleFavoriteUseCase @Inject constructor(
    private val repository: NewsRepository
) {
    suspend operator fun invoke(newsId: String, isFavorite: Boolean) {
        repository.toggleFavorite(newsId, isFavorite)
    }
}
