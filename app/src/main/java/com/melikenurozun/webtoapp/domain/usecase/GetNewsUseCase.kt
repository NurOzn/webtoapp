package com.melikenurozun.webtoapp.domain.usecase

import com.melikenurozun.webtoapp.domain.model.NewsArticle
import com.melikenurozun.webtoapp.domain.repository.NewsRepository
import com.melikenurozun.webtoapp.util.Resource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetNewsUseCase @Inject constructor(
    private val repository: NewsRepository
) {
    operator fun invoke(category: String = "news"): Flow<Resource<List<NewsArticle>>> {
        return repository.getNews(category)
    }
}
