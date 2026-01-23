package com.melikenurozun.webtoapp.domain.usecase

import com.melikenurozun.webtoapp.domain.model.NewsArticle
import com.melikenurozun.webtoapp.domain.repository.NewsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetReadLaterUseCase @Inject constructor(
    private val repository: NewsRepository
) {
    operator fun invoke(): Flow<List<NewsArticle>> {
        return repository.getReadLater()
    }
}
