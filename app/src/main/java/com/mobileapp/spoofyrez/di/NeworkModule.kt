package com.mobileapp.spoofyrez.di

import com.mobileapp.spoofyrez.data.api.SpotifyService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

// Stolen code ehhehehehehehe
// prolly won't use
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    @Provides
    @Singleton
    fun provideSpotifyService(): SpotifyService {
        return Retrofit.Builder()
            .build()
            .create(SpotifyService::class.java)
    }
}