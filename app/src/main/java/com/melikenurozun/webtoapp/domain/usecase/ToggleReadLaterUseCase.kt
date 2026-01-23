package com.melikenurozun.webtoapp.domain.usecase

import com.melikenurozun.webtoapp.domain.repository.NewsRepository
import javax.inject.Inject

class ToggleReadLaterUseCase @Inject constructor(
    private val repository: NewsRepository
) {
    suspend operator fun invoke(newsId: String, isReadLater: Boolean) {
        repository.toggleReadLater(newsId, isReadLater)
    }
}
