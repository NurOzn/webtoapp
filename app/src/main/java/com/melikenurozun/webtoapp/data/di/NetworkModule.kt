package com.melikenurozun.webtoapp.data.di

import com.melikenurozun.webtoapp.data.remote.BbcNewsService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://feeds.bbci.co.uk/")
            .client(okHttpClient)
            .build()
    }

    @Provides
    @Singleton
    fun provideBbcNewsService(retrofit: Retrofit): BbcNewsService {
        return retrofit.create(BbcNewsService::class.java)
    }
}
